package com.riis.androidarduino.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public abstract class SerialComm implements Communication, Runnable {
	protected boolean shouldLog;
	
	protected LinkedBlockingQueue<FlagMsg> inputBuffer;
	
	protected InputStream inputStream;
	protected OutputStream outputStream;
	
	protected Context context;
	
	protected boolean isConnected;
	
	public SerialComm(Activity parentActivity) {
		this.context = parentActivity.getApplicationContext();
		
		shouldLog = false;
		isConnected = false;
		
		inputBuffer = new LinkedBlockingQueue<FlagMsg>();
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
		if (outputStream != null) {
			try {
				outputStream.write(new byte[] {value});
			} catch (IOException e) {
				isConnected = false;
				log("Send failed: " + e.getMessage());
			}
		}
		else {
			isConnected = false;
			log("Send failed: outputStream was null");
		}
	}
	
	public void clearMessages() {
		inputBuffer.clear();
	}
	
	public boolean hasNewMessages() {
		return !inputBuffer.isEmpty();
	}
	
	public FlagMsg readMessage() {
		return inputBuffer.poll();
	}
	
	public FlagMsg peekAtMessage() {
		return inputBuffer.peek();
	}
	
	protected void checkAndHandleMessages(byte[] buffer) {
		int msgLen = 0;
		try {
			msgLen = inputStream.read(buffer);
		} catch (IOException e) {
			isConnected = false;
			log("InputStream error");
			return;
		}

		for(int i = 0; i < msgLen; i += 2) {
			int len = msgLen - i;
			if(len >= 2) {
				try {
					inputBuffer.put(new FlagMsg((char)buffer[i], buffer[i+1]));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
