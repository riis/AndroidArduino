package com.riis.androidarduino.rs232.test;

import com.riis.androidarduino.lib.UsbComm;
import com.riis.androidarduino.rs232.MainActivity;

import android.test.ActivityInstrumentationTestCase2;

public class UnitTests extends ActivityInstrumentationTestCase2<MainActivity> {
	
	private UsbComm usbComm;
	
	public UnitTests() throws ClassNotFoundException {		
		super("com.riis.androidarduino.rs232", MainActivity.class);
	}

	protected void setUp() throws Exception {  
		super.setUp();
		
		usbComm = getActivity().getUsbComm();
		
		while(!usbComm.isConnected()) {
			this.wait(100);
		}
	}
	
	public void testTest() throws SecurityException, NoSuchFieldException {
		assertTrue(usbComm.isConnected());
	}
	
	@Override
	public void tearDown() throws Exception {

	}
}
