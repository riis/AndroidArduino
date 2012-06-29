package com.riis.androidarduino.lib;

public class Util {
	public static int composeInt(byte hi, byte lo) {
		int val = (int) hi & 0xff;
		val *= 256;
		val += (int) lo & 0xff;
		return val;
	}
	
	public static byte[] stringToByteArray(String str) {
		char[] buffer = str.toCharArray();
		byte[] b = new byte[buffer.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) buffer[i];
		}
		return b;
	}
}
