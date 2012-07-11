package com.riis.androidarduino.lib.test;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.riis.androidarduino.lib.Util;

public class UtilTest {
	private static Random rand;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		rand = new Random();
	}

	@Test
	public void testComposeInt() {		
		byte highByte = (byte)rand.nextInt(255);
		byte lowByte = (byte)rand.nextInt(255);
		
		int resultInt = Util.composeInt(highByte, lowByte);
		
		assertEquals(resultInt, (highByte << 8) & lowByte);
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
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}
}
