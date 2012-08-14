package com.riis.androidarduino.card;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import com.riis.androidarduino.lib.BluetoothComm;


import android.annotation.SuppressLint;
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
import android.widget.Toast;

public class MainActivity extends Activity {
	private static String DEVICE_NAME = "AndroidArduinoBTRS232";
	
	private Button connectButton;
	private Button disconnectButton;
	
	private ScrollView logScrollContainer;
	private TextView msgLog;
	
	private LinearLayout scannedContainer;
	private ArrayList<ScannedCreditCard> scanList;
	
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
						String cardInfo = btComm.readMessage();
						addItemToScanLog(cardInfo);
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
        
		appendMsgToMsgLog("Waiting for Bluetooth connection...");
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
    				try {
						btComm.connect();
					} catch (IOException e) {
						Toast.makeText(MainActivity.this, "Couldn't connect!", Toast.LENGTH_SHORT).show();
					}
    			}
    		}
    	);
    }
    
    private void setUpDisconnectButton() {
    	disconnectButton = (Button)findViewById(R.id.disconnectButton);
    	disconnectButton.setOnClickListener(
    		new OnClickListener() {
    			public void onClick(View v) {
    				try {
						btComm.disconnect();
					} catch (IOException e) {
						Toast.makeText(MainActivity.this, "Couldn't disconnect!", Toast.LENGTH_SHORT).show();
					}
    			}
    		}
    	);
    }
    
    @SuppressLint("HandlerLeak")
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
			    	ScannedCreditCard scannedCard = new ScannedCreditCard();
			    	scannedCard.parseData(message, 55);
					boolean newCard = true;
					for(ScannedCreditCard card : scanList) {
						if(scannedCard.matches(card)) {
							newCard = false;
							break;
						}
					}
					if(newCard) {
				    	scanList.add(scannedCard);
				    	LinearLayout newScanView = new LinearLayout(context);
				    	TextView scanTracks = new TextView(context);
				    	TextView scanDate = new TextView(context);
				    	
				    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1);
				    	
				    	scanTracks.setLayoutParams(params);
				    	scanTracks.setText(scannedCard.generateCardInfoString());
				    	
				    	scanDate.setLayoutParams(params);
				    	scanDate.setText(scannedCard.getScanDate().toGMTString());
				    	
				    	newScanView.addView(scanTracks);
				    	newScanView.addView(scanDate);
				    	
				    	scannedContainer.addView(newScanView, 0);
					}
					else
						Toast.makeText(getApplicationContext(), "Card has already been read.", Toast.LENGTH_SHORT).show();
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
    	scanList = new ArrayList<ScannedCreditCard>();
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
		
		try {
			if(btComm == null) {
				btComm = new BluetoothComm(DEVICE_NAME);
				btComm.connect();
			} else {
				btComm.resumeConnection();
			}
		} catch(IOException e) {
			Toast.makeText(MainActivity.this, "Couldn't connect!", Toast.LENGTH_SHORT).show();
		}
		
		btComm.shouldPrintLogMsgs(true);
		msgThread = new Thread(msgUpdateThread);
		msgThread.start();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		keepRunning = false;
		try {
			btComm.pauseConnection();
		} catch (IOException e) {
			Toast.makeText(MainActivity.this, "Couldn't disconnect!", Toast.LENGTH_SHORT).show();
		}
	}
    
    public BluetoothComm getBlueToothComm() {
    	return btComm;
    }
}