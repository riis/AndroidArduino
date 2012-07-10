#ifndef Bluetooth_H
#define Bluetooth_H

#include <Arduino.h>
#include <SoftwareSerial.h>

class Bluetooth {
public:
	Bluetooth();
	Bluetooth(int RX, int TX, String deviceName, boolean shouldPrintLog);
	~Bluetooth();
	
	boolean beginBluetooth();
	boolean setUpBluetooth();
	boolean sendCommand(char command[]);
	
	void sendByte(byte value);
	void sendByteWithFlag(char flag, byte value);
	
	int bytesAvailable();
	byte readByte();
	
	void process();
	boolean isConnected();
private:
	boolean setUpBluetoothConnection();
	boolean waitForCommandOK();
	boolean isFlag(char flag);
	
	boolean isStateMsgStarting();
	boolean isStateMsgAStatusUpdate();

	SoftwareSerial bluetoothSerial;
	int connectionState;
	String name;
	boolean printLog;
	
	String stateMsg;
	boolean readingStateMsg;
};

#endif