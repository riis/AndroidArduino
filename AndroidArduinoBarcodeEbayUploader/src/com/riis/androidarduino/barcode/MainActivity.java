package com.riis.androidarduino.barcode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiCredential;
import com.ebay.soap.eBLBaseComponents.AmountType;
import com.ebay.soap.eBLBaseComponents.BuyerPaymentMethodCodeType;
import com.ebay.soap.eBLBaseComponents.CategoryType;
import com.ebay.soap.eBLBaseComponents.CountryCodeType;
import com.ebay.soap.eBLBaseComponents.CurrencyCodeType;
import com.ebay.soap.eBLBaseComponents.FeesType;
import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebay.soap.eBLBaseComponents.ListingDurationCodeType;
import com.ebay.soap.eBLBaseComponents.ListingTypeCodeType;
import com.ebay.soap.eBLBaseComponents.ProductListingDetailsType;
import com.ebay.soap.eBLBaseComponents.ReturnPolicyType;
import com.ebay.soap.eBLBaseComponents.ShippingDetailsType;
import com.ebay.soap.eBLBaseComponents.ShippingServiceCodeType;
import com.ebay.soap.eBLBaseComponents.ShippingServiceOptionsType;
import com.ebay.soap.eBLBaseComponents.ShippingTypeCodeType;
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

	private ApiContext ebayContext;
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
		try {
			ebayContext = getApiContext();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setUpGUI();

		appendMsgToMsgLog("Waiting for Bluetooth connection...");
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
									AddItemCall api = new AddItemCall(ebayContext);
									try {
										api.setItem(buildItem("a book", "with a description", message));
										FeesType fees = api.addItem();
										double listingFee = eBayUtil.findFeeByName(fees.getFee(), "ListingFee").getFee().getValue();
										SuccessOrFaliureDialog(true, listingFee);
									} catch (Exception e) {
										SuccessOrFaliureDialog(false, 0);
										e.printStackTrace();
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

	private static ApiContext getApiContext() throws IOException {
		ApiContext apiContext = new ApiContext();

		// set Api Token to access eBay Api Server
		ApiCredential cred = apiContext.getApiCredential();
		cred.seteBayToken("26fe32fa-8721-479b-8051-f0a4d241a8b3");

		// set Api Server Url
		apiContext.setApiServerUrl("https://api.ebay.com/wsapi");

		return apiContext;
	}

	private static ItemType buildItem(String title, String description, String UPC) throws IOException {
		ItemType item = new ItemType();

		// item title
		item.setTitle(title);
		// item description
		item.setDescription(description);

		// listing type
		item.setListingType(ListingTypeCodeType.CHINESE);
		// listing price
		item.setCurrency(CurrencyCodeType.USD);
		AmountType amount = new AmountType();
		amount.setValue(5.0);
		item.setStartPrice(amount);

		// listing duration
		item.setListingDuration(ListingDurationCodeType.DAYS_14.value());

		// item location and country
		item.setLocation("Southfield, MI");
		item.setCountry(CountryCodeType.US);

		// listing category

		// TODO need catid;
		CategoryType cat = new CategoryType();
		cat.setCategoryID(ConsoleUtil.readString("Primary Category (e.g., 30022): "));
		item.setPrimaryCategory(cat);

		// item quality
		item.setQuantity(1);

		// payment methods
		item.setPaymentMethods(new BuyerPaymentMethodCodeType[] { BuyerPaymentMethodCodeType.PAY_PAL });
		// email is required if paypal is used as payment method
		item.setPayPalEmailAddress("godfrey@riis.com");

		ProductListingDetailsType listingDetails = new ProductListingDetailsType();
//		listingDetails.setISBN(title); // TODO get this from scanner
		listingDetails.setUPC(UPC); // TODO get this from scanner
		listingDetails.setIncludeStockPhotoURL(true);
		listingDetails.setIncludePrefilledItemInformation(true);
		listingDetails.setUseFirstProduct(true);
		listingDetails.setUseStockPhotoURLAsGallery(true);
		listingDetails.setReturnSearchResultOnDuplicates(true);

		item.setProductListingDetails(listingDetails);

		// item condition, New
		item.setConditionID(1000);

		// handling time is required
		item.setDispatchTimeMax(1);

		// shipping details
		item.setShippingDetails(buildShippingDetails());

		// return policy
		ReturnPolicyType returnPolicy = new ReturnPolicyType();
		returnPolicy.setReturnsAcceptedOption("ReturnsAccepted");
		item.setReturnPolicy(returnPolicy);

		return item;
	}

	private static ShippingDetailsType buildShippingDetails() {
		// Shipping details.
		ShippingDetailsType sd = new ShippingDetailsType();

		sd.setApplyShippingDiscount(true);
		AmountType amount = new AmountType();
		amount.setValue(2.8);
		sd.setPaymentInstructions("paypal");

		// Shipping type and shipping service options
		sd.setShippingType(ShippingTypeCodeType.FLAT);
		ShippingServiceOptionsType shippingOptions = new ShippingServiceOptionsType();
		shippingOptions.setShippingService(ShippingServiceCodeType.SHIPPING_METHOD_STANDARD.value());
		amount = new AmountType();
		amount.setValue(2.0);
		shippingOptions.setShippingServiceAdditionalCost(amount);
		amount = new AmountType();
		amount.setValue(10);
		shippingOptions.setShippingServiceCost(amount);
		shippingOptions.setShippingServicePriority(1);
		amount = new AmountType();
		amount.setValue(1.0);
		shippingOptions.setShippingInsuranceCost(amount);

		sd.setShippingServiceOptions(new ShippingServiceOptionsType[] { shippingOptions });

		return sd;
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