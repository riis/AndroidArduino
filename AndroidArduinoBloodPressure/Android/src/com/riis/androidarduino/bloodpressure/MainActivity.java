package com.riis.androidarduino.bloodpressure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
    private Button addUserButton;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setUpGUI();
    }
    
    private void setUpGUI() {
    	setupAddUserButton();
    }
    
    void setupAddUserButton() {
    	addUserButton = (Button) findViewById(R.id.addUserButton);
    	addUserButton.setOnClickListener(
    		new OnClickListener() {
				public void onClick(View v) {
					Intent myIntent = new Intent(getApplicationContext(), AddUserActivity.class);
					startActivity(myIntent);
				}
			}
		);
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