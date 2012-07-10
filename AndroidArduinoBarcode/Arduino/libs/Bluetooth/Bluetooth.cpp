#include <Bluetooth.h>

Bluetooth::Bluetooth()
	: bluetoothSerial(0, 0, false)
{

}

Bluetooth::Bluetooth(int RX, int TX, char* deviceName, boolean shouldPrintLog)
	: bluetoothSerial(RX, TX, false)
{
	Serial.begin(115200);
	printLog = shouldPrintLog;

	if(printLog) { Serial.println("Setting up Bluetooth I/O..."); }
	pinMode(RX, INPUT);
	pinMode(TX, OUTPUT);
	
	state = 0;
	name = deviceName;
	
	if(printLog) { Serial.println("Powering up the Bluetooth device..."); }
    if(!setUpBluetooth())
    {
        if(printLog) { Serial.println("Bluetooth setup failed!"); }
        if(printLog) { Serial.println("Make sure it's connected properly and reset the Arduino board."); }
    } else {
		if(printLog) { Serial.println("Bluetooth ready!"); }
	}	
}

Bluetooth::~Bluetooth() { }

boolean Bluetooth::setUpBluetooth()
{
    bluetoothSerial.begin(38400);

	Serial.print(0);
    if(!sendCommand("\r\n+STWMOD=0\r\n")) {return false;} //Set the Bluetooth to slave mode
	Serial.print(1);
    if(!sendCommand("\r\n+STNA=AndroidArduinoBTRS232\r\n")) {return false;} //Set the Bluetooth name to "AndroidArduinoBTRS232"
	Serial.print(2);
    if(!sendCommand("\r\n+STOAUT=1\r\n")) {return false;} //Permit a paired device to connect
    Serial.print(3);
	if(!sendCommand("\r\n+STAUTO=0\r\n")) {return false;} //No auto connection
    Serial.print(4);
	
    delay(2000);    
    if(!sendCommand("\r\n+INQ=1\r\n")) {return false;} //Make the Bluetooth device inquirable 
	Serial.print(5);
	
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
	bluetoothSerial.print(value);
}

void Bluetooth::sendByteWithFlag(char flag, byte value)
{
	bluetoothSerial.print(flag);
	bluetoothSerial.print(value);
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
	if(bluetoothSerial.available())
	{
		if(!isFlag(bluetoothSerial.peek()))
		{
			readStatusUpdate();
		}
	}
}

void Bluetooth::readStatusUpdate()
{
	if(bluetoothSerial.available())
	{
		if(bluetoothSerial.peek() == '\r')
		{
			bluetoothSerial.read();
			
			if(bluetoothSerial.peek() == '\n')
			{
				bluetoothSerial.read();
			
				if(bluetoothSerial.peek() == '+')
				{
					bluetoothSerial.read();
			
					if(bluetoothSerial.read() == 'B' &&
					   bluetoothSerial.read() == 'S' &&
					   bluetoothSerial.read() == 'T' &&
					   bluetoothSerial.read() == 'A' &&
					   bluetoothSerial.read() == 'T' &&
					   bluetoothSerial.read() == 'E' &&
					   bluetoothSerial.read() == ':')
					{
						state = bluetoothSerial.read() - 48;
					}
				}
			}
		}
	}
}

boolean Bluetooth::isConnected()
{
	return (state == 4);
}