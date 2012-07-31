package com.riis.androidarduino.lib.test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.riis.androidarduino.lib.BluetoothComm;
import com.riis.androidarduino.lib.FlagMsg;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BluetoothSocket.class, BluetoothDevice.class, BluetoothComm.class, BluetoothAdapter.class, Activity.class })
public class BluetoothCommTest {
	
	private static final String FULL_DEVICE_NAME = "AndroidArduinoBTRS232";
	private static final String TRUNCATED_DEVICE_NAME = "AndroidArduinoBTRS23";
	
	private BluetoothAdapter mockAdapter;
	private BluetoothSocket mockSocket;
	private ByteArrayInputStream mockInputStream;
	private ByteArrayOutputStream mockOutputStream;
	private BluetoothComm bluetoothComm;
	
	@Before
	public void setup() {
		initializeStreams();
		mockSocket = createMockSocket();
		
		//Create the mock adapter and attach a set of mock devices to it
		mockAdapter = createMock(BluetoothAdapter.class);
		Set<BluetoothDevice> mockDeviceSet = createMockDeviceSet();
		expect(mockAdapter.getBondedDevices()).andReturn(mockDeviceSet);
		
		//Mock BluetoothAdapter's static method to return our mock adapter
		mockStatic(BluetoothAdapter.class);
		expect(BluetoothAdapter.getDefaultAdapter()).andReturn(mockAdapter);
		
		//Create the BluetoothComm
		bluetoothComm = new BluetoothComm(FULL_DEVICE_NAME);
		
		//Get ready for testing
		replay(mockAdapter);
		replay(BluetoothAdapter.class);
	}

