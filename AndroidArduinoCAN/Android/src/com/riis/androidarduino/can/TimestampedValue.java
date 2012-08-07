package com.riis.androidarduino.can;

public class TimestampedValue {
	public long timestamp;
	public double value;
	
	public TimestampedValue(long time, double value) {
		this.timestamp = time;
		this.value = value;
	}
}
