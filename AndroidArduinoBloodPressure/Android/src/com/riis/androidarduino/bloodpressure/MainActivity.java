package com.riis.androidarduino.bloodpressure;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setUpGUI();
    }
    
    private void setUpGUI() {
    	
    }
    
    @Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
}