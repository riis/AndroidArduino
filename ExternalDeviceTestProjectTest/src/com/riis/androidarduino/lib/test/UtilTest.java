package com.riis.androidarduino.lib.test;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.riis.androidarduino.lib.Util;

public class UtilTest {

	@Test
	public void testComposeInt() {
		Random rand = new Random();
		int highByte = rand.nextInt(256);
		int lowByte = rand.nextInt(256);
		
		int resultInt = Util.composeInt((byte)highByte, (byte)lowByte);
		
		assertEquals(resultInt, (highByte*256 + lowByte));
	}

	@Test
	public void testStringToByteArray() {
		String testString = "Hello, World";		
		byte byteString[] = Util.stringToByteArray(testString);
		
		assertEquals(testString.length(), byteString.length);
		
		for(int i = 0; i < testString.length(); i++) {
			assertEquals((byte)testString.charAt(i), byteString[i]);
		}
	}

}
