package com.riis.androidarduino.bt;

import com.riis.androidarduino.bt.R;

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
	
	UsbCommWrapper usbHost;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        usbHost = new UsbCommWrapper(this);
        
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
			         	usbHost.sendByte(LED_OFF);
			         	((Button)v).setText("Turn Red On");
			         	isRedLEDOn = false;
					} else {
						usbHost.sendByte(LED_ON);
			         	((Button)v).setText("Turn Red Off");
			         	isRedLEDOn = true;
					}
					usbHost.sendByte(LED1);
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
			         	usbHost.sendByte(LED_OFF);
			         	((Button)v).setText("Turn Yellow On");
			         	isYellowLEDOn = false;
					} else {
						usbHost.sendByte(LED_ON);
			         	((Button)v).setText("Turn Yellow Off");
			         	isYellowLEDOn = true;
					}
					usbHost.sendByte(LED2);
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
			         	usbHost.sendByte(LED_OFF);
			         	((Button)v).setText("Turn Green On");
			         	isGreenLEDOn = false;
					} else {
						usbHost.sendByte(LED_ON);
			         	((Button)v).setText("Turn Green Off");
			         	isGreenLEDOn = true;
					}
					usbHost.sendByte(LED3);
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