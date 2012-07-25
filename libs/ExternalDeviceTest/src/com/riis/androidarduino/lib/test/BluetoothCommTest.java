package com.riis.androidarduino.lib.test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import com.riis.androidarduino.lib.BluetoothComm;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BluetoothComm.class, BluetoothAdapter.class, Activity.class, Toast.class })
public class BluetoothCommTest {

	@Test
	public void testFindDevice() throws Exception {
		//Create the mock activity, override the appropriate functions
		Activity mockActivity = PowerMockito.mock(Activity.class);
		PowerMockito.when(mockActivity.getApplicationContext()).thenReturn(null);
		
		//Mock Toast so that it doesn't do anything
		mockStatic(Toast.class);
		Toast toastMock = createMock(Toast.class);
		expect(Toast.makeText(null, "Couldn't find any Bluetooth devices.", 0)).andReturn(toastMock);
		
		//Create the mock BluetoothAdapter and add devices for it to return
		BluetoothAdapter mockAdapter = createMock(BluetoothAdapter.class);
		Set<BluetoothDevice> btDeviceSet = new LinkedHashSet<BluetoothDevice>();
		expect(mockAdapter.getBondedDevices()).andReturn(btDeviceSet);

		//Mock the BluetoothAdapter's static method to return our mock adapter
		mockStatic(BluetoothAdapter.class);
		expect(BluetoothAdapter.getDefaultAdapter()).andReturn(mockAdapter);
		
		//Set up for the testing action
		replay(mockAdapter);
		replay(BluetoothAdapter.class);
		replay(Toast.class);
		
		//Run the actual test action: invoking findDevice
		BluetoothComm btComm = new BluetoothComm(mockActivity, "AndroidArduinoBTRS232");
		Whitebox.invokeMethod(btComm, "findDevice", "AndroidArduinoBTRS232");
		
		//Finish the test
		verify(Toast.class);
		verify(mockAdapter);
		verify(BluetoothAdapter.class);		
		assertTrue(true);
	}
	
	@Test
	public void testBluetoothComm() {
		fail("Not yet implemented");
	}

	@Test
	public void testConnect() {
		fail("Not yet implemented");
	}

	@Test
	public void testDisconnect() {
		fail("Not yet implemented");
	}

	@Test
	public void testPauseConnection() {
		fail("Not yet implemented");
	}

	@Test
	public void testResumeConnection() {
		fail("Not yet implemented");
	}

	@Test
	public void testSendString() {
		fail("Not yet implemented");
	}

	@Test
	public void testSendByteWithFlag() {
		fail("Not yet implemented");
	}

	@Test
	public void testWrite() {
		fail("Not yet implemented");
	}

	@Test
	public void testClearMessages() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsMessageReady() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadMessageWithFlags() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadMessage() {
		fail("Not yet implemented");
	}

	@Test
	public void testRead() {
		fail("Not yet implemented");
	}

	@Test
	public void testShouldPrintLogMsgs() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsConnected() {
		fail("Not yet implemented");
	}

}
