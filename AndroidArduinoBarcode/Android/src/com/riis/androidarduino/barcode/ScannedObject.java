package com.riis.androidarduino.barcode;

import java.util.Date;


public class ScannedObject {
	
	public ScannedObject(String itemCode, Date scanDate) {
		this.scanCode = itemCode;
		this.scanDate = scanDate;
	}
	
	public String scanCode;
	public Date scanDate;
}
