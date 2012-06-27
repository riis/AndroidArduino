package com.riis.androidarduino.usb;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class UsbCommWrapper implements Runnable {
	public static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
	
	private BroadcastReceiver usbBroadcastReceiver;
	private UsbManager manager;
	private UsbAccessory accessory;
	private ParcelFileDescriptor fileDescriptor;
	private FileInputStream inputStream;
	private FileOutputStream outputStream;
	private PendingIntent permissionIntent;
	private boolean permissionRequestPending;
	private Activity parentActivity;
	
	Handler handler;
	
	public UsbCommWrapper(Activity parentActivity) {
		this.parentActivity = parentActivity;
		setupHandler();
		setupBroadcastReceiver();
		setupAccessory();
	}
	
	private void setupHandler() {
		handler = new Handler() {
			public void handleMessage(Message msg) {
				ValueMsg t = (ValueMsg) msg.obj;
				log("Usb Accessory sent: " + t.getFlag() + " " + t.getReading());
			}
		};
	}
	
	private void setupBroadcastReceiver() {
		usbBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (ACTION_USB_PERMISSION.equals(action)) {
					synchronized (this) {
						UsbAccessory accessory = UsbManager.getAccessory(intent);
						if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
							openAccessory(accessory);
						} else {
							log("USB permission denied");
						}
					}
				} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (accessory != null && accessory.equals(accessory)) {
						log("Detached");
						closeAccessory();
					}
				}
			}
		};
	}
	
	private void setupAccessory() {
		log("Setting up accessory");
		manager = UsbManager.getInstance(parentActivity);
		permissionIntent = PendingIntent.getBroadcast(parentActivity, 0, new Intent(ACTION_USB_PERMISSION), 0);
		registerReceiver();
		
		UsbAccessory acc = (UsbAccessory) parentActivity.getLastNonConfigurationInstance();
		if (acc != null) {
			accessory = acc;
			openAccessory(accessory);
		}
    }
	
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		parentActivity.registerReceiver(usbBroadcastReceiver, filter);
	}
	
	private void openAccessory(UsbAccessory accessory) {
		fileDescriptor = manager.openAccessory(accessory);
		if (fileDescriptor != null) {
			this.accessory = accessory;
			
			FileDescriptor fd = fileDescriptor.getFileDescriptor();
			inputStream = new FileInputStream(fd);
			outputStream = new FileOutputStream(fd);
			
			Thread thread = new Thread(null, this, "UsbCommWrapperLoop");
			thread.start();
			
			alert("openAccessory: Accessory opened");
			log("Accessory attached");
		} else {
			log("Accessory open failed");
		}
	}
	
	private void closeAccessory() {
		try {
			if (fileDescriptor != null) {
				fileDescriptor.close();
				log("Dettached");
			}
		} catch (IOException e) {
		} finally {
			fileDescriptor = null;
			accessory = null;
		}
	}
	
	public void pauseConnection() {
		unregisterReceiver();
	}
	
	public void resumeConnection() {
		registerReceiver();
		
		if (inputStream != null && outputStream != null) {
			log("Resuming: streams were not null");
			return;
		}
		log("Resuming: streams were null");
		
		UsbAccessory[] accessories = manager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		
		if (accessory != null) {
			if (manager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (usbBroadcastReceiver) {
					if (!permissionRequestPending) {
						manager.requestPermission(accessory, permissionIntent);
						permissionRequestPending = true;
					}
				}
			}
		} else {
			log("Resuming: accessory is null");
		}
	}
	
	public void run() {
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;

		while(true) { //Read messages forever
			try {
				ret = inputStream.read(buffer);
			} catch (IOException e) {
				break;
			}

			i = 0;
			while (i < ret) {
				int len = ret - i;
				if (len >= 2) {
					Message m = Message.obtain(handler);
					int value = composeInt(buffer[i], buffer[i + 1]);
					m.obj = new ValueMsg('a', value);
					handler.sendMessage(m);
				}
				i += 2;
			}

		}
	}
	
	public void sendByte(byte msg) {
		log("Sending byte '" + msg + "' to Usb Accessory");
		
		byte[] buffer = new byte[1];
		buffer[0] = msg;
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
	
	public void unregisterReceiver() {
		parentActivity.unregisterReceiver(usbBroadcastReceiver);
	}
	
	public UsbAccessory getAccessory() {
		return accessory;
	}
	
	private void alert(String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(parentActivity).create();
	    alertDialog.setTitle("Alert");
	    alertDialog.setMessage(message);
	    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		return;
	    	} 
	    }); 
	    alertDialog.show();
	}

	private int composeInt(byte hi, byte lo) {
		int val = (int) hi & 0xff;
		val *= 256;
		val += (int) lo & 0xff;
		return val;
	}
	
	private void log(String string) {
		Log.v("UsbCommWrapper", string);
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
