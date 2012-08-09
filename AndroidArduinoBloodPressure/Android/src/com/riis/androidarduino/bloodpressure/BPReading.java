package com.riis.androidarduino.bloodpressure;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class BPReading {
	public String user;
	private double systolic;
	private double diastolic;
	private String timestamp;
	
	public BPReading(String userName, double systolic, double diastolic) {
		this.user = userName;
		this.systolic = systolic;
		this.diastolic = diastolic;
		DateFormat df = DateFormat.getDateInstance();
		timestamp = df.format(df.getCalendar());
	}
	
	public String getTimestampString() {
		NumberFormat formatter = new DecimalFormat("#0.0");
		return "" + formatter.format(systolic) + "/" + formatter.format(diastolic);
	}
	
	public String getBloodPressureString() {
		return timestamp;
	}
}
