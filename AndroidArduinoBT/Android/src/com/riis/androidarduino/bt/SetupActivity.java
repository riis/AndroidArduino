package com.riis.androidarduino.bt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import at.abraxas.amarino.Amarino;

public class SetupActivity extends Activity implements OnClickListener{
	public static final String TAG = "AndroidArduinoBT";
    
    public static String DEVICE_ADDRESS;
    
    EditText idField;
    Button connectButton;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.setup);
	    
	    Log.d(TAG, "Main onStart");
	    
	    // get references to views defined in our main.xml layout file
	    idField = (EditText) findViewById(R.id.deviceIDField);
	    connectButton = (Button) findViewById(R.id.connectButton);
	    // register listeners
	    connectButton.setOnClickListener(this);
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    DEVICE_ADDRESS = prefs.getString("device", "00:18:E4:0C:68:02");
	    idField.setText(DEVICE_ADDRESS);
	}


    public void onClick(View v) {
        DEVICE_ADDRESS = idField.getText().toString();
        PreferenceManager.getDefaultSharedPreferences(this).edit()
        		.putString("device", DEVICE_ADDRESS).commit();
        Amarino.connect(this, DEVICE_ADDRESS);
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    
}
