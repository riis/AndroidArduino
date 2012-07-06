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
	
	protected LinkedBlockingQueue<Byte> inputBuffer;
	protected LinkedBlockingQueue<FlagMsg[]> msgBuffer;
	
	protected InputStream inputStream;
	protected OutputStream outputStream;
	
	protected boolean foundNullTerminatorFlag;
	
	protected Context context;
	
	protected boolean isConnected;
	
	public SerialComm(Activity parentActivity) {
		this.context = parentActivity.getApplicationContext();
		
		shouldLog = false;
		isConnected = false;
		
		foundNullTerminatorFlag = false;
		
		inputBuffer = new LinkedBlockingQueue<Byte>();
		msgBuffer = new LinkedBlockingQueue<FlagMsg[]>();
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
	
	public boolean isMessageReady() {
		return !msgBuffer.isEmpty();
	}
	
	public FlagMsg[] readMessageWithFlags() {
		if(isMessageReady()) {
			return msgBuffer.poll();
		}
		else
			return null;
	}
	
	public String readMessage() {
		String message = "";
		if(isMessageReady()) {
			FlagMsg[] msgArray = msgBuffer.poll();
			for(int i = 0; i < msgArray.length; i++) {
				if(msgArray[i].getFlag() == Flags.STRING)
					message += (char)msgArray[i].getValue();
			}
			return message;
		}
		else
			return null;
	}
	
	protected void checkAndHandleMessages(byte[] buffer) {
		int msgLen = 0;
		try {
			msgLen = inputStream.read(buffer);
		} catch (IOException e) {
			isConnected = false;
			log("InputStream error");
			return;
		} catch (NullPointerException e) {
			isConnected = false;
			return;
		}
		
		log("Message start");
		for(int i = 0; i < msgLen; i++) {
			log("byte in: " + buffer[i] + " " + (char)buffer[i]);
		}
		log("Message end");

		for(int i = 0; i < msgLen; i++) {
			try {
				inputBuffer.put(buffer[i]);
				if((char)buffer[i] == 'N')
					foundNullTerminatorFlag = true;
				if(foundNullTerminatorFlag && buffer[i] == 0)
					storeMsg();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void storeMsg() {
		try {
			msgBuffer.put(makeFlagMsgArrayFromByteArray());
			foundNullTerminatorFlag = false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private FlagMsg[] makeFlagMsgArrayFromByteArray() {
		FlagMsg[] msgs = new FlagMsg[inputBuffer.size()/2];
		
		boolean hasFlag = false;
		char flag = '\0';
		byte data = 0;
		
		int i = 0;
		while(!inputBuffer.isEmpty()) {
			if(!hasFlag) {
				flag = (char)inputBuffer.poll().byteValue();
				if(isFlag(flag)) {
					hasFlag = true;
				}
			} else {
				data = inputBuffer.poll().byteValue();
				msgs[i] = new FlagMsg(flag, data);
				i++;
				hasFlag = false;
			}
		}
		
		return msgs;
	}
	
	private boolean isFlag(char flag) {
		for(int i = 0; i < Flags.FLAG_VALUES.length; i++) {
			if(Flags.FLAG_VALUES[i] == flag)
				return true;
		}
		return false;
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