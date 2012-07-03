package com.riis.androidarduino.btrs232.test;

import com.riis.androidarduino.lib.BlueToothComm;
import com.riis.androidarduino.lib.FlagMsg;
import com.riis.androidarduino.btrs232.MainActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class UnitTests extends ActivityInstrumentationTestCase2<MainActivity> {
	
	private static BlueToothComm btComm;
	
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
			
			btComm.sendByteWithFlag('T', (byte) 1);
		}
	}
	
	public void testBlueToothConnected() {
		assertTrue(btComm.isConnected());
	}
	
	public void testArduinoInTestMode() throws InterruptedException {
		while(!btComm.hasNewMessages()) {}
		
		if(btComm.hasNewMessages()) {
			FlagMsg msg = btComm.readMessage();
			Log.v("BTRS232 Test", "Message info: " + (byte)msg.getFlag() + ", " + msg.getReading());
			assertTrue(msg.getFlag() == 'T');
			assertTrue(msg.getReading() == 0);
		} else {		
			assertTrue(false);
		}
	}
	
	@Override
	public void tearDown() throws Exception {

	}
}
