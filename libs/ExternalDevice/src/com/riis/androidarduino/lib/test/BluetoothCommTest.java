package com.riis.androidarduino.lib.test;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.riis.androidarduino.lib.BluetoothComm;
import com.riis.androidarduino.lib.SerialComm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BluetoothComm.class, SerialComm.class })
public class BluetoothCommTest {

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
