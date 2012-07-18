#include <SoftwareSerial.h>
#include <Bluetooth.h>

#define TESTING 0

#if TESTING == 1
    #include <Mocks.h>
    #include <ArduinoUnit.h>
#endif

//Encryption defines
#if TESTING == 0
    #define XOR_VAL 55
#else
    #define XOR_VAL 0
#endif

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
#include "MockSoftwareSerial.h"

#define LIGHT_SUCCESS 8
#define LIGHT_RUNNING 9
#define LIGHT_FAILED  10

MockSerial mockLogSerial = MockSerial();
MockSerial mockCardSerial = MockSerial();
MockSoftwareSerial mockBluetoothSerial = MockSoftwareSerial();

TestSuite suite;

void setup() {
    logSerial = &mockLogSerial;
    cardSerial = &mockCardSerial;
    bluetooth = Bluetooth("AndroidArduinoBTRS232", mockBluetoothSerial, false);
    
    setUpStatusLightsIO();
}

void setUpStatusLightsIO() {
    pinMode(LIGHT_SUCCESS, OUTPUT);
    pinMode(LIGHT_RUNNING, OUTPUT);
    pinMode(LIGHT_FAILED, OUTPUT); 
}

//Testing utility functions

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

boolean areByteArraysEqual(byte buffer1[], byte buffer2[], int len) {
    boolean areEqual = true;
 
    for(int i = 0; i < len; i++) {
        if(buffer1[i] != buffer2[i]) {
            areEqual = false;
        }   
    }
 
    return areEqual;   
}

//The tests

test(canFlushAndResetFlush) {
    fillBuffer(incomingMsgBuf);
    fillBuffer((byte*)terminalMsg);
    fillBuffer((byte*)bluetoothMsg);
    
    flushBuffersAndResetStates();
    
    assertTrue(isBufferEmpty(incomingMsgBuf));
    assertTrue(isBufferEmpty((byte*)terminalMsg));
    assertTrue(isBufferEmpty((byte*)bluetoothMsg));
}

test(canFlushAndResetReset) {
    terminalState = 17; //Set to bogus states
    bluetoothState = 17;
    
    flushBuffersAndResetStates();
    
    assertEquals(bluetoothState, RECEIVING_FLAG);
    assertEquals(terminalState, WAITING_FOR_START);
}

test(canFlushAndResetResetMsgLens) {
    terminalMsgLen = 42;
    bluetoothMsgLen = 42;
    
    flushBuffersAndResetStates();
    
    assertEquals(terminalMsgLen, 0);
    assertEquals(bluetoothMsgLen, 0)
}

test(canPrintConnectedMsgCorrectly) {
    printConnectedMessage();
    
    String correctMsg = "Connected! Start scanning!\n\n\r";
    String sentMsg = (char*)mockLogSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockLogSerial.reset();   
}

test(canPrintDisconnectedMsgCorrectly) {
    printDisconnectedMessage();
    
    String correctMsg = "\n\rDisconnected! Halting communications...\n\rWaiting for Bluetooth connection...\n\r";
    String sentMsg = (char*)mockLogSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockLogSerial.reset();
}

test(canFlushAndResetAfterPrintingDisconnectedMsg) {
    fillBuffer(incomingMsgBuf);
    fillBuffer((byte*)terminalMsg);
    fillBuffer((byte*)bluetoothMsg);
    terminalState = 17;
    bluetoothState = 17;
    
    printDisconnectedMessage();
    
    assertTrue(isBufferEmpty(incomingMsgBuf));
    assertTrue(isBufferEmpty((byte*)terminalMsg));
    assertTrue(isBufferEmpty((byte*)bluetoothMsg));
    
    assertEquals(terminalMsgLen, 0);
    assertEquals(bluetoothMsgLen, 0);
    
    assertEquals(terminalState, WAITING_FOR_START);
    assertEquals(bluetoothState, RECEIVING_FLAG);
    
    mockLogSerial.reset();
}

test(canReadTerminalMsg) {
    char incomingMessage[5] = {'h', 'e', 'l', 'l', 'o'};
    
    mockCardSerial.set_input_buffer((byte*)incomingMessage, 5);
    
    byte testBuffer[5];
    int charsRead = tryToReadTerminalMsgInto(testBuffer);
    
    assertEquals(charsRead, 5);
    assertTrue(areByteArraysEqual((byte*)incomingMessage, testBuffer, 5));
    
    mockCardSerial.reset();
}

test(canReadBlankTerminalMsg) {
    char incomingMessage[0] = { };
    
    mockCardSerial.set_input_buffer((byte*)incomingMessage, 0);
    
    byte testBuffer[0];
    int charsRead = tryToReadTerminalMsgInto(testBuffer);
    
    assertEquals(charsRead, 0);
    assertTrue(areByteArraysEqual((byte*)incomingMessage, testBuffer, 0));
    
    mockCardSerial.reset();
}

test(canAppendLetterOnTerminalMsg) {
    flushTerminalMsgBuffer();
    
    appendLetterOnTerminalMsg('a');
    
    assertEquals(terminalMsg[0], 'a');
}

test(canIncrementTerminalMsgLenWhenAppending) {
    flushTerminalMsgBuffer();
    
    appendLetterOnTerminalMsg('a');
    assertEquals(terminalMsgLen, 1);
    
    appendLetterOnTerminalMsg('b');
    assertEquals(terminalMsgLen, 2);
}

test(canNotAffectTerminalMsgWhenFull) {
    flushTerminalMsgBuffer();
    
    byte oldBuffer[MSG_LENGTH_MAX];
    
    fillBuffer(oldBuffer);
    fillBuffer((byte*)terminalMsg);
    terminalMsgLen = MSG_LENGTH_MAX;
    
    appendLetterOnTerminalMsg('a');
    
    assertTrue(areByteArraysEqual(oldBuffer, (byte*)terminalMsg, MSG_LENGTH_MAX));
}

test(canNotIncrementTerminalMsgLenWhenFull) {
    flushTerminalMsgBuffer();
    
    byte oldBuffer[MSG_LENGTH_MAX];
    
    fillBuffer(oldBuffer);
    fillBuffer((byte*)terminalMsg);
    terminalMsgLen = MSG_LENGTH_MAX;
    
    appendLetterOnTerminalMsg('a');
    
    assertEquals(terminalMsgLen, MSG_LENGTH_MAX);
}

test(canSendMessageToBluetoothSerial) {
    terminalMsg[0] = 'h';
    terminalMsg[1] = 'e';
    terminalMsg[2] = 'l';
    terminalMsg[3] = 'l';
    terminalMsg[4] = 'o';
    terminalMsgLen = 5;
    
    sendTerminalMsgToBluetooth();
    
    String correctMsg = "ShSeSlSlSoN\0";
    String sentMsg = (char*)mockBluetoothSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockBluetoothSerial.reset();   
}

test(canStopAndSendOutputCorrectLogMsg) {
    terminalMsg[0] = 'h';
    terminalMsg[1] = 'e';
    terminalMsg[2] = 'l';
    terminalMsg[3] = 'l';
    terminalMsg[4] = 'o';
    terminalMsgLen = 5;
    
    stopAndSendTerminalMsg();
    
    String correctMsg = "Received card data: hell. Sending it over Bluetooth...\r\n";
    String sentMsg = (char*)mockLogSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockBluetoothSerial.reset();
    mockLogSerial.reset();
}

test(canStopAndSendSendTerminalMsgToBluetooth) {
    terminalMsg[0] = 'h';
    terminalMsg[1] = 'e';
    terminalMsg[2] = 'l';
    terminalMsg[3] = 'l';
    terminalMsg[4] = 'o';
    terminalMsgLen = 5;
    
    stopAndSendTerminalMsg();
    
    String correctMsg = "ShSeSlSlSoN\0";
    String sentMsg = (char*)mockBluetoothSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockBluetoothSerial.reset();
    mockLogSerial.reset();
}

test(canStopAndSendFlushMsgBuffer) {
    terminalMsg[0] = 'h';
    terminalMsg[1] = 'e';
    terminalMsg[2] = 'l';
    terminalMsg[3] = 'l';
    terminalMsg[4] = 'o';
    terminalMsgLen = 5;
    
    stopAndSendTerminalMsg();
    
    assertEquals(terminalMsgLen, 0);
    assertTrue(isBufferEmpty((byte*)terminalMsg));
    
    mockBluetoothSerial.reset();
    mockLogSerial.reset();
}

test(canStopAndSendResetState) {
    terminalMsg[0] = 'h';
    terminalMsg[1] = 'e';
    terminalMsg[2] = 'l';
    terminalMsg[3] = 'l';
    terminalMsg[4] = 'o';
    terminalMsgLen = 5;
    
    terminalState = 42; //Bogus state
    
    stopAndSendTerminalMsg();
    
    assertEquals(terminalState, WAITING_FOR_START);
    
    mockBluetoothSerial.reset();
    mockLogSerial.reset();
}

test(canStayInWaitState) {
    flushBuffersAndResetStates();
    
    runTerminalStateMachine('g'); //Bogus letter
    
    assertEquals(terminalState, WAITING_FOR_START);
}

test(canAdvanceToReceivingState) {
    flushBuffersAndResetStates();
    
    runTerminalStateMachine(CARD_START);
    
    assertEquals(terminalState, RECEIVING_CHARS);
}

test(canAppendCharactersToMessageInReceivingState) {
    flushBuffersAndResetStates();
    
    runTerminalStateMachine(CARD_START);
    runTerminalStateMachine('a');
    
    assertEquals(terminalMsg[0], 'a');
    assertEquals(terminalMsgLen, 1);
}

test(canReturnToWaitStateAfterReceivingState) {
    flushBuffersAndResetStates();
    
    runTerminalStateMachine(CARD_START);
    runTerminalStateMachine(RETURN_CHAR);
    
    assertEquals(terminalState, WAITING_FOR_START);
    
    mockBluetoothSerial.reset();
    mockLogSerial.reset();
}

test(canResetStateIfStateIsBogus) {
    flushBuffersAndResetStates();
    
    terminalState = 42;
    runTerminalStateMachine('a');
    
    assertEquals(terminalState, WAITING_FOR_START);
}

test(canSendMessageAfterReceivingState) {
    flushBuffersAndResetStates();
    
    terminalMsg[0] = 'h';
    terminalMsg[1] = 'e';
    terminalMsg[2] = 'l';
    terminalMsg[3] = 'l';
    terminalMsg[4] = 'o';
    terminalMsgLen = 5;
    
    runTerminalStateMachine(CARD_START);
    runTerminalStateMachine(RETURN_CHAR);
    
    String correctMsg = "ShSeSlSlSoN\0";
    String sentMsg = (char*)mockBluetoothSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockBluetoothSerial.reset();
    mockLogSerial.reset();
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
