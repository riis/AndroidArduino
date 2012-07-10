package com.riis.androidarduino.barcode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import com.riis.androidarduino.lib.BlueToothComm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static String DEVICE_NAME = "AndroidArduinoBTRS232";
	private static final String GOOGLE_SHOPPER_PACKAGE = "com.google.android.apps.shopper";
	private static final String GOOGLE_SHOPPER_ACTIVITY = GOOGLE_SHOPPER_PACKAGE + ".results.SearchResultsActivity";
	private static final String MARKET_URI_PREFIX = "market://details?id=";
	
	private Button connectButton;
	private Button disconnectButton;
	
	private ScrollView logScrollContainer;
	private TextView msgLog;
	
	private LinearLayout scannedContainer;
	private ScrollView scannedScroll;
	private ArrayList<ScannedObject> scanList;
	
	private volatile boolean keepRunning;
	private boolean lastStatus;
	private Thread msgThread;
	
	private static Handler handler;
	private Context context;

	private BlueToothComm btComm;
	
	private final DialogInterface.OnClickListener shopperMarketListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI_PREFIX + GOOGLE_SHOPPER_PACKAGE)));
		}
	};
	
	private Runnable msgUpdateThread = new Runnable() { 
		public void run() {
			while(keepRunning) {
				if(btComm.isConnected()) {
					if(!lastStatus) {
						lastStatus = true;
						appendMsgToMsgLog("Bluetooth Connected!");
					}
					
					if(btComm.isMessageReady()) {
						String barcode = btComm.readMessage();
						appendMsgToMsgLog("Scanned: " + barcode);
						addItemToScanLog(barcode);
					}
		        	
		        } else {
		        	if(lastStatus) {
						lastStatus = false;
						appendMsgToMsgLog("Bluetooth Disconnected!");
						appendMsgToMsgLog("Waiting for Bluetooth connection...");
					}
		        }
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        keepRunning = true;
        lastStatus = false;
        context = this;
        setUpGUI();
    }
    
    private void setUpGUI() {
    	setUpConnectButton();
    	setUpDisconnectButton();
    	setupHandler();
    	setupMsgLog();
    	setupScanLog();
	}
    
    private void setUpConnectButton() {
    	connectButton = (Button)findViewById(R.id.connectButton);
    	connectButton.setOnClickListener(
    		new OnClickListener() {
    			public void onClick(View v) {
    				btComm.connect();
    			}
    		}
    	);
    }
    
    private void setUpDisconnectButton() {
    	disconnectButton = (Button)findViewById(R.id.disconnectButton);
    	disconnectButton.setOnClickListener(
    		new OnClickListener() {
    			public void onClick(View v) {
    				btComm.disconnect();
    			}
    		}
    	);
    }
    
    private void setupHandler() {
		handler = new Handler() {
			public void handleMessage(Message msg) {
				String taggedMessage = (String) msg.obj;
				String[] tokens = taggedMessage.split("~");
				
				final String message = tokens[1];
				if(tokens[0].equals("LOG")) {
					msgLog.append(message + "\n");
			    	logScrollContainer.fullScroll(View.FOCUS_DOWN);
				} else if(tokens[0].equals("SCAN")) {
//			    	scannedScroll.fullScroll(View.FOCUS_DOWN);
			    	
			    	ScannedObject scan = new ScannedObject(message, Calendar.getInstance().getTime());
			    	scanList.add(scan);
			    	LinearLayout newScanView = new LinearLayout(context);
			    	TextView scanCode = new TextView(context);
			    	TextView scanDate = new TextView(context);
			    	Button lookupItem = new Button(context);
			    	
			    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1);
			    	
			    	
			    	scanCode.setLayoutParams(params);
			    	scanCode.setText(message);
			    	
			    	scanDate.setLayoutParams(params);
			    	scanDate.setText(scan.scanDate.toGMTString());
			    	
			    	lookupItem.setLayoutParams(params);
			    	lookupItem.setText("Lookup Item (Google Shopper)");
			    	lookupItem.setOnClickListener(
			    		new OnClickListener() {
			    			public void onClick(View v) {
			    				openGoogleShopper(message);
			    			}
			    		}
			    	);
			    	
			    	newScanView.addView(scanCode);
			    	newScanView.addView(scanDate);
			    	newScanView.addView(lookupItem);
			    	
			    	scannedContainer.addView(newScanView, 0);
				}
			}
		};
	}
    
    private void setupMsgLog() {
    	logScrollContainer = (ScrollView)findViewById(R.id.scrollView);
    	msgLog = (TextView) findViewById(R.id.messageLog);
    	msgLog.append("Android Service Init...\n");
    	msgLog.setMovementMethod(new ScrollingMovementMethod());
    }
    
    private void setupScanLog() {
    	scannedContainer = (LinearLayout)findViewById(R.id.scannedItems);
    	scannedScroll = (ScrollView)findViewById(R.id.scannedScrollView);
    	scanList = new ArrayList<ScannedObject>();
    }
    
    private void appendMsgToMsgLog(String str) {
		Message msg = Message.obtain(handler);
		msg.obj = "LOG~" + str;
		handler.sendMessage(msg);
    }
    
    private void addItemToScanLog(final String itemCode) {
    	Message msg = Message.obtain(handler);
		msg.obj = "SCAN~" + itemCode;
		handler.sendMessage(msg);
    }
    
    private void openGoogleShopper(String query) {
		// Construct Intent to launch Shopper
		Intent intent = new Intent(Intent.ACTION_SEARCH);
		intent.setClassName(GOOGLE_SHOPPER_PACKAGE, GOOGLE_SHOPPER_ACTIVITY);
		intent.putExtra(SearchManager.QUERY, query);

		// Is it available?
		PackageManager pm = getPackageManager();
		Collection<?> availableApps = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		
		if (availableApps != null && !availableApps.isEmpty()) {
			// If something can handle it, start it
			startActivity(intent);
		} else {
			// Otherwise offer to install it from Market.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Google shopper not found!");
			builder.setMessage("You currently do not have Google Shopper installed, would you like to install it now?");
			builder.setPositiveButton("Install", shopperMarketListener);
			builder.setNegativeButton("Not now", null);
			builder.show();
		}
    }
    
    @Override
	public void onResume() {
		super.onResume();
		
		if(btComm == null) {
			btComm = new BlueToothComm(this, DEVICE_NAME);
		} else {
			btComm.resumeConnection();
		}
		
		btComm.shouldPrintLogMsgs(true);
		msgThread = new Thread(msgUpdateThread);
		msgThread.start();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		keepRunning = false;
		btComm.pauseConnection();
	}
    
    
    
    public BlueToothComm getBlueToothComm() {
    	return btComm;
    }
}