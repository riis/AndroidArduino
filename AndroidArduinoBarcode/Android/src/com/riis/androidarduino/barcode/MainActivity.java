package com.riis.androidarduino.barcode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.riis.androidarduino.lib.BlueToothComm;
import com.riis.androidarduino.lib.FlagMsg;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {
	private static String DEVICE_NAME = "AndroidArduinoBTRS232";
	
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
	
	protected Handler handler;
	private Context context;

	private BlueToothComm btComm;
	
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
			    	
			    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1);
			    	
			    	scanCode.setLayoutParams(params);
			    	scanCode.setText(message);
			    	
			    	scanDate.setLayoutParams(params);
			    	scanDate.setText(scan.scanDate.toGMTString());
			    	
			    	lookupItem.setLayoutParams(params);
			    	lookupItem.setText("Lookup Item (Google Shopper)");
			    	lookupItem.setOnClickListener(
			    		new OnClickListener() {
			    			public void onClick(View v) {
			    				Toast.makeText(getApplicationContext(), "YOU CLICKED ON ITEM " + message + "!!", Toast.LENGTH_SHORT).show();
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