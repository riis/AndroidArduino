package com.riis.androidarduino.usb;

import com.riis.androidarduino.usb.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private final byte LED_OFF = 1;
	private final byte LED_ON = 2;
	private final byte LED1 = 3;
	private final byte LED2 = 4;
	private final byte LED3 = 5;
	
	private Button redButton;
	private Button yellowButton;
	private Button greenButton;
	
	private boolean isRedLEDOn = false;
	private boolean isYellowLEDOn = false;
	private boolean isGreenLEDOn = false;
	
	UsbCommWrapper usb;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        usb = new UsbCommWrapper(this);
        
        setUpButtons();
    }
    
    private void setUpButtons() {
	    setUpRedButton();
		setUpYellowButton();
		setUpGreenButton();
	}
	
	private void setUpRedButton() {
		redButton = (Button)findViewById(R.id.redButton);
        redButton.setOnClickListener(
    		new OnClickListener(){
				public void onClick(View v) {
					if(isRedLEDOn) {
			         	usb.sendByte(LED_OFF);
			         	((Button)v).setText("Turn Red On");
			         	isRedLEDOn = false;
					} else {
						usb.sendByte(LED_ON);
			         	((Button)v).setText("Turn Red Off");
			         	isRedLEDOn = true;
					}
					usb.sendByte(LED1);
				}
    		}
    	);
	}
	
	private void setUpYellowButton() {
		yellowButton = (Button)findViewById(R.id.yellowButton);
        yellowButton.setOnClickListener(
    		new OnClickListener(){
				public void onClick(View v) {
					if(isYellowLEDOn) {
			         	usb.sendByte(LED_OFF);
			         	((Button)v).setText("Turn Yellow On");
			         	isYellowLEDOn = false;
					} else {
						usb.sendByte(LED_ON);
			         	((Button)v).setText("Turn Yellow Off");
			         	isYellowLEDOn = true;
					}
					usb.sendByte(LED2);
				}
    		}
    	);
	}
	
	private void setUpGreenButton() {
		greenButton = (Button)findViewById(R.id.greenButton);
        greenButton.setOnClickListener(
    		new OnClickListener(){
				public void onClick(View v) {
					if(isGreenLEDOn) {
			         	usb.sendByte(LED_OFF);
			         	((Button)v).setText("Turn Green On");
			         	isGreenLEDOn = false;
					} else {
						usb.sendByte(LED_ON);
			         	((Button)v).setText("Turn Green Off");
			         	isGreenLEDOn = true;
					}
					usb.sendByte(LED3);
				}
    		}
    	);
	}
    
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (usb.getAccessory() != null) {
			return usb.getAccessory();
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}
	
	@Override
	public void onResume() {
		Log.v("Arduino App", "Resuming");
		super.onResume();
		usb.resumeConnection();
	}

	@Override
	public void onPause() {
		Log.v("Arduino App", "Pausing");
		super.onPause();
		usb.pauseConnection();
	}

	@Override
	public void onDestroy() {
		Log.v("Arduino App", "Destroying");
		usb.unregisterReceiver();
		super.onDestroy();
	}
}