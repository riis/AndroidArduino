package com.riis.androidarduino.lib;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothComm extends SerialComm implements Runnable {
	public static UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private BluetoothAdapter adapter;
	private BluetoothDevice device;
	private BluetoothSocket socket;
	private String deviceName;
	
	private Thread inputThread;
	
	public BluetoothComm(String deviceName) {
		super();
		
		this.deviceName = deviceName;
		
		inputThread = new Thread(this, "BluetoothComm");
		inputThread.start();
	}
	
	public void connect() throws IOException {
		try {
			device = findDevice(deviceName);
			connectSocket();
		} catch(Exception e) {
			throw new IOException("Could not connect to device.");
		}
		
		if(socket != null) {
			isConnected = true;
			log("Connected!");
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
		    		log("Found device " + deviceName);
			    	return searchDevice;
		    	}
		    }
		}
		log("Couldn't find any Bluetooth devices.");
		throw new NullPointerException();
	}
	
	private void connectSocket() throws IOException {
		try {
			socket = device.createRfcommSocketToServiceRecord(uuid);			
			socket.connect();
		
			setInputStream(socket.getInputStream());								
			setOutputStream(socket.getOutputStream());		
		} catch (IOException e) {
			throw e;
		}
	}
	
	public void disconnect() throws IOException {
		byte[] terminateConnectonSignal = {'\r', '\n', '+', 'B', 'T', 'S', 'T', 'A', 'T', 'E', ':', '1', '\n', '\r'};
		
		write(terminateConnectonSignal);
		
		try {
			if(socket != null)
				socket.close();
			if(getInputStream() != null)
				getInputStream().close();
			if(getOutputStream() != null)
				getOutputStream().close();
		} catch (IOException e) {
			isConnected = false;
			throw e;
		}
		
		isConnected = false;
		log("Disconnected!");
	}

	public void pauseConnection() throws IOException {
		disconnect();
	}

	public void resumeConnection() throws IOException {
		connect();
	}
}
