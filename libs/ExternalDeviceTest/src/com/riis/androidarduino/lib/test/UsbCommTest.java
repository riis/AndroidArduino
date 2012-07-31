package com.riis.androidarduino.lib.test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.ParcelFileDescriptor;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.riis.androidarduino.lib.FlagMsg;
import com.riis.androidarduino.lib.UsbComm;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Intent.class, PendingIntent.class, FileOutputStream.class, FileInputStream.class, FileDescriptor.class, ParcelFileDescriptor.class, UsbAccessory.class, UsbManager.class, UsbComm.class, Activity.class })
public class UsbCommTest {
	
	private Activity mockActivity;
	private UsbAccessory mockAccessory;
	private FileDescriptor mockFileDescriptor;
	private ParcelFileDescriptor mockParcelFileDescriptor;
	private UsbManager mockManager;
	
	private ByteArrayInputStream mockInputStream;
	private ByteArrayOutputStream mockOutputStream;
	
	private UsbComm usbComm;

	@Before
	public void setup() throws Exception {
		initializeStreams();

		//Mock a UsbAccessory for the activity to get
		mockAccessory = PowerMockito.mock(UsbAccessory.class);
		
		//Mock an activity to return a null context and nonconfiguration instance, and our own accessory
		mockActivity = PowerMockito.mock(Activity.class);
		PowerMockito.when(mockActivity.getApplicationContext()).thenReturn(null);
		PowerMockito.when(mockActivity.getLastNonConfigurationInstance()).thenReturn(mockAccessory);
		
		//Mock a FileDescriptor to be returned
		mockFileDescriptor = PowerMockito.mock(FileDescriptor.class);
		
		//Mock FileInput and OutputStream so their constructors return null
		PowerMockito.whenNew(FileInputStream.class).withArguments(mockFileDescriptor).thenReturn(null);
		PowerMockito.whenNew(FileOutputStream.class).withArguments(mockFileDescriptor).thenReturn(null);
		
		//Mock a ParcelFileDescriptor to return our own FileDescriptor
		mockParcelFileDescriptor = PowerMockito.mock(ParcelFileDescriptor.class);
		PowerMockito.when(mockParcelFileDescriptor.getFileDescriptor()).thenReturn(mockFileDescriptor);
		
		//Mock a UsbManager so it gets our own ParcelFileManager and accessory
		mockManager = PowerMockito.mock(UsbManager.class);
		PowerMockito.when(mockManager.openAccessory(mockAccessory)).thenReturn(mockParcelFileDescriptor);
		PowerMockito.when(mockManager.getAccessoryList()).thenReturn(new UsbAccessory[] {mockAccessory});
		
		//Mock the UsbManager static method to return our own mock manager
		mockStatic(UsbManager.class);
		expect(UsbManager.getInstance(null)).andReturn(mockManager);
		
		//Mock PendingIntent so it doesn't cramp my style
		PowerMockito.whenNew(Intent.class).withArguments(UsbComm.ACTION_USB_PERMISSION).thenReturn(null);
		mockStatic(PendingIntent.class);
		expect(PendingIntent.getBroadcast(null, 0, null, 0)).andReturn(null);
		
		//Suppress a few unnecessary methods in UsbComm
		suppress(method(UsbComm.class, "registerReceiver"));
		suppress(method(UsbComm.class, "unregisterReceiver"));
		suppress(method(UsbComm.class, "setupBroadcastReceiver"));
		
		//Get ready for the tests
		usbComm = new UsbComm(mockActivity);
		
		replay(UsbManager.class);
		replay(PendingIntent.class);
	}
	
