package com.riis.androidarduino.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BlueToothComm implements Runnable {
	private BluetoothAdapter adapter;
	private BluetoothDevice device;
	private BluetoothSocket socket;
	
	private InputStream inputStream;
	private OutputStream outputStream;
	
	private Context context;
	Handler handler;
	
	public BlueToothComm(Context context, String deviceName) {
		this.context = context;
		
		setupHandler();
		connect(deviceName);
	}
	
	private void setupHandler() {
		handler = new Handler() {
			public void handleMessage(Message msg) {
				ValueMsg t = (ValueMsg) msg.obj;
				Log.v("AndroidArduinoBT", "Bluetooth message received: " + t.getFlag() + " " + t.getReading());
			}
		};
	}
	
	public void connect(String deviceName) {
		findDevice(deviceName);
		connectSocket();
		
		Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();
	}
	
	private void findDevice(String deviceName) {
		boolean gotDevice = false;
    	
    	adapter = BluetoothAdapter.getDefaultAdapter();
    	if(adapter == null) {
    		Toast.makeText(context, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

    	if (pairedDevices.size() > 0) {
		    for (BluetoothDevice searchDevice : pairedDevices) {
		    	if(searchDevice.getName().equals(deviceName)) {
		    		device = searchDevice;
		    		gotDevice = true;
		    		break;
		    	}
		    }
		    
		    if(gotDevice) {
		    	Toast.makeText(context, "Found device " + deviceName, Toast.LENGTH_SHORT).show();
		    } else {
	    		Toast.makeText(context, "Couldn't find device, is it paired?", Toast.LENGTH_SHORT).show();
		    }
		}
	}
	
	private void connectSocket() {
		try {
			socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));			
			socket.connect();
		
			inputStream = socket.getInputStream();														
			outputStream = socket.getOutputStream();		
		} catch (Exception e) {
			Toast.makeText(context, "Couldn't connect to device", Toast.LENGTH_SHORT).show();
			return ;
		}
	}
	
	public void disconnect() {
		try {
			socket.close();
			inputStream.close();
			outputStream.close();
		} catch (Exception e) { }
		
		Toast.makeText(context, "Disconnected!", Toast.LENGTH_SHORT).show();
	}
	
	public void sendString(String str) {
		byte[] messageBytes = stringToByteArray(str);
		for(int i = 0; i < messageBytes.length; ++i) {
			sendByte(messageBytes[i]);
		}
		
		//Send the null terminator to signify the end of the string.
		sendByte((byte) 0);
	}
	
	private byte[] stringToByteArray(String str) {
		char[] buffer = str.toCharArray();
		byte[] b = new byte[buffer.length];
				
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) buffer[i];
		}
		return b;
	}
	
	public void sendByte(byte msg) {
		try {
			outputStream.write(new byte[] {(byte)msg});
		} catch (Exception e) {
			Toast.makeText(context, "Send failed!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void run() {
		int msgLen = 0;
		byte[] buffer = new byte[256];
		int i;

		while (true) { // keep reading messages forever.
			try {
				msgLen = inputStream.read(buffer);
			} catch (IOException e) {
				break;
			}

			i = 0;
			while (i < msgLen) {
				int len = msgLen - i;
				if (len >= 2) {
					Message m = Message.obtain(handler);
					int value = composeInt(buffer[i], buffer[i + 1]);
					m.obj = new ValueMsg('a', value);
					handler.sendMessage(m);
				}
				i += 2;
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {	}
		}
	}
	
	private int composeInt(byte hi, byte lo) {
		int val = (int) hi & 0xff;
		val *= 256;
		val += (int) lo & 0xff;
		return val;
	}
	
	public class ValueMsg {
		private char flag;
		private int reading;

		public ValueMsg(char flag, int reading) {
			this.flag = flag;
			this.reading = reading;
		}

		public int getReading() {
			return reading;
		}
		
		public char getFlag() {
			return flag;
		}
	}
}
