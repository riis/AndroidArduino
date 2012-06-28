package com.riis.androidarduino.bt;

import com.riis.androidarduino.bt.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private static String DEVICE_NAME = "AndroidArduinoBT";
	
	private final byte LED_OFF = 1;
	private final byte LED_ON = 2;
	private final byte LED1 = 3;
	private final byte LED2 = 4;
	private final byte LED3 = 5;
	
	private Button connectButton;
	private Button disconnectButton;
	
	private Button redButton;
	private Button yellowButton;
	private Button greenButton;
	
	private boolean isRedLEDOn = false;
	private boolean isYellowLEDOn = false;
	private boolean isGreenLEDOn = false;
	
	private BlueToothComm btComm;
	
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
		
		if(btComm == null) {
			btComm = new BlueToothComm(this, DEVICE_NAME);
		} else {
			btComm.connect(DEVICE_NAME);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		btComm.disconnect();
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
    				btComm.connect(DEVICE_NAME);
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
    
	private void setUpRedButton() {
		redButton = (Button)findViewById(R.id.redButton);
        redButton.setOnClickListener(
    		new OnClickListener() {
				public void onClick(View v) {
					if(isRedLEDOn) {
			         	((Button)v).setText("Turn Red On");
			         	btComm.sendByte(LED_OFF);
			         	isRedLEDOn = false;
					} else {
			         	((Button)v).setText("Turn Red Off");
			         	btComm.sendByte(LED_ON);
			         	isRedLEDOn = true;
					}
					btComm.sendByte(LED1);
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
			         	btComm.sendByte(LED_OFF);
			         	isYellowLEDOn = false;
					} else {
			         	((Button)v).setText("Turn Yellow Off");
			         	btComm.sendByte(LED_ON);
			         	isYellowLEDOn = true;
					}
					btComm.sendByte(LED2);
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
						btComm.sendByte(LED_OFF);
		                isGreenLEDOn = false;
					} else {
			         	((Button)v).setText("Turn Green Off");
			         	btComm.sendByte(LED_ON);
			         	isGreenLEDOn = true;
					}
					btComm.sendByte(LED3);
				}
    		}
    	);
	}
}