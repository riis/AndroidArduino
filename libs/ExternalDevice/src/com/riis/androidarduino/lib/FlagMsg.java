package com.riis.androidarduino.lib;

public class FlagMsg {
	private char flag;
	private int reading;

	public FlagMsg(char flag, int reading) {
		this.flag = flag;
		this.reading = reading;
	}

	public int getReading() {
		return reading;
	}
	
	public char getFlag() {
		return flag;
	}
}
