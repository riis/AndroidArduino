package com.riis.androidarduino.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class SerialComm implements Communication, Runnable {
	protected boolean shouldLog;

	protected InputStream inputStream;
	protected OutputStream outputStream;
	
	protected Context context;
	protected Handler handler;
	
	public SerialComm(Activity parentActivity) {
		this.context = parentActivity.getApplicationContext();
		shouldLog = false;
		setupHandler();
	}
	
	protected void setupHandler() {
		handler = new Handler() {
			public void handleMessage(Message msg) {
				ValueMsg t = (ValueMsg) msg.obj;
				log("Usb Accessory sent: " + t.getFlag() + " " + t.getReading());
			}
		};
	}
	
	public void sendString(String str) {
		byte[] messageBytes = Util.stringToByteArray(str);
		for(int i = 0; i < messageBytes.length; ++i) {
			sendByte('S', messageBytes[i]);
		}
		//Send the null terminator to signify the end of the string.
		sendByte('N', (byte) 0);
	}
	
	public void sendByte(char flag, byte value) {
		log("Sending Byte '" + value + "' to Usb Accessory");
		
		byte[] buffer = new byte[1];
		buffer[0] = value;
		if (outputStream != null) {
			try {
				outputStream.write(buffer);
			} catch (IOException e) {
				log("Send failed: " + e.getMessage());
			}
		}
		else {
			log("Send failed: outputStream was null");
		}
	}
	
	
	public void shouldPrintLogMsgs(boolean x) {
		shouldLog = x;
	}
	
	protected void checkAndHandleMessages(byte[] buffer) throws IOException {
		int msgLen = 0;
		msgLen = inputStream.read(buffer);

		for(int i = 0; i < msgLen; i += 2) {
			int len = msgLen - i;
			if (len >= 2) {
				Message m = Message.obtain(handler);
				int value = Util.composeInt(buffer[i], buffer[i + 1]);
				m.obj = new ValueMsg('a', value);
				handler.sendMessage(m);
			}
		}
	}

	protected void log(String string) {
		if(shouldLog)
			Log.v("UsbCommWrapper", string);
	}
}
