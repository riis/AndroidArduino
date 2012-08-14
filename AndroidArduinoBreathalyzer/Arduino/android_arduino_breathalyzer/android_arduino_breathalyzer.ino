#include <Bluetooth.h>
#include <SoftwareSerial.h>

#define SENSOR_PIN 0
#define REFRESH_INTERVAL 100

//Bluetooth
#define RX 62
#define TX 7

SoftwareSerial bluetoothSerial(RX, TX);
Bluetooth bluetooth("AndroidArduinoCANBT", bluetoothSerial, true, 3);

boolean lastConnectionState; //True for connected and false for disconnected
unsigned long lastRunTime = 0;

HardwareSerial* logSerial;

void setup() {
    logSerial = &Serial;
    
    setUpIO();
    logSerial->begin(115200);
    
    if(!bluetooth.beginBluetooth())
    {
        logSerial->println("\n\rHalting program...");
        while(true) { }
    }
    
    logSerial->print("Waiting for Bluetooth connection...\n\r");
}

void setUpIO()
{    
    pinMode(RX, INPUT);
    pinMode(TX, OUTPUT);
}

void loop() {
    bluetooth.process();
    
    if(bluetooth.isConnected()) {
        if(!lastConnectionState)
        {
            printConnectedMessage();
            lastConnectionState = true;
        }
        
        if(millis() > (lastRunTime + REFRESH_INTERVAL)) {
            int sensorReading = analogRead(SENSOR_PIN);
            String readingString = String(sensorReading, DEC);
            
            for(int i = 0; i < readingString.length(); i++) {
                bluetooth.sendByteWithFlag('S', (byte)readingString[i]);
                logSerial->print('S');
                logSerial->print(readingString[i]);
            }
            bluetooth.sendByteWithFlag('N', 255);
            logSerial->print('N');
            logSerial->println((char)255);
            
            lastRunTime = millis();
        }
    }
    else
    {
        if(lastConnectionState)
        {
            printDisconnectedMessage();
            lastConnectionState = false;
        }   
    }
}

void printConnectedMessage()
{
    logSerial->print("\n\rConnected! Communications ready on the terminal\n\n\r");
}

void printDisconnectedMessage()
{
    logSerial->print("Disconnected! Halting communications...");    
    logSerial->print("\n\rWaiting for Bluetooth connection...\n\r");
}
