package com.riis.androidarduino.lib;

public class FlagMsg {
	private char flag;
	private byte reading;

	public FlagMsg(char flag, byte reading) {
		this.flag = flag;
		this.reading = reading;
	}

	public int getValue() {
		return reading;
	}
	
	public char getFlag() {
		return flag;
	}
}
