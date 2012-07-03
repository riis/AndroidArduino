package com.riis.androidarduino.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class SerialComm implements Communication, Runnable {
	protected boolean shouldLog;
	
	protected Queue<FlagMsg> inputBuffer;
	
	protected InputStream inputStream;
	protected OutputStream outputStream;
	
	protected Context context;
	protected Handler handler;
	
	protected boolean isConnected;
	
	public SerialComm(Activity parentActivity) {
		this.context = parentActivity.getApplicationContext();
		
		shouldLog = false;
		isConnected = false;
		
		inputBuffer = new LinkedList<FlagMsg>();
		
		setupHandler();
		
		Thread inputThread = new Thread(this);
		inputThread.start();
	}
	
	protected void setupHandler() {
		handler = new Handler() {
			public void handleMessage(Message msg) {
				FlagMsg message = (FlagMsg) msg.obj;
				inputBuffer.add(message);
				
				log("Message received: " + message.getFlag() + " " + message.getValue());
			}
		};
	}
	
	public void sendString(String str) {
		byte[] messageBytes = Util.stringToByteArray(str);
		for(int i = 0; i < messageBytes.length; ++i) {
			sendByteWithFlag('S', messageBytes[i]);
		}
		//Send the null terminator to signify the end of the string.
		sendByteWithFlag('N', (byte) 0);
	}
	
	public void sendByteWithFlag(char flag, byte value) {
		log("Sending byte '" + value + "'  with flag '" + flag + "'.");
		
		sendByte((byte) flag);
		sendByte(value);
	}
	
	public void sendByte(byte value) {
		byte buffer[] = new byte[1];
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
	
	public boolean hasNewMessages() {
		return !inputBuffer.isEmpty();
	}
	
	public FlagMsg readMessage() throws NoSuchElementException {
		try {
			return inputBuffer.remove();
		} catch (NoSuchElementException e) {
			throw e;
		}
	}
	
	protected void checkAndHandleMessages(byte[] buffer) throws IOException {
		if(inputStream == null) {
			return;
		}
		
		int msgLen = 0;
		msgLen = inputStream.read(buffer);

		for(int i = 0; i < msgLen; i += 2) {
			int len = msgLen - i;
			if (len >= 2) {
				Message msg = Message.obtain(handler);
				msg.obj = new FlagMsg((char)buffer[0], buffer[1]);
				handler.sendMessage(msg);
			}
		}
	}
	
	public void shouldPrintLogMsgs(boolean shouldLog) {
		this.shouldLog = shouldLog;
	}
	
	public boolean isConnected() {
		return isConnected;
	}

	protected void log(String string) {
		if(shouldLog) {
			Log.v("SerialComm", string);
		}
	}
}
