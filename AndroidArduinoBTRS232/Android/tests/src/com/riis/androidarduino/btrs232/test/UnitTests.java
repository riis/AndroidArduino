package com.riis.androidarduino.btrs232.test;

import com.riis.androidarduino.lib.BluetoothComm;
import com.riis.androidarduino.lib.FlagMsg;
import com.riis.androidarduino.btrs232.MainActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class UnitTests extends ActivityInstrumentationTestCase2<MainActivity> {
	
	private static BluetoothComm btComm;
	
	public UnitTests() throws ClassNotFoundException {		
		super("com.riis.androidarduino.btrs232", MainActivity.class);
	}
	
	protected void setUp() throws Exception {  
		super.setUp();
		
		if(btComm == null || !btComm.isConnected()) {
			btComm = getActivity().getBlueToothComm();
			btComm.shouldPrintLogMsgs(true);
			
			while(!btComm.isConnected()) {

			}
		}
	}
	
	public void testBlueToothConnected() {
		assertTrue(btComm.isConnected());
	}
	
	@Override
	public void tearDown() throws Exception {

	}
}
