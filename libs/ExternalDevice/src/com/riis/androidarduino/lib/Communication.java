package com.riis.androidarduino.lib;

public interface Communication {
	public void connect();
	public void disconnect();
	public void pauseConnection();
	public void resumeConnection();
}
