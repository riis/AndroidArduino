#include <Bluetooth.h>
#include <SoftwareSerial.h>

//LED command codes
#define LED_OFF 2
#define LED_ON  1

//LED pin numbers
#define RED_LED 8
#define YLW_LED 10
#define GRN_LED 12

//State machine states
#define RECEIVING_COMMAND_FLAG 0
#define RECEIVING_COMMAND_DATA 1

//Bluetooth
#define RX 62
#define TX 7

SoftwareSerial bluetoothSerial(RX, TX);
Bluetooth bluetooth("AndroidArduinoBT", bluetoothSerial, true, 3);

boolean lastConnectionState = false;

int state;
int currentCommand;
int currentLEDNum;

void setup()
{
    setUpIO();  
    resetState();

    Serial.begin(115200);

    if(!bluetooth.beginBluetooth()) {
        Serial.println("\n\rHalting program...");
        while(true) { 
        }
    }
}

void setUpIO()
{
    pinMode(RED_LED, OUTPUT);
    pinMode(YLW_LED, OUTPUT); 
    pinMode(GRN_LED, OUTPUT);

    pinMode(RX, INPUT);
    pinMode(TX, OUTPUT);
}

void resetState()
{
    state = RECEIVING_COMMAND_FLAG;
    currentCommand = LED_OFF;
    currentLEDNum = RED_LED;
}

void loop()
{
    bluetooth.process();
    
    byte msg;

    if(bluetooth.isConnected()) {
        if(!lastConnectionState) {
            printConnectedMessage();
            lastConnectionState = true;
        }
        
        if(bluetooth.bytesAvailable() > 1) {
            msg = bluetooth.readByte();
            Serial.println(msg);
            if(msg == 'L') {
                msg = bluetooth.readByte();   
                Serial.println(msg);
            }

            runStateMachine(msg);
        }
    } else {
        if(lastConnectionState) {
            printDisconnectedMessage();
            lastConnectionState = false;
        } 
    }
}

void printConnectedMessage() {
    Serial.print("Bluetooth connected!\n\r");
}

void printDisconnectedMessage() {
    Serial.print("Disconnected! Halting communications...");    
    Serial.print("\n\rWaiting for Bluetooth connection...\n\r");
}

void runStateMachine(byte msg)
{
    switch(state)
    {
    case RECEIVING_COMMAND_FLAG:
        if(isMessageAnLEDFlag(msg))
        {
            setCurrentLEDNum(msg);
            state = RECEIVING_COMMAND_DATA;
        } 
        else {
            resetState(); 
        }
        break;
    case RECEIVING_COMMAND_DATA:
        if(isMessageACommand(msg))
        {
            currentCommand = msg;
            executeCommand();
        }

        resetState(); 
        break;
    default:
        resetState();
        break;
    }
}

boolean isMessageAnLEDFlag(byte msg)
{
    char msgChar = (char) msg;
    return (msgChar == 'r' || msgChar == 'y' || msgChar == 'g');
}

void setCurrentLEDNum(byte msg)
{
    char msgChar = (char) msg;

    if(msgChar == 'r')
    {
        currentLEDNum = RED_LED;
    }
    else if(msgChar == 'y')
    {
        currentLEDNum = YLW_LED;
    }
    else if(msgChar == 'g')
    {
        currentLEDNum = GRN_LED;
    }
}

boolean isMessageACommand(byte msg)
{
    return (msg == LED_OFF || msg == LED_ON);
}

void executeCommand()
{
    Serial.print("Turning pin ");
    Serial.print(currentLEDNum);

    if(currentCommand == LED_ON)
    {
        Serial.println(" on...");
        digitalWrite(currentLEDNum, HIGH);
    }
    else if(currentCommand == LED_OFF)
    {
        Serial.println(" off...");
        digitalWrite(currentLEDNum, LOW);
    }

    Serial.println();
}


