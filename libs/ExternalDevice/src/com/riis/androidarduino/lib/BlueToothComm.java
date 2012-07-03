package com.riis.androidarduino.lib;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.Toast;

public class BlueToothComm extends SerialComm implements Runnable {
	private BluetoothAdapter adapter;
	private BluetoothDevice device;
	private BluetoothSocket socket;
	private String deviceName;
	
	public BlueToothComm(Activity parentActivity, String deviceName) {
		super(parentActivity);
		this.deviceName = deviceName;
		connect();
	}
	
	@Override
	public void connect() {
		findDevice(deviceName);
		connectSocket();
		
		if(socket != null) {
			isConnected = true;
			Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void findDevice(String deviceName) {
		boolean gotDevice = false;
    	
    	adapter = BluetoothAdapter.getDefaultAdapter();
    	Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

    	if(deviceName.length() > 20) {
    		deviceName = deviceName.substring(0, 20);
    	}
    	
    	if(pairedDevices.size() > 0) {
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
		
		isConnected = false;
		Toast.makeText(context, "Disconnected!", Toast.LENGTH_SHORT).show();
	}
	
	public Handler getInputHandler() {
		return handler;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[256];

		while (true) { // keep reading messages forever.
			try {
				checkAndHandleMessages(buffer);
			} catch (IOException e) {

			}
		}
	}

	@Override
	public void pauseConnection() {
		disconnect();
		
	}

	@Override
	public void resumeConnection() {
		connect();
	}
}