	private void initializeStreams() {
		mockInputStream = new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255});
		mockOutputStream = new ByteArrayOutputStream();
	}
	
	private BluetoothSocket createMockSocket() {
		BluetoothSocket mockSocket = PowerMockito.mock(BluetoothSocket.class);
		
		try {
			PowerMockito.doNothing().when(mockSocket).connect();
			PowerMockito.when(mockSocket.getInputStream()).thenReturn(mockInputStream);
			PowerMockito.when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);
		} catch (IOException e) { }
		
		return mockSocket;
	}

	private Set<BluetoothDevice> createMockDeviceSet() {
		Set<BluetoothDevice> btDeviceSet = new LinkedHashSet<BluetoothDevice>();
		
		btDeviceSet.add(createMockDevice(TRUNCATED_DEVICE_NAME));
		btDeviceSet.add(createMockDevice("Not a real device"));
		
		return btDeviceSet;
	}

	private BluetoothDevice createMockDevice(String deviceName) {
		BluetoothDevice btDevice = PowerMockito.mock(BluetoothDevice.class);
		
		PowerMockito.when(btDevice.getName()).thenReturn(deviceName);
		try {
			PowerMockito.when(btDevice.createRfcommSocketToServiceRecord(BluetoothComm.uuid)).thenReturn(mockSocket);
		} catch (IOException e) { }
		
		return btDevice;
	}
	


	
	@Test
	public void testFindDevice() throws Exception {
		//Run the actual test action: invoking findDevice
		BluetoothDevice foundDevice = Whitebox.invokeMethod(bluetoothComm, "findDevice", FULL_DEVICE_NAME);
		
		//Finish the test
		assertTrue(foundDevice.getName().equals(TRUNCATED_DEVICE_NAME));
	}
	
	@Test
	public void testBluetoothComm() {
		//Test that that device name is saved and thread gets started
		assertEquals(Whitebox.getInternalState(bluetoothComm, "deviceName"), FULL_DEVICE_NAME);
		assertTrue(((Thread)Whitebox.getInternalState(bluetoothComm, "inputThread")).isAlive());
	}

	@Test
	public void testConnect() throws IOException {
		bluetoothComm.connect();
		
		//Test that BluetoothComm thinks it's connected, and that the device is correct
		assertTrue(bluetoothComm.isConnected());
		String deviceName = ((BluetoothDevice)Whitebox.getInternalState(bluetoothComm, "device")).getName();
		assertEquals(deviceName, TRUNCATED_DEVICE_NAME);
	}

	@Test
	public void testDisconnect() throws Exception {
		bluetoothComm.connect();
		assertTrue(bluetoothComm.isConnected());
		bluetoothComm.disconnect();
		
		//Finish the test
		assertTrue(!bluetoothComm.isConnected());
		assertTrue(!mockSocket.isConnected());
	}

	@Test
	public void testPauseConnection() throws IOException {
		bluetoothComm.connect();
		bluetoothComm.pauseConnection();
		
		assertTrue(!bluetoothComm.isConnected());
		assertTrue(!mockSocket.isConnected());
	}

	@Test
	public void testResumeConnection() throws IOException {
		bluetoothComm.resumeConnection();
		
		assertTrue(bluetoothComm.isConnected());
		String deviceName = ((BluetoothDevice)Whitebox.getInternalState(bluetoothComm, "device")).getName();
		assertEquals(deviceName, TRUNCATED_DEVICE_NAME);
	}

	@Test
	public void testSendString() throws IOException {
		bluetoothComm.connect();
		bluetoothComm.sendString("hello");
		
		String sentData = new String(mockOutputStream.toByteArray());
		
		assertEquals(sentData, "ShSeSlSlSoN" + (char)255);
	}

	@Test
	public void testSendByteWithFlag() throws IOException {
		bluetoothComm.connect();
		bluetoothComm.sendByteWithFlag('S', (byte)97);
		
		String sentData = new String(mockOutputStream.toByteArray());
		
		assertEquals(sentData, "Sa");
	}

	@Test
	public void testWrite() throws IOException {
		bluetoothComm.connect();
		bluetoothComm.write(new byte[] {97, 97});
		
		String sentData = new String(mockOutputStream.toByteArray());
		
		assertEquals(sentData, "aa");
	}

	@Test
	public void testClearMessages() throws Exception {
		bluetoothComm.connect();
		Whitebox.invokeMethod(bluetoothComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		while(!bluetoothComm.isMessageReady());
		bluetoothComm.clearMessages();
		
		assertTrue(!bluetoothComm.isMessageReady());
	}

	@Test
	public void testIsMessageReady() throws Exception {
		bluetoothComm.connect();
		Whitebox.invokeMethod(bluetoothComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		while(!bluetoothComm.isMessageReady());
		
		assertTrue(bluetoothComm.isMessageReady());
	}

	@Test
	public void testReadMessageWithFlags() throws Exception {
		bluetoothComm.connect();
		Whitebox.invokeMethod(bluetoothComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		while(!bluetoothComm.isMessageReady());
		
		//Get the message and convert the FlagMsg array to an array of bytes
		FlagMsg[] sentMessage = bluetoothComm.readMessageWithFlags();
		byte[] sentMessageBytes = new byte[sentMessage.length * 2];
		
		for(int i = 0; i < sentMessage.length; i++) {
			sentMessageBytes[i * 2] = (byte) sentMessage[i].getFlag();
			sentMessageBytes[(i * 2) + 1] = (byte) sentMessage[i].getValue();
		}
		
		//Make a string out of it, then test its contents
		String sentMessageString = new String(sentMessageBytes);
		
		assertEquals(sentMessageString, "SaSbN" + (char)255);
	}

	@Test
	public void testReadMessage() throws Exception {
		bluetoothComm.connect();
		Whitebox.invokeMethod(bluetoothComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		while(!bluetoothComm.isMessageReady());
		
		assertEquals(bluetoothComm.readMessage(), "ab");
	}

	@Test
	public void testRead() throws Exception {
		bluetoothComm.connect();
		Whitebox.invokeMethod(bluetoothComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		byte[] sentMessageBytes = new byte[6];
		bluetoothComm.read(sentMessageBytes);
		String sentMessageString = new String(sentMessageBytes);
		
		assertEquals(sentMessageString, "SaSbN" + (char)255);
	}

	@Test
	public void testIsConnected() throws Exception {
		bluetoothComm.connect();
		Whitebox.invokeMethod(bluetoothComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		assertTrue(bluetoothComm.isConnected());
	}

}
