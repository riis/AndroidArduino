package com.riis.androidarduino.barcode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.text.TextUtils;

public class EbayInvoke {
	private static final String ebayListingRequestUrl = "https://api.ebay.com/wsapi";

	private Context context;

	public EbayInvoke(Context context) {
		this.context = context;
	}

	public String listBookWithEbay(String title, String description, String UPC) throws IOException {
		String xmlTemplateString = "";
		char[] inputBufer = new char[1024];

		InputStreamReader xmlInputStream = new InputStreamReader(context.getAssets().open("addItemTemplate.xml"));
		StringBuilder sb = new StringBuilder();

		try {
			for(int cnt; (cnt = xmlInputStream.read(inputBufer)) > 0;)
				sb.append(inputBufer, 0, cnt);
		} finally {
			xmlInputStream.close();
		}
		xmlTemplateString = sb.toString();

		CharSequence requestXml = TextUtils.expandTemplate(xmlTemplateString, title, description, UPC);

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(ebayListingRequestUrl);
		httpPost.addHeader("Host", "api.ebay.com");
		httpPost.addHeader("application/xml", requestXml.toString());

		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity httpEntity = response.getEntity();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		httpEntity.writeTo(baos);
		String responseString = baos.toString();

		return null;
	}
}