package com.riis.androidarduino.lib;

import java.io.IOException;

public interface iCommunication {
	public void connect() throws IOException;
	public void disconnect() throws IOException;
	public void pauseConnection() throws IOException;
	public void resumeConnection() throws IOException;
	public int  read(byte[] byteBuffer);
	public void write(byte[] byteBuffer);
}
