#include <SoftwareSerial.h>
#include "Bluetooth.h"

//Encryption defines
#define XOR_VAL 55

//Parsing defines
#define STRING_END 0
#define CARD_START 37
#define RETURN_CHAR 13
#define MSG_LENGTH_MAX 256

//BluetoothStates for the Bluetooth BluetoothState machine
#define RECEIVING_FLAG        1
#define RECEIVING_STRING_CHAR 2
#define RECEIVING_END_CODE    3

//BluetoothStates for the terminal BluetoothState machine
#define WAITING_FOR_START 1
#define RECEIVING_CHARS 2

//RX and TX pin numbers
#define RX 11
#define TX 3

Bluetooth bluetooth(RX, TX, "AndroidArduinoBTRS232", true);

int bluetoothState;
int terminalState;

boolean lastConnectionState; //True for connected and false for disconnected

byte incomingMsgBuf[MSG_LENGTH_MAX];

int bluetoothMsgLen;
char bluetoothMsg[MSG_LENGTH_MAX];

int terminalMsgLen;
char terminalMsg[MSG_LENGTH_MAX];

void setup()
{
    Serial.begin(115200);

    flushBuffersAndResetStates();

    Serial.print("Initializing card reader serial port...\n\r");    
    Serial1.begin(9600);
    
    if(!bluetooth.beginBluetooth())
    {
        Serial.println("\n\n\rHalting program...");
        while(true) { }
    }
    
    Serial.print("\n\rWaiting for Bluetooth connection...\n\r");
}

void flushBuffersAndResetStates()
{  
    flushIncomingMsgBuffer();
    flushBluetoothMsgBuffer();
    flushTerminalMsgBuffer();
    
    resetBluetoothState();
    resetTerminalState();
}

void flushIncomingMsgBuffer()
{
    for(int i = 0; i < MSG_LENGTH_MAX; i++) {    
        incomingMsgBuf[i] = 0; 
    } 
}

void flushBluetoothMsgBuffer()
{
    for(int i = 0; i < MSG_LENGTH_MAX; i++) {    
        bluetoothMsg[i] = 0; 
    }
    bluetoothMsgLen = 0;
}

void flushTerminalMsgBuffer()
{
    for(int i = 0; i < MSG_LENGTH_MAX; i++) {    
        terminalMsg[i] = 0; 
    }
    terminalMsgLen = 0;
}

void resetBluetoothState()
{
    bluetoothState = RECEIVING_FLAG;
}

void resetTerminalState()
{
    terminalState = WAITING_FOR_START;
}

