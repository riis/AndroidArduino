package com.riis.androidarduino.card;

import java.util.Date;


public class ScannedObject {
	
	private String track1;
	private String track2;
	private String track3;
	
	private Date scanDate;
	
	public boolean matches(ScannedObject otherScan) {
		return track1.equalsIgnoreCase(otherScan.track1) &&
				track2.equalsIgnoreCase(otherScan.track2) &&
				track3.equalsIgnoreCase(otherScan.track3);
	}

	public String getTrack1() {
		return track1;
	}

	public void setTrack1(String track1) {
		this.track1 = track1;
	}

	public String getTrack2() {
		return track2;
	}

	public void setTrack2(String track2) {
		this.track2 = track2;
	}

	public String getTrack3() {
		return track3;
	}

	public void setTrack3(String track3) {
		this.track3 = track3;
	}

	public Date getScanDate() {
		return scanDate;
	}

	public void setScanDate(Date scanDate) {
		this.scanDate = scanDate;
	}
}
