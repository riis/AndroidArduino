package com.riis.androidarduino.rs232;

import com.riis.androidarduino.rs232.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private EditText msgBox;
	private Button sendMsgButton;
	
	UsbCommWrapper usbHost;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        usbHost = new UsbCommWrapper(this);
        
        setUpGUI();
    }
    
    private void setUpGUI() {
    	setupMsgBox();
    	setupSendMsgButton();
	}
    
    private void setupMsgBox() {
    	msgBox = (EditText) findViewById(R.id.messageBox);
    }
    
    private void setupSendMsgButton() {
    	sendMsgButton = (Button) findViewById(R.id.messageLabel);
    	sendMsgButton.setOnClickListener(
    	    		new OnClickListener(){
    					public void onClick(View v) {
    						usbHost.sendString(msgBox.getText().toString());
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