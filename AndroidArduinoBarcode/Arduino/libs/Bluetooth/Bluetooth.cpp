#include <Bluetooth.h>

Bluetooth::Bluetooth()
	: bluetoothSerial(0, 0, false)
{

}

Bluetooth::Bluetooth(int RX, int TX, String deviceName, boolean shouldPrintLog)
	: bluetoothSerial(RX, TX, false)
{
	printLog = shouldPrintLog;

	if(printLog) { Serial.println("Setting up Bluetooth I/O..."); }
	pinMode(RX, INPUT);
	pinMode(TX, OUTPUT);
	
	connectionState = 0;
	name = deviceName;
	
	readingStateMsg = false;
}

Bluetooth::~Bluetooth() { }

boolean Bluetooth::beginBluetooth()
{
	if(printLog) { Serial.println("Powering up the Bluetooth device..."); }
    if(!setUpBluetooth())
    {
        if(printLog) { Serial.println("Bluetooth setup failed!"); }
        if(printLog) { Serial.println("Make sure it's connected properly and reset the Arduino board."); }
		return false;
    } else {
		if(printLog) { Serial.println("Bluetooth ready!"); }
		return true;
	}	
}

boolean Bluetooth::setUpBluetooth()
{
    bluetoothSerial.begin(38400);

    if(!sendCommand("\r\n+STWMOD=0\r\n")) {return false;} //Set the Bluetooth to slave mode
    if(!sendCommand("\r\n+AndroidArduinoBTRS232\r\n")) {return false;} //Set the Bluetooth name to "AndroidArduinoBTRS232"
    if(!sendCommand("\r\n+STOAUT=1\r\n")) {return false;} //Permit a paired device to connect
	if(!sendCommand("\r\n+STAUTO=0\r\n")) {return false;} //No auto connection
	
    delay(2000);    
    if(!sendCommand("\r\n+INQ=1\r\n")) {return false;} //Make the Bluetooth device inquirable 
	
    delay(2000);
    bluetoothSerial.flush();

    return true;
}

boolean Bluetooth::sendCommand(char command[])
{
	bluetoothSerial.print(command);
    return waitForCommandOK();
}

boolean Bluetooth::waitForCommandOK()
{
    unsigned long startTime = millis();
    char msgChar = '\0';
    
    while(msgChar != 'O')
    {
        if(bluetoothSerial.available())
        {
            msgChar = bluetoothSerial.read();
        }
        
        if(millis() > startTime + 5000)
        {
            return false; //Timed out, return that the command failed
        }
    }
    
    while(msgChar != 'K')
    {
        if(bluetoothSerial.available())
        {
            msgChar = bluetoothSerial.read();
        }
        
        if(millis() > startTime + 5000)
        {
            return false; //Timed out, return that the command failed
        }
    }
 
    return true;
}


void Bluetooth::sendByte(byte value)
{
	bluetoothSerial.write(value);
}

void Bluetooth::sendByteWithFlag(char flag, byte value)
{
	bluetoothSerial.write(flag);
	bluetoothSerial.write(value);
}

int Bluetooth::bytesAvailable()
{	
	//if(bluetoothSerial.available())
	//{
	//	if(!isFlag(bluetoothSerial.peek()))
	//	{
	//		readStatusUpdate();
	//		return bluetoothSerial.available();
	//	}
	//	else
	//	{
	//		return bluetoothSerial.available();
	//	}
	//}
	//else
	//{
		return bluetoothSerial.available();
	//}
}

boolean Bluetooth::isFlag(char flag)
{
	return (flag == 'S' || flag == 'N');
}

byte Bluetooth::readByte()
{
	return (byte)bluetoothSerial.read();
}

void Bluetooth::process()
{
//	int charsAvailable = bluetoothSerial.available();
//	if(charsAvailable)
//	{
//		if(!readingStateMsg && stateMsgLen < 3)
//		{
//			while(charsAvailable && stateMsgLen < 14)
//			{
//				stateMsgBuf[stateMsgLen] = (char)bluetoothSerial.read();
//				stateMsgLen += 1
//				charsAvailable -= 1;
//			}
//		}
//	}
//	
//	if(!readingStateMsg && stateMsgLen >= 3)
//	{
//		readingStateMsg = isStateMsgStarting();
//	}
//	
//	if(readingStateMsg && stateMsgLen == 14)
//	{
//		if(isStateMsgAStatusUpdate())
//		{
//			connectionState = 
//		}
//	}
}

boolean Bluetooth::isStateMsgStarting()
{
//	return (stateMsgBuf[0] == '\r' && stateMsgBuf[1] == '\n' && stateMsgBuf[2] == '+');
}

boolean Bluetooth::isStateMsgAStatusUpdate()
{
//	return(stateMsgBuf[2] == '+' &&
//		   stateMsgBuf[3] == 'B' &&
//		   stateMsgBuf[4] == 'T' &&
//		   stateMsgBuf[5] == 'S' &&
//		   stateMsgBuf[6] == 'T' &&
//		   stateMsgBuf[7] == 'A' &&
//		   stateMsgBuf[8] == 'T' &&
//		   stateMsgBuf[9] == 'E');
}

boolean Bluetooth::isConnected()
{
	return (connectionState == 4);
}