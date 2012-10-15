package com.riis.androidarduino.btrs232;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.riis.androidarduino.lib.BluetoothComm;

public class MainActivity extends Activity {
	private static String DEVICE_NAME = "AndroidArduinoBTRS232";
	
	private Button connectButton;
	private Button disconnectButton;
	
	private ScrollView scrollContainer;
	private TextView msgLog;
	private EditText msgBox;
	private Button sendMsgButton;
	
	private volatile boolean keepRunning;
	private boolean lastStatus;
	private Thread msgThread;
	
	protected Handler handler;

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
						String newMsg = DEVICE_NAME + ": " + btComm.readMessage();
						appendMsgToMsgLog(newMsg);
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
        setUpGUI();

		appendMsgToMsgLog("Waiting for Bluetooth connection...");
    }
    
    private void setUpGUI() {
    	setUpConnectButton();
    	setUpDisconnectButton();
    	setupHandler();
    	setupMsgLog();
    	setupMsgBox();
    	setupSendMsgButton();
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
		    	msgLog.append((String) msg.obj + "\n");
		    	scrollContainer.fullScroll(View.FOCUS_DOWN);
			}
		};
	}
    
    private void setupMsgLog() {
    	scrollContainer = (ScrollView)findViewById(R.id.scrollView);
    	msgLog = (TextView) findViewById(R.id.messageLog);
    	msgLog.append("Android Service Init...\n");
    	msgLog.setMovementMethod(new ScrollingMovementMethod());
    }
    
    private void setupMsgBox() {
    	msgBox = (EditText) findViewById(R.id.messageBox);
    	msgBox.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
    	msgBox.setImeOptions(EditorInfo.IME_ACTION_SEND);
    	msgBox.setOnEditorActionListener(new OnEditorActionListener() {
    	    @Override
    	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    	        if (actionId == EditorInfo.IME_ACTION_SEND) {
    	            sendMessage();
    	            return true;
    	        }
    	        return false;
    	    }
    	});
    }
    
    private void sendMessage() {
    	appendMsgToMsgLog("Me: " + msgBox.getText().toString());
		btComm.sendString(msgBox.getText().toString());
		msgBox.setText("");
    }
    
    private void appendMsgToMsgLog(String str) {
		Message msg = Message.obtain(handler);
		msg.obj = str;
		handler.sendMessage(msg);
    }
    
    private void setupSendMsgButton() {
    	sendMsgButton = (Button) findViewById(R.id.sendButton);
    	sendMsgButton.setOnClickListener(
    	    		new OnClickListener() {
    					public void onClick(View v) {
    						sendMessage();
    					}
    	    		}
    	    	);
    }
    
    @Override
	public void onResume() {
		super.onResume();
		
		try {
			if(btComm == null) {
				btComm = new BluetoothComm(DEVICE_NAME);
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
}