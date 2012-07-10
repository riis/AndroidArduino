package com.riis.androidarduino.lib;

public interface iCommunication {
	public void connect();
	public void disconnect();
	public void pauseConnection();
	public void resumeConnection();
	public int  read(byte[] byteBuffer);
	public void write(byte[] byteBuffer);
}
