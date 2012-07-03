package com.riis.androidarduino.btrs232;

import com.riis.androidarduino.btrs232.R;
import com.riis.androidarduino.lib.BlueToothComm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	private static String DEVICE_NAME = "AndroidArduinoBTRS232";
	
	private Button connectButton;
	private Button disconnectButton;
	
	private EditText msgBox;
	private Button sendMsgButton;

	private BlueToothComm btComm;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setUpGUI();
    }
    
    private void setUpGUI() {
    	setUpConnectButton();
    	setUpDisconnectButton();
    	setupMsgBox();
    	setupSendMsgButton();
	}
    
    private void setupMsgBox() {
    	msgBox = (EditText) findViewById(R.id.messageBox);
    }
    
    private void setupSendMsgButton() {
    	sendMsgButton = (Button) findViewById(R.id.sendButton);
    	sendMsgButton.setOnClickListener(
    	    		new OnClickListener() {
    					public void onClick(View v) {
    						//btComm.sendString(msgBox.getText().toString());
    						btComm.sendByteWithFlag('T', (byte) 1);
    						msgBox.setText("");
    					}
    	    		}
    	    	);
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
	}
	
	@Override
	public void onPause() {
		super.onPause();
		btComm.pauseConnection();
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
    
    public BlueToothComm getBlueToothComm() {
    	return btComm;
    }
}