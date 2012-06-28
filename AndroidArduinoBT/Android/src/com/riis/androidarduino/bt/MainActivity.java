package com.riis.androidarduino.bt;

import com.riis.androidarduino.bt.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import at.abraxas.amarino.Amarino;

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
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
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
			         	((Button)v).setText("Turn Red On");
			         	Amarino.sendDataToArduino(v.getContext(), SetupActivity.DEVICE_ADDRESS, 'r', LED_OFF);
			         	isRedLEDOn = false;
					} else {
			         	((Button)v).setText("Turn Red Off");
			         	Amarino.sendDataToArduino(v.getContext(), SetupActivity.DEVICE_ADDRESS, 'r', LED_ON);
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
			         	((Button)v).setText("Turn Yellow On");
			         	Amarino.sendDataToArduino(v.getContext(), SetupActivity.DEVICE_ADDRESS, 'y', LED_OFF);
			         	isYellowLEDOn = false;
					} else {
			         	((Button)v).setText("Turn Yellow Off");
			         	Amarino.sendDataToArduino(v.getContext(), SetupActivity.DEVICE_ADDRESS, 'y', LED_ON);
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
						((Button)v).setText("Turn Green On");
		                Amarino.sendDataToArduino(v.getContext(), SetupActivity.DEVICE_ADDRESS, 'g', LED_OFF);
		                isGreenLEDOn = false;
					} else {
			         	((Button)v).setText("Turn Green Off");
			         	Amarino.sendDataToArduino(v.getContext(), SetupActivity.DEVICE_ADDRESS, 'g', LED_ON);
			         	isGreenLEDOn = true;
					}
				}
    		}
    	);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Amarino.disconnect(this, SetupActivity.DEVICE_ADDRESS);
	}
}