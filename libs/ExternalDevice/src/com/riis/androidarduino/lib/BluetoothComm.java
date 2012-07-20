package com.riis.androidarduino.lib;

import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

public class BluetoothComm extends SerialComm implements Runnable {
	private BluetoothAdapter adapter;
	private BluetoothDevice device;
	private BluetoothSocket socket;
	private String deviceName;
	
	public BluetoothComm(Activity parentActivity, String deviceName) {
		super(parentActivity);
		this.deviceName = deviceName;
		connect();
		
		Thread inputThread = new Thread(this, "BlueToothComm");
		inputThread.start();
	}
	
//	@Override
	public void connect() {
		device = findDevice(deviceName);
		connectSocket();
		
		if(socket != null) {
			isConnected = true;
			Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();
		}
	}
	
	private BluetoothDevice findDevice(String deviceName) {
    	adapter = BluetoothAdapter.getDefaultAdapter();
    	Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

    	if(deviceName.length() > 20) {
    		deviceName = deviceName.substring(0, 20);
    	}
    	
    	if(pairedDevices.size() > 0) {
		    for (BluetoothDevice searchDevice : pairedDevices) {
		    	if(searchDevice.getName().equals(deviceName)) {
		    		Toast.makeText(context, "Found device " + deviceName, Toast.LENGTH_SHORT).show();
			    	return searchDevice;
		    	}
		    }
		}
		Toast.makeText(context, "Couldn't find any Bluetooth devices.", Toast.LENGTH_SHORT).show();
		return null;
	}
	
	private void connectSocket() {
		try {
			socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));			
			socket.connect();
		
			setInputStream(socket.getInputStream());								
			setOutputStream(socket.getOutputStream());		
		} catch (Exception e) {
			Toast.makeText(context, "Couldn't connect to device", Toast.LENGTH_SHORT).show();
			return ;
		}
	}
	
	public void disconnect() {
		try {
			if(socket != null)
				socket.close();
			if(getInputStream() != null)
				getInputStream().close();
			if(getOutputStream() != null)
				getOutputStream().close();
		} catch (Exception e) { }
		
		isConnected = false;
		Toast.makeText(context, "Disconnected!", Toast.LENGTH_SHORT).show();
	}

//	@Override
	public void pauseConnection() {
		disconnect();
	}

//	@Override
	public void resumeConnection() {
		connect();
	}
}
