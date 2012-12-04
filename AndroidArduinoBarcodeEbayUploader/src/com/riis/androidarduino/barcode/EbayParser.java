package com.riis.androidarduino.barcode;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.res.Resources;

public class EbayParser {
	private static SimpleDateFormat dateFormat;
	private Resources resources;

	public EbayParser(Context context) {
		synchronized(this) {
			if(dateFormat == null) {
				dateFormat = new SimpleDateFormat("[\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\"]");
			}
		}
		this.resources = context.getResources();
	}

	//Functions for parsing responses from JSON for our calls
}