void loop()
{    
    bluetooth.process();
    
    if(bluetooth.isConnected()) {
        if(!lastConnectionState)
        {
            printConnectedMessage();
            lastConnectionState = true;
        }
        
        flushIncomingMsgBuffer();
        byte msgLen = tryToReadBluetoothMsgInto(incomingMsgBuf);
	if(msgLen > 0)
        {
            for(int i = 0; i < msgLen; i++)
            {
                byte msg = incomingMsgBuf[i];
                runBluetoothStateMachine((char)msg);
            }
        }
        
        flushIncomingMsgBuffer();
        msgLen = tryToReadTerminalMsgInto(incomingMsgBuf);
        if(msgLen > 0)
        {
            for(int i = 0; i < msgLen; i++)
            {
                byte msg = incomingMsgBuf[i];
                runTerminalStateMachine((char)msg);
            }
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
    Serial.print("Connected! Start scanning!\n\n\r");
}

void printDisconnectedMessage()
{
    Serial.print("\n\rDisconnected! Halting communications...");
    
    flushBuffersAndResetStates();
    
    Serial.print("\n\rWaiting for Bluetooth connection...\n\r");
}

byte tryToReadBluetoothMsgInto(byte msgBuf[])
{
    byte msgLen = bluetooth.bytesAvailable();
    
    for(int i = 0; i < msgLen && i < MSG_LENGTH_MAX; i++)
    {
        msgBuf[i] = bluetooth.readByte();
    }
   
    return msgLen; 
}

byte tryToReadTerminalMsgInto(byte msgBuf[])
{
    byte msgLen = Serial1.available();
    
    for(int i = 0; i < msgLen && i < MSG_LENGTH_MAX; i++)
    {
        msgBuf[i] = Serial1.read();
    }
   
    return msgLen; 
}

void runBluetoothStateMachine(char letter)
{
    switch(bluetoothState)
    {
        case RECEIVING_FLAG:
            if(isLetterStringFlag(letter))
            {
                bluetoothState = RECEIVING_STRING_CHAR;
            }
            else if(isLetterEndFlag(letter))
            {
                bluetoothState = RECEIVING_END_CODE;
            }
            break;
        case RECEIVING_STRING_CHAR:
            if(!isLetterEndCode(letter))
            {
                resetBluetoothState();
                appendLetterOnBluetoothMsg(letter);
            }
            else
            {
                stopAndSendBluetoothMsg();
            }
            break;
        case RECEIVING_END_CODE:
            if(isLetterEndCode(letter))
            {
                stopAndSendBluetoothMsg();
            }
            else
            {
                resetBluetoothState();
                Serial.println("WARNING: Received null flag, but did not receive null value. Continuing string read.");
            }
        default:
          resetBluetoothState();
          break;
    }
}

boolean isLetterStringFlag(char letter)
{
    return (letter == 'S');
}

boolean isLetterEndFlag(char letter)
{
    return (letter == 'N');
}

boolean isLetterEndCode(char letter)
{
    byte charNum = (byte) letter;
    return (charNum == STRING_END); 
}

void appendLetterOnBluetoothMsg(char letter)
{
    if(bluetoothMsgLen < MSG_LENGTH_MAX)
    {
        bluetoothMsg[bluetoothMsgLen] = letter;
        bluetoothMsgLen++;
    }
}

void stopAndSendBluetoothMsg()
{
    Serial.println("Received string from Bluetooth connection, ignoring for the time being");
    
    flushBluetoothMsgBuffer();
    resetBluetoothState();
    
//    sendBluetoothMsgToTerminal();
}

//void sendBluetoothMsgToTerminal()
//{
//    Serial1.print("Connected device: ");
//    
//    for(int i = 0; i < bluetoothMsgLen; i++)
//    {
//        Serial1.print(bluetoothMsg[i]);
//    }
//  
//    Serial1.print("\n\r");
//}

void runTerminalStateMachine(char letter)
{
    switch(terminalState)
    {
        case WAITING_FOR_START:
            if((byte)letter == CARD_START)
            {
                terminalState = RECEIVING_CHARS;   
            }
            break;
        case RECEIVING_CHARS:
            if((byte)letter == RETURN_CHAR)
            {
                stopAndSendTerminalMsg(); 
            } else {
                appendLetterOnTerminalMsg(letter);
            }
            break;
        default:
            resetTerminalState();
            break;   
    }
}

void appendLetterOnTerminalMsg(char letter)
{
    if(terminalMsgLen < MSG_LENGTH_MAX)
    {
        terminalMsg[terminalMsgLen] = letter;
        terminalMsgLen++;
    }
}

void stopAndSendTerminalMsg()
{
    Serial.print("Received card data: ");
    for(int i = 0; i < terminalMsgLen-1; i++) {
        Serial.print(terminalMsg[i]);
    }
    Serial.print(". Sending it over Bluetooth...\r\n");
    
    sendTerminalMsgToBluetooth();
    flushTerminalMsgBuffer();
    resetTerminalState(); 
}

void sendTerminalMsgToBluetooth()
{
    for(int i = 0; i < terminalMsgLen; i++) {
        byte encrypted = (byte)terminalMsg[i] ^ XOR_VAL;
        bluetooth.sendByteWithFlag('S', encrypted);
    }
    
    bluetooth.sendByteWithFlag('N', (byte)0);
}
