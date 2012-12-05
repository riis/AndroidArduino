/**
 * This program is licensed under the terms of the eBay Common Development and
 * Distribution License (CDDL) Version 1.0 (the "License") and any subsequent
 * version thereof released by eBay.  The then-current version of the License
 * can be found at http://www.opensource.org/licenses/cddl1.php
 */

package com.ebay.api.soap.examples;

import java.util.Properties;

/**
 * @author JOHNSONAA
 * Class that encapsulates the properties
 * needed to make SOAP calls to the Ebay
 * API.
 */
public class EbayConfig {

	private String url;
	private String devid;
	private String appid;
	private String cert;
	private String token;
	private int siteid;
	private int version;

	private EbayConfig() {
	}

	public static EbayConfig parseConfig(Properties props) {
		EbayConfig config = new EbayConfig();
		config.setAppid(props.getProperty("appid"));
		config.setCert(props.getProperty("cert"));
		config.setDevid(props.getProperty("devid"));
		config.setSiteid(Integer.parseInt(props.getProperty("siteid")));
		config.setToken(props.getProperty("token"));
		config.setUrl(props.getProperty("url"));
		config.setVersion(Integer.parseInt(props.getProperty("version")));
		return config;
	}

	public String getRequestUrl(String callname) {
		return url +
		"?callname=" + callname +
		"&siteid=" + siteid +
		"&appid=" + appid +
		"&version=" + version +
		"&routing=default";

	}


	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getCert() {
		return cert;
	}
	public void setCert(String cert) {
		this.cert = cert;
	}
	public String getDevid() {
		return devid;
	}
	public void setDevid(String devid) {
		this.devid = devid;
	}
	public int getSiteid() {
		return siteid;
	}
	public void setSiteid(int siteid) {
		this.siteid = siteid;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
}
