#ifndef Bluetooth_H
#define Bluetooth_H

#include <Arduino.h>
#include <SoftwareSerial.h>

class Bluetooth {
public:
	Bluetooth();
	Bluetooth(int RX, int TX, char deviceName[], boolean shouldPrintLog);
	~Bluetooth();
	
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
	
	void readStatusUpdate();

	SoftwareSerial bluetoothSerial;
	int state;
	boolean printLog;
	char* name;
};

#endif