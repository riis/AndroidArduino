package com.riis.androidarduino.bloodpressure;

import android.app.Activity;
import android.os.Bundle;

public class AddUserActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newuserform);
        
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
