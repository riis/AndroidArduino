#include <SoftwareSerial.h>
#include <Bluetooth.h>

#define STRING_END 0
#define RETURN_CHAR 13
#define MSG_LENGTH_MAX 256

//BluetoothStates for the Bluetooth BluetoothState machine
#define RECEIVING_FLAG        1
#define RECEIVING_STRING_CHAR 2
#define RECEIVING_END_CODE    3

//BluetoothStates for the terminal BluetoothState machine
#define RECEIVING_CHARS 1

//RX and TX pin numbers
#define RX 11
#define TX 3

//SoftwareSerial bluetoothSerial(RX, TX);

Bluetooth bluetooth(RX, TX, "AndroidArdinoRS232", true);

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

    setUpIO();
    flushBuffersAndResetStates();
    
    if(!bluetooth.beginBluetooth())
    {
        while(true) { }
    }
    
    Serial.println("Bluetooth device ready, connect to \"AndroidArduinoBTRS232\".");

    Serial.println("Initializing barcode scanner serial port...");    
    Serial1.begin(9600);
    
    Serial.print("Waiting for Bluetooth connection...\n\r");
}

void setUpIO()
{    
    pinMode(RX, INPUT);
    pinMode(TX, OUTPUT);
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
    terminalState = RECEIVING_CHARS;
}

void loop()
{    
    //TODO: FIGURE OUT BT CONNECTION STATE STUFF
    
    if(true) {//ISBTCONNECTED) {
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
    Serial.print("\n\rConnected! Communications ready on the terminal\n\n\r");
}

void printDisconnectedMessage()
{
    Serial.print("Disconnected! Halting communications...");
    
    flushBuffersAndResetStates();
    
    Serial.print("\n\rWaiting for Bluetooth connection...\n\r");
}

byte tryToReadBluetoothMsgInto(byte msgBuf[])
{
    byte msgLen = bluetoothSerial.available();
    
    for(int i = 0; i < msgLen && i < MSG_LENGTH_MAX; i++)
    {
        msgBuf[i] = bluetoothSerial.read();
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
    Serial.println("Received string from Bluetooth connection, printing it to terminal");
    
    sendBluetoothMsgToTerminal();
    
    flushBluetoothMsgBuffer();
    resetBluetoothState();
}

void sendBluetoothMsgToTerminal()
{
//    Serial1.print("Connected device: ");
//    
//    for(int i = 0; i < bluetoothMsgLen; i++)
//    {
//        Serial1.print(bluetoothMsg[i]);
//    }
//  
//    Serial1.print("\n\r");
}

void runTerminalStateMachine(char letter)
{
    switch(terminalState)
    {
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
    Serial.println("Received string from terminal, sending it across Bluetooth");
    
    sendTerminalMsgToBluetooth();
    flushTerminalMsgBuffer();
    resetTerminalState(); 
}

void sendTerminalMsgToBluetooth()
{
    bluetoothSerial.write((byte)'q');
    
    for(int i = 0; i < terminalMsgLen; i++) {
        char thingy = terminalMsg[i];
        if(thingy != 'T') {
            bluetoothSerial.write((byte)'S');
            bluetoothSerial.write((byte)thingy);
        }
    }
    
    bluetoothSerial.write((byte)'N'); 
    bluetoothSerial.write((byte)0);
}
