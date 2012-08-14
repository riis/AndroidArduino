package com.riis.androidarduino.bt;

import java.io.IOException;

import com.riis.androidarduino.bt.R;
import com.riis.androidarduino.lib.BluetoothComm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static String DEVICE_NAME = "AndroidArduinoBT";
	
	private final byte LED_OFF = 2;
	private final byte LED_ON = 1;
	private final byte LED1 = 'r';
	private final byte LED2 = 'y';
	private final byte LED3 = 'g';
	
	private Button connectButton;
	private Button disconnectButton;
	
	private Button redButton;
	private Button yellowButton;
	private Button greenButton;
	
	private boolean isRedLEDOn = false;
	private boolean isYellowLEDOn = false;
	private boolean isGreenLEDOn = false;
	
	private BluetoothComm btComm;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setUpButtons();
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
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
			btComm.pauseConnection();
		} catch (IOException e) {
			Toast.makeText(MainActivity.this, "Couldn't disconnect!", Toast.LENGTH_SHORT).show();
		}
	}
    
    private void setUpButtons() {
    	setUpConnectButton();
    	setUpDisconnectButton();
    	
	    setUpRedButton();
		setUpYellowButton();
		setUpGreenButton();
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
    
	private void setUpRedButton() {
		redButton = (Button)findViewById(R.id.redButton);
        redButton.setOnClickListener(
    		new OnClickListener() {
				public void onClick(View v) {
					if(isRedLEDOn) {
			         	((Button)v).setText("Turn Red On");
			         	btComm.sendByteWithFlag('L', LED1);
			         	btComm.sendByteWithFlag('L', LED_OFF);
			         	isRedLEDOn = false;
					} else {
			         	((Button)v).setText("Turn Red Off");
			         	btComm.sendByteWithFlag('L', LED1);
			         	btComm.sendByteWithFlag('L', LED_ON);
			         	isRedLEDOn = true;
					}
				}
    		}
    	);
	}
	
	private void setUpYellowButton() {
		yellowButton = (Button)findViewById(R.id.yellowButton);
        yellowButton.setOnClickListener(
    		new OnClickListener() {
				public void onClick(View v) {
					if(isYellowLEDOn) {
			         	((Button)v).setText("Turn Yellow On");
			         	btComm.sendByteWithFlag('L', LED2);
			         	btComm.sendByteWithFlag('L', LED_OFF);
			         	isYellowLEDOn = false;
					} else {
			         	((Button)v).setText("Turn Yellow Off");
			         	btComm.sendByteWithFlag('L', LED2);
			         	btComm.sendByteWithFlag('L', LED_ON);
			         	isYellowLEDOn = true;
					}
				}
    		}
    	);
	}
	
	private void setUpGreenButton() {
		greenButton = (Button)findViewById(R.id.greenButton);
        greenButton.setOnClickListener(
    		new OnClickListener() {
				public void onClick(View v) {
					if(isGreenLEDOn) {
						((Button)v).setText("Turn Green On");
			         	btComm.sendByteWithFlag('L', LED3);
			         	btComm.sendByteWithFlag('L', LED_OFF);
		                isGreenLEDOn = false;
					} else {
			         	((Button)v).setText("Turn Green Off");
			         	btComm.sendByteWithFlag('L', LED3);
			         	btComm.sendByteWithFlag('L', LED_ON);
			         	isGreenLEDOn = true;
					}
				}
    		}
    	);
	}
}