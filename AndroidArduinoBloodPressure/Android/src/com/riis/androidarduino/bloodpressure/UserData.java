package com.riis.androidarduino.bloodpressure;

import java.util.Date;

public class UserData {
	private int userID;
	private String timestamp;
	private int systolic;
	private int diastolic;
	
	public UserData() {
		
	}
	
	public UserData(int userID, Date date, int systolic, int diastolic) {
		this.userID = userID;
		this.timestamp = date.toString();
		this.systolic = systolic;
		this.diastolic = diastolic;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int getSystolic() {
		return systolic;
	}

	public void setSystolic(int systolic) {
		this.systolic = systolic;
	}

	public int getDiastolic() {
		return diastolic;
	}

	public void setDiastolic(int diastolic) {
		this.diastolic = diastolic;
	}
}
