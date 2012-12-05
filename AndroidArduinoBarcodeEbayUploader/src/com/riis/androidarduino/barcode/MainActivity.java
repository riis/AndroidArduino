package com.riis.androidarduino.barcode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.riis.androidarduino.lib.BluetoothComm;

public class MainActivity extends Activity {
	private static String DEVICE_NAME = "AndroidArduinoBT";

	private Button connectButton;
	private Button disconnectButton;

	private ScrollView logScrollContainer;
	private TextView msgLog;

	private LinearLayout scannedContainer;
	private ArrayList<ScannedObject> scanList;

	private volatile boolean keepRunning;
	private boolean lastStatus;
	private Thread msgThread;

	private static Handler handler;
	private Context context;

	private BluetoothComm btComm;

	private AlertDialog ebayDialog;

	private Runnable msgUpdateThread = new Runnable() {
		public void run() {
			while (keepRunning) {
				if (btComm.isConnected()) {
					if (!lastStatus) {
						lastStatus = true;
						appendMsgToMsgLog("Bluetooth Connected!");
					}

					if (btComm.isMessageReady()) {
						String barcode = btComm.readMessage();
						appendMsgToMsgLog("Scanned: " + barcode);
						addItemToScanLog(barcode);
					}

				} else {
					if (lastStatus) {
						lastStatus = false;
						appendMsgToMsgLog("Bluetooth Disconnected!");
						appendMsgToMsgLog("Waiting for Bluetooth connection...");
					}
				}

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		keepRunning = true;
		lastStatus = false;
		context = this;
		setUpGUI();

		new EbayTask().execute("a book", "it's a good book", "9780966101072");

		appendMsgToMsgLog("Waiting for Bluetooth connection...");
	}
	
	private class EbayTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			EbayInvoke ebayInvoker = new EbayInvoke(MainActivity.this);
			try {
				ebayInvoker.listBookWithEbay(params[0], params[1], params[2]);
				SuccessOrFaliureDialog(true, 0.0);
			} catch(IOException e) {
				SuccessOrFaliureDialog(false, 0.0);
			}
			
			return null;
		}
	}

	private void setUpGUI() {
		setUpConnectButton();
		setUpDisconnectButton();
		setupHandler();
		setupMsgLog();
		setupScanLog();
	}

	private void setUpConnectButton() {
		connectButton = (Button) findViewById(R.id.connectButton);
		connectButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					btComm.connect();
				} catch (IOException e) {
					Toast.makeText(MainActivity.this, "Couldn't connect!", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private void setUpDisconnectButton() {
		disconnectButton = (Button) findViewById(R.id.disconnectButton);
		disconnectButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					btComm.disconnect();
				} catch (IOException e) {
					Toast.makeText(MainActivity.this, "Couldn't disconnect!", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@SuppressLint("HandlerLeak")
	private void setupHandler() {
		handler = new Handler() {
			public void handleMessage(Message msg) {
				String taggedMessage = (String) msg.obj;
				String[] tokens = taggedMessage.split("~");

				final String message = tokens[1];
				if (tokens[0].equals("LOG")) {
					msgLog.append(message + "\n");
					logScrollContainer.fullScroll(View.FOCUS_DOWN);
				} else if (tokens[0].equals("SCAN")) {
					ScannedObject scan = new ScannedObject(message, Calendar.getInstance().getTime());
					scanList.add(scan);
					LinearLayout newScanView = new LinearLayout(context);
					TextView scanCode = new TextView(context);
					TextView scanDate = new TextView(context);
					Button lookupItem = new Button(context);

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1);

					scanCode.setLayoutParams(params);
					scanCode.setText(message);

					scanDate.setLayoutParams(params);
					scanDate.setText(scan.scanDate.toGMTString());

					lookupItem.setLayoutParams(params);
					lookupItem.setText("List on ebay");
					lookupItem.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							LinearLayout dialogView = (LinearLayout) findViewById(R.id.dialogView);
							AlertDialog.Builder ebayDialogBuilder = new AlertDialog.Builder(context);
							ebayDialogBuilder.setView(dialogView);
							ebayDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									ebayDialog.dismiss();
								}
							});
							ebayDialogBuilder.setPositiveButton("List Item", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									EbayInvoke ebayInvoker = new EbayInvoke(MainActivity.this);
									try {
										ebayInvoker.listBookWithEbay("a book", "it's a really cool book", message);
										SuccessOrFaliureDialog(true, 0.0);
									} catch(IOException e) {
										SuccessOrFaliureDialog(false, 0.0);
									}
								}
							});
						}
					});

					newScanView.addView(scanCode);
					newScanView.addView(scanDate);
					newScanView.addView(lookupItem);

					scannedContainer.addView(newScanView, 0);
				}
			}
		};
	}

	private void SuccessOrFaliureDialog(boolean wasSuccessful, double fee) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		if (wasSuccessful) {
			dialogBuilder.setTitle("Listing successful");
			dialogBuilder.setMessage("Estimated listing fee: " + fee);
		} else {
			dialogBuilder.setTitle("Listing unsuccessful");
		}
		dialogBuilder.create().show();
	}

	private void setupMsgLog() {
		logScrollContainer = (ScrollView) findViewById(R.id.scrollView);
		msgLog = (TextView) findViewById(R.id.messageLog);
		msgLog.append("Android Service Init...\n");
		msgLog.setMovementMethod(new ScrollingMovementMethod());
	}

	private void setupScanLog() {
		scannedContainer = (LinearLayout) findViewById(R.id.scannedItems);
		scanList = new ArrayList<ScannedObject>();
	}

	private void appendMsgToMsgLog(String str) {
		Message msg = Message.obtain(handler);
		msg.obj = "LOG~" + str;
		handler.sendMessage(msg);
	}

	private void addItemToScanLog(final String itemCode) {
		Message msg = Message.obtain(handler);
		msg.obj = "SCAN~" + itemCode;
		handler.sendMessage(msg);
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			if (btComm == null) {
				btComm = new BluetoothComm(DEVICE_NAME);
				btComm.connect();
			} else {
				btComm.resumeConnection();
			}
		} catch (IOException e) {
			Toast.makeText(MainActivity.this, "Couldn't connect!", Toast.LENGTH_LONG).show();
		}

		btComm.shouldPrintLogMsgs(true);
		msgThread = new Thread(msgUpdateThread);
		msgThread.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		keepRunning = false;

		try {
			btComm.pauseConnection();
		} catch (IOException e) {
			Toast.makeText(MainActivity.this, "Couldn't disconnect!", Toast.LENGTH_LONG).show();
		}
	}
}