	private void initializeStreams() {
		mockInputStream = new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255});
		mockOutputStream = new ByteArrayOutputStream();
	}
	
	//A utility to get our streams in there, since it couldn't be done all the way through mocking
	private void injectStreams() throws Exception {
		Whitebox.invokeMethod(usbComm, "setInputStream", mockInputStream);
		Whitebox.invokeMethod(usbComm, "setOutputStream", mockOutputStream);
	}
	
	
	
	@Test
	public void testUsbComm() {
		assertTrue(Whitebox.getInternalState(usbComm, "context") == null);
		assertEquals(Whitebox.getInternalState(usbComm, "accessory"), mockAccessory);
	}

	//TODO: next 4 tests need some more stuff to them
	@Test
	public void testConnect() throws IOException {
		usbComm.connect();
		
		//Test that UsbComm thinks it's connected and that the thread is running
		assertTrue(usbComm.isConnected());
		assertTrue(((Thread)Whitebox.getInternalState(usbComm, "inputThread")).isAlive());
	}

	@Test
	public void testDisconnect() throws Exception {
//		usbComm.connect();
//		injectStreams();
//		
//		assertTrue(usbComm.isConnected());
//		usbComm.disconnect();
//		
//		//Finish the test
//		assertTrue(!usbComm.isConnected());
	}

	@Test
	public void testPauseConnection() throws IOException {
		usbComm.connect();
		usbComm.pauseConnection();
		
		assertTrue(!usbComm.isConnected());
	}

	@Test
	public void testResumeConnection() throws Exception {
		usbComm.connect();
		injectStreams();
		usbComm.pauseConnection();
		usbComm.resumeConnection();
		
		assertTrue(usbComm.isConnected());
	}

	@Test
	public void testGetAccessory() {
		usbComm.connect();
		assertEquals(usbComm.getAccessory(), mockAccessory);
	}

	@Test
	public void testSendString() throws Exception {
		usbComm.connect();
		injectStreams();
		usbComm.sendString("hello");
		
		String sentData = new String(mockOutputStream.toByteArray());
		
		assertEquals(sentData, "ShSeSlSlSoN" + (char)255);
	}

	@Test
	public void testSendByteWithFlag() throws Exception {
		usbComm.connect();
		injectStreams();
		usbComm.sendByteWithFlag('S', (byte)97);
		
		String sentData = new String(mockOutputStream.toByteArray());
		
		assertEquals(sentData, "Sa");
	}

	@Test
	public void testWrite() throws Exception {
		usbComm.connect();
		injectStreams();
		usbComm.write(new byte[] {97, 97});
		
		String sentData = new String(mockOutputStream.toByteArray());
		
		assertEquals(sentData, "aa");
	}

	@Test
	public void testClearMessages() throws Exception {
		usbComm.connect();
		Whitebox.invokeMethod(usbComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		while(!usbComm.isMessageReady());
		usbComm.clearMessages();
		
		assertTrue(!usbComm.isMessageReady());
	}

	@Test
	public void testIsMessageReady() throws Exception {
		usbComm.connect();
		Whitebox.invokeMethod(usbComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		while(!usbComm.isMessageReady());
		
		assertTrue(usbComm.isMessageReady());
	}

	@Test
	public void testReadMessageWithFlags() throws Exception {
		usbComm.connect();
		Whitebox.invokeMethod(usbComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		while(!usbComm.isMessageReady());
		
		//Get the message and convert the FlagMsg array to an array of bytes
		FlagMsg[] sentMessage = usbComm.readMessageWithFlags();
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
		usbComm.connect();
		Whitebox.invokeMethod(usbComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		while(!usbComm.isMessageReady());
		
		assertEquals(usbComm.readMessage(), "ab");
	}

	@Test
	public void testRead() throws Exception {
		usbComm.connect();
		Whitebox.invokeMethod(usbComm, "setInputStream", new ByteArrayInputStream(new byte[] {'S', 'a', 'S', 'b', 'N', (byte) 255}));
		
		byte[] sentMessageBytes = new byte[6];
		usbComm.read(sentMessageBytes);
		String sentMessageString = new String(sentMessageBytes);
		
		assertEquals(sentMessageString, "SaSbN" + (char)255);
	}

	@Test
	public void testIsConnected() throws Exception {
		usbComm.connect();
		
		assertTrue(usbComm.isConnected());
	}

}
