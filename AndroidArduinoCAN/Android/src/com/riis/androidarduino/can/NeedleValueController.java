package com.riis.androidarduino.can;

import java.util.ArrayList;

public class NeedleValueController {
	private static final int SMOOTH_LEVEL = 5;
	private ArrayList<TimestampedValue> dataList;
	
	private long lastUpdatedTime;
	
	public NeedleValueController() {
		dataList = new ArrayList<TimestampedValue>(SMOOTH_LEVEL);
		lastUpdatedTime = System.currentTimeMillis();
	}

	public void addValue(double value) {
		if(dataList.size()  == SMOOTH_LEVEL)
			dataList.remove(0);
		dataList.add(new TimestampedValue(System.currentTimeMillis(), value));
	}
	
	public double getLatestSlope() {
		if(dataList.size() < SMOOTH_LEVEL) {
			return 0.0;
		}
		
		double dy = 0;
		for(int i = 0; i < dataList.size()-1; ++i) {
			double yDiff = (double)(dataList.get(i).value - dataList.get(i+1).value);
			dy += yDiff;
		}
		dy /= dataList.size();
		
		double avgdt = 0;
		for(int i = 0; i < dataList.size()-1; ++i) {
			double timeDiff = (double)(dataList.get(i).timestamp - dataList.get(i+1).timestamp);
			avgdt += timeDiff;
		}
		avgdt /= dataList.size();
		
		return dy / avgdt;
	}
	
	public double getIncrementSinceLastCall() {
		long deltaTime = System.currentTimeMillis() - lastUpdatedTime;
		lastUpdatedTime = System.currentTimeMillis();
		
		return getLatestSlope() * deltaTime;
	}
}
