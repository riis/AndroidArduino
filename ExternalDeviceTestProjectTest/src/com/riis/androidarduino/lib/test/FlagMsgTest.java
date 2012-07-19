package com.riis.androidarduino.lib.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.riis.androidarduino.lib.FlagMsg;

public class FlagMsgTest {

	@Test
	public void testFlagMsg() {
		char flag = 'F';
		byte reading = 5;
		FlagMsg fm = new FlagMsg(flag, reading);
		assertEquals(flag, fm.getFlag());
		assertEquals(reading, fm.getValue());
	}

	@Test
	public void testGetValue() {
		char flag = 'S';
		byte reading = 's';
		FlagMsg fm = new FlagMsg(flag, reading);
		assertEquals(flag, fm.getFlag());
	}

	@Test
	public void testGetFlag() {
		char flag = 'S';
		byte reading = 'v';
		FlagMsg fm = new FlagMsg(flag, reading);
		assertEquals(reading, fm.getValue());
	}

}
