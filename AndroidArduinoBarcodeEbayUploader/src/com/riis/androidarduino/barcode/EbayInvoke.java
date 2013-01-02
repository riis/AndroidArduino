package com.riis.androidarduino.barcode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.text.TextUtils;

public class EbayInvoke {
	private static final String ebayListingRequestUrl = "https://api.sandbox.ebay.com/wsapi";

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
		httpPost.addHeader("Content-Type", "text/xml");
		httpPost.addHeader("SOAPAction", "anything");
		httpPost.addHeader("X-EBAY-SOA-SECURITY-TOKEN", "AgAAAA**AQAAAA**aAAAAA**2Oi9UA**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6AFlIGpC5WApw+dj6x9nY+seQ**tK4BAA**AAMAAA**pnpeztXJFAky2tnAVXm7d6vm4558e4ei+nz2RuMW4Uri3IPrQUuDUdlA5Yz+jnef7FMLFyi7n7VroXr1tvpgKpg4kUMoKdaeMC5ok8kplfJIndVMjbaWYX9zQCf0NaLRX2dXAE2yMtNU74dj5DgYZesqB+qXIkYB3XcsAXFf2+EtwH7YSJ/rfc7JD5RWAb4SnrAHE1OhWkVKa2Z901xq21N3/DHX6rhEYBV2UgH5HksO+Nlkfdq0o+RoPZjhmAedqB5SJ0JcYC6vbSQs8mpqzgl/Li6a9O0HwA1m4VjI1n+ga4ohDsJOUFt8o/+WecQERvOfnlUnMFo+Xj3Mus2s7Bis1cpPlO2OJVCyWII/f/PoCcKvoqvayjToeTtWEeYi9tZX6J+FkjlcXQtQnvZaU92shMsuOgwkdzayEWzzFqWA1ZLBK/+t6APVgJ2ApXKBwnGPsvG9Fz2rQm99vM92SzyLtuFvmourwy14F+1dTxVd7TdtFLKQ4j+GavX6+ap474hxhNYRS2VvNQ6GFKu8nDpnVDADuts4WEKBJf919mImV42p+BJ/nCgLW4ohEco8QBSCcPgsBovoHgAbw0nruxgLtrWyrL15E7i6mHKZ1RckK5YFJR8CDcAJybiQw0CU+q/Jh+yRO7bgG5TEN0Lp1w9HDQxfI4EoyUsFv5Vl1uk3Mxj04G6csBxJ9JjO44gf+p4ZJQDzUq+mZG1Kx1/FF6PWj8EHxjE6oMUxCLkgrbFHvhYjIT5XBidP0PhX6wTe");
		httpPost.setEntity(new StringEntity(requestXml.toString()));
		
		HttpResponse response = httpClient.execute(httpPost);
		StatusLine statusLine = response.getStatusLine();
		HttpEntity httpEntity = response.getEntity();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		httpEntity.writeTo(baos);
		String responseString = baos.toString();

		return responseString;
	}
}