#ifndef Bluetooth_H
#define Bluetooth_H

#include <Arduino.h>
#include <SoftwareSerial.h>

class Bluetooth {
public:
    Bluetooth(String deviceName, SoftwareSerial &bluetoothSerial, boolean shouldPrintLog);
    ~Bluetooth();
	
    boolean beginBluetooth();
    boolean sendSetupCommands();
    boolean sendCommand(String command);
	
    void sendByte(byte value);
    void sendStringRaw(String message);
    
    void sendByteWithFlag(char flag, byte value);
    void sendStringWithFlags(String message);
	
    int bytesAvailable();
    byte readByte();
	
    void process();
    boolean isConnected();
    
private:
    boolean setUpBluetoothConnection();
    boolean waitForCommandOK();
	
    int isStringWithinBuffer(String string);
    boolean isCompleteStatusMessageInBuffer();
    String cutStatusMessageOutOfBuffer();
    void parseStatusMessage();

    void logMsg(String message);

    SoftwareSerial* bluetoothSerial;
    char connectionState;
    boolean shouldPrintLog;
    
    String deviceName;
    
    String inputBuffer;    
    String statusMessage;
    boolean allowReading;
};

#endif
