package com.riis.androidarduino.card;

import java.util.ArrayList;
import java.util.Calendar;

import com.riis.androidarduino.lib.BluetoothComm;


import android.app.Activity;
import android.content.Context;
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

	private BluetoothComm btComm;
	
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
				
				String message = tokens[1];
				if(tokens[0].equals("LOG")) {
					msgLog.append(message + "\n");
			    	logScrollContainer.fullScroll(View.FOCUS_DOWN);
				} else if(tokens[0].equals("SCAN")) {
					message = decodeMessage(message);
					String[] tracks = message.split("[%?;+]+");
			    	
			    	ScannedObject scan = new ScannedObject();
			    	scan.setScanDate(Calendar.getInstance().getTime());
			    	if(tracks.length >= 1)
			    		scan.setTrack1(tracks[0]);
			    	else
			    		scan.setTrack1("");
			    	if(tracks.length >= 2)
			    		scan.setTrack2(tracks[1]);
			    	else
			    		scan.setTrack2("");
			    	if(tracks.length >= 3)
			    		scan.setTrack3(tracks[2]);
			    	else
			    		scan.setTrack3("");
			    	scanList.add(scan);
			    	LinearLayout newScanView = new LinearLayout(context);
			    	TextView scanTracks = new TextView(context);
			    	TextView scanDate = new TextView(context);
			    	
			    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1);
			    	
			    	scanTracks.setLayoutParams(params);
			    	scanTracks.setText(scan.getTrack1() + "\n" + scan.getTrack2() + "\n" + scan.getTrack3());
			    	
			    	scanDate.setLayoutParams(params);
			    	scanDate.setText(scan.getScanDate().toGMTString());
			    	
			    	newScanView.addView(scanTracks);
			    	newScanView.addView(scanDate);
			    	
			    	scannedContainer.addView(newScanView, 0);
				}
			}
		};
	}
    
    private String decodeMessage(String msg) {
    	String decoded = "";
    	for(int i = 0; i < msg.length(); i++) {
    		decoded += (char)(msg.charAt(i) ^ 55);
    	}
    	return decoded;
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
			btComm = new BluetoothComm(this, DEVICE_NAME);
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
    
    public BluetoothComm getBlueToothComm() {
    	return btComm;
    }
}