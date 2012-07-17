#include <SoftwareSerial.h>
#include <Bluetooth.h>
#include <ArduinoUnit.h>

#define TESTING 0

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

SoftwareSerial bluetoothSerial(RX, TX);
Bluetooth bluetooth("AndroidArduinoBTRS232", bluetoothSerial, true);

int bluetoothState;
int terminalState;

boolean lastConnectionState; //True for connected and false for disconnected

byte incomingMsgBuf[MSG_LENGTH_MAX];

int bluetoothMsgLen;
char bluetoothMsg[MSG_LENGTH_MAX];

int terminalMsgLen;
char terminalMsg[MSG_LENGTH_MAX];

HardwareSerial* logSerial;
HardwareSerial* cardSerial;

#if TESTING == 0
void setup()
{
    logSerial = &Serial;
    cardSerial = &Serial1;
    
    setUpIO();
    logSerial->begin(115200);

    flushBuffersAndResetStates();

    logSerial->print("Initializing card reader serial port...\n\r");    
    cardSerial->begin(9600);
    
    if(!bluetooth.beginBluetooth())
    {
        logSerial->println("\n\n\rHalting program...");
        while(true) { }
    }
    
    logSerial->print("\n\rWaiting for Bluetooth connection...\n\r");
}
#endif

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
    terminalState = WAITING_FOR_START;
}

#if TESTING == 0
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
        int msgLen = tryToReadTerminalMsgInto(incomingMsgBuf);
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
#endif

void printConnectedMessage()
{
    logSerial->print("Connected! Start scanning!\n\n\r");
}

void printDisconnectedMessage()
{
    logSerial->print("\n\rDisconnected! Halting communications...");
    
    flushBuffersAndResetStates();
    
    logSerial->print("\n\rWaiting for Bluetooth connection...\n\r");
}

byte tryToReadTerminalMsgInto(byte msgBuf[])
{
    byte msgLen = cardSerial->available();
    
    for(int i = 0; i < msgLen && i < MSG_LENGTH_MAX; i++)
    {
        msgBuf[i] = cardSerial->read();
    }
   
    return msgLen; 
}

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
    logSerial->print("Received card data: ");
    for(int i = 0; i < terminalMsgLen-1; i++) {
        logSerial->print(terminalMsg[i]);
    }
    logSerial->print(". Sending it over Bluetooth...\r\n");
    
    sendTerminalMsgToBluetooth();
    flushTerminalMsgBuffer();
    resetTerminalState(); 
}

void sendTerminalMsgToBluetooth()
{
    for(int i = 0; i < terminalMsgLen; i++) {
        byte encrypted = (byte)terminalMsg[i] ^ XOR_VAL;
        bluetooth.sendByteWithFlag('S' ^ XOR_VAL, encrypted);
    }
    
    bluetooth.sendByteWithFlag('N' ^ XOR_VAL, (byte)0 ^ XOR_VAL);
}

//////////////////
// UNIT TESTING //
//////////////////

#if TESTING == 1

#include "MockSerial.h"

#define LIGHT_SUCCESS 8
#define LIGHT_RUNNING 9
#define LIGHT_FAILED  10

MockSerial mockSerial = MockSerial();

TestSuite suite;

void setup() {
    logSerial = &mockSerial;
    setUpStatusLightsIO();
}

void setUpStatusLightsIO() {
    pinMode(LIGHT_SUCCESS, OUTPUT);
    pinMode(LIGHT_RUNNING, OUTPUT);
    pinMode(LIGHT_FAILED, OUTPUT); 
}

//The tests

test(doesFlushAndResetWork) {
    fillBuffer(incomingMsgBuf);
    fillBuffer((byte*)terminalMsg);
    fillBuffer((byte*)bluetoothMsg);
    terminalState = 17;
    bluetoothState = 17;
    
    flushBuffersAndResetStates();
    
    assertTrue(isBufferEmpty(incomingMsgBuf));
    assertTrue(isBufferEmpty((byte*)terminalMsg));
    assertTrue(isBufferEmpty((byte*)bluetoothMsg));
    
    assertEquals(bluetoothState, RECEIVING_FLAG);
    assertEquals(terminalState, WAITING_FOR_START);
}

void fillBuffer(byte buffer[]) {
    for(int i = 0; i < MSG_LENGTH_MAX; i++) {
        buffer[i] = i;   
    }
}

boolean isBufferEmpty(byte buffer[]) {
    boolean isEmpty = true;
    
    for(int i = 0; i < MSG_LENGTH_MAX; i++)
    {
        if(buffer[i] != 0) {
            isEmpty = false;
        }
    }
    
    return isEmpty;
}

test(printConnectedMessageIsCorrect) {
    printConnectedMessage();
    
    String correctMsg = "Connected! Start scanning!\n\n\r";
    String sentMsg = (char*)mockSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockSerial.reset();   
}

test(printDisconnectedMessageIsCorrect) {
    printDisconnectedMessage();
    
    String correctMsg = "\n\rDisconnected! Halting communications...\n\rWaiting for Bluetooth connection...\n\r";
    String sentMsg = (char*)mockSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockSerial.reset();
}

test(printDisconnectedMessageFlushesAndResets) {
    fillBuffer(incomingMsgBuf);
    fillBuffer((byte*)terminalMsg);
    fillBuffer((byte*)bluetoothMsg);
    terminalState = 17;
    bluetoothState = 17;
    
    printDisconnectedMessage();
    
    assertTrue(isBufferEmpty(incomingMsgBuf));
    assertTrue(isBufferEmpty((byte*)terminalMsg));
    assertTrue(isBufferEmpty((byte*)bluetoothMsg));
    
    assertEquals(terminalState, WAITING_FOR_START);
    assertEquals(bluetoothState, RECEIVING_FLAG);
}

//Running the tests and updating the status lights

void loop()
{
    if(!suite.hasCompleted()) {
        setStatusLightsToRunning();
    }
    
    suite.run();
    
    if(suite.hasCompleted()) {
        if(suite.getFailureCount() > 0) {
            setStatusLightsToFailed();
        } else {
            setStatusLightsToSuccess();
        }
    }
}

void setStatusLightsToRunning() {
    digitalWrite(LIGHT_SUCCESS, LOW);
    digitalWrite(LIGHT_RUNNING, HIGH);
    digitalWrite(LIGHT_FAILED, LOW);
}

void setStatusLightsToSuccess() {
    digitalWrite(LIGHT_SUCCESS, HIGH);
    digitalWrite(LIGHT_RUNNING, LOW);
    digitalWrite(LIGHT_FAILED, LOW);
}

void setStatusLightsToFailed() {
    digitalWrite(LIGHT_SUCCESS, LOW);
    digitalWrite(LIGHT_RUNNING, LOW);
    digitalWrite(LIGHT_FAILED, HIGH);
}

#endif
