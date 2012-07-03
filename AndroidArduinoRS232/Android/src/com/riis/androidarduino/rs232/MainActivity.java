package com.riis.androidarduino.rs232;

import com.riis.androidarduino.lib.FlagMsg;
import com.riis.androidarduino.lib.UsbComm;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {
	
	private ScrollView scrollContainer;
	private TextView msgLog;
	private EditText msgBox;
	private Button sendMsgButton;
	
	private UsbComm usbComm;
	
	private volatile boolean keepRunning;
	private boolean displayedStatus;
	private boolean lastStatus;
	private Thread msgThread;

	protected Handler handler;
	
	private Runnable msgUpdateThread = new Runnable() { 
		public void run() {
			while(keepRunning) {
				if(!displayedStatus) {
					if(usbComm.isConnected()) {
						appendMsgToMsgLog("Usb Connected!");
					}
					else {
						appendMsgToMsgLog("Usb disconnected!");
						appendMsgToMsgLog("Waiting for USB device...");
					}
					displayedStatus = true;
				}
				
				if(usbComm.isConnected()) {
					if(!lastStatus) {
						lastStatus = true;
						displayedStatus = false;
					}
					
					String newMsg = "UsbHost: ";
		        	while(usbComm.hasNewMessages()) {
		        		if(usbComm.readMessage().getReading() != 0) {
		        			newMsg += Integer.toString(usbComm.readMessage().getReading());
		        		}
		        		else {
		        			appendMsgToMsgLog(newMsg);
		        			break;
		        		}
		        	}
		        } else {
		        	if(lastStatus) {
						lastStatus = false;
						displayedStatus = false;
					}
		        }
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        usbComm = new UsbComm(this);
        keepRunning = true;
        displayedStatus = false;
        lastStatus = false;
        setUpGUI();
        msgThread = new Thread(msgUpdateThread);
    }
    
    private void setUpGUI() {
    	setupHandler();
    	setupMsgLog();
    	setupMsgBox();
    	setupSendMsgButton();
	}
    
    protected void setupHandler() {
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
    	            InputMethodManager inputManager =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    	            return true;
    	        }
    	        return false;
    	    }
    	});
    }
    
    private void sendMessage() {
    	msgLog.append("Me: " + msgBox.getText().toString() + "\n");
		usbComm.sendString(msgBox.getText().toString());
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
	public Object onRetainNonConfigurationInstance() {
		// In case the app is restarted, try to retain the usb accessory object
		// so that the connection to the device is not lost.
		if (usbComm.getAccessory() != null) {
			return usbComm.getAccessory();
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}
	
	@Override
	public void onResume() {
		Log.v("Arduino App", "Resuming");
		keepRunning = true;
		msgThread.start();
		super.onResume();
		usbComm.resumeConnection();
	}

	@Override
	public void onPause() {
		Log.v("Arduino App", "Pausing");
		keepRunning = false;
		try {
			msgThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.onPause();
		usbComm.pauseConnection();
	}

	@Override
	public void onDestroy() {
		Log.v("Arduino App", "Destroying");
		usbComm.unregisterReceiver();
		super.onDestroy();
	}
	
	public UsbComm getUsbComm() {
		return usbComm;
	}
}