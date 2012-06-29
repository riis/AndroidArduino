package com.riis.androidarduino.usb;

import com.riis.androidarduino.lib.UsbComm;
import com.riis.androidarduino.usb.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private final byte LED_OFF = 0;
	private final byte LED_ON = 1;
	private final char LED1 = 'r';
	private final char LED2 = 'y';
	private final char LED3 = 'g';
	
	private Button redButton;
	private Button yellowButton;
	private Button greenButton;
	
	private boolean isRedLEDOn = false;
	private boolean isYellowLEDOn = false;
	private boolean isGreenLEDOn = false;
	
	UsbComm usbHost;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        usbHost = new UsbComm(this);
        
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
			         	usbHost.sendByte(LED1, LED_OFF);
			         	((Button)v).setText("Turn Red On");
			         	isRedLEDOn = false;
					} else {
						usbHost.sendByte(LED1, LED_ON);
			         	((Button)v).setText("Turn Red Off");
			         	isRedLEDOn = true;
					}
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
			         	usbHost.sendByte(LED2, LED_OFF);
			         	((Button)v).setText("Turn Yellow On");
			         	isYellowLEDOn = false;
					} else {
						usbHost.sendByte(LED2, LED_ON);
			         	((Button)v).setText("Turn Yellow Off");
			         	isYellowLEDOn = true;
					}
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
			         	usbHost.sendByte(LED3, LED_OFF);
			         	((Button)v).setText("Turn Green On");
			         	isGreenLEDOn = false;
					} else {
						usbHost.sendByte(LED3, LED_ON);
			         	((Button)v).setText("Turn Green Off");
			         	isGreenLEDOn = true;
					}
				}
    		}
    	);
	}
    
	@Override
	public Object onRetainNonConfigurationInstance() {
		// In case the app is restarted, try to retain the usb accessory object
		// so that the connection to the device is not lost.
		if (usbHost.getAccessory() != null) {
			return usbHost.getAccessory();
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}
	
	@Override
	public void onResume() {
		Log.v("Arduino App", "Resuming");
		super.onResume();
		usbHost.resumeConnection();
	}

	@Override
	public void onPause() {
		Log.v("Arduino App", "Pausing");
		super.onPause();
		usbHost.pauseConnection();
	}

	@Override
	public void onDestroy() {
		Log.v("Arduino App", "Destroying");
		usbHost.unregisterReceiver();
		super.onDestroy();
	}
}