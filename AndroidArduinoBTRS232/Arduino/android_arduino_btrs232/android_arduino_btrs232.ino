#include <SoftwareSerial.h>
#include <Bluetooth.h>

#define TESTING 2

#define STRING_END 255
#define RETURN_CHAR 13
#define MSG_LENGTH_MAX 256

//BluetoothStates for the Bluetooth state machine
#define RECEIVING_FLAG        1
#define RECEIVING_STRING_CHAR 2
#define RECEIVING_END_CODE    3

//BluetoothStates for the terminal state machine
#define RECEIVING_CHARS 1

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
HardwareSerial* terminalSerial;

#if TESTING == 0
void setup()
{
    logSerial = &Serial;
    terminalSerial = &Serial1;
    
    setUpIO();
    logSerial->begin(115200); 
    
    flushBuffersAndResetStates();   
    
    logSerial->println("Initializing terminal serial port...");    
    terminalSerial->begin(9600);
    terminalSerial->print('\f');

    if(!bluetooth.beginBluetooth())
    {
        logSerial->println("\n\n\rHalting program...");
        while(true) { }
    }
    
    logSerial->print("Waiting for Bluetooth connection...\n\r");
    terminalSerial->print("Waiting for Bluetooth connection...\n\r");
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
    terminalState = RECEIVING_CHARS;
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
            echoTerminalMessage(incomingMsgBuf, msgLen);
            
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
    logSerial->print("\n\rConnected! Communications ready on the terminal\n\n\r");
    
    terminalSerial->print('\f');
    terminalSerial->print("Bluetooth connected!, monitoring for messages\n\r");
    terminalSerial->print("To send messages, type a line, then press enter.\n\n\r");
}

void printDisconnectedMessage()
{
    logSerial->print("Disconnected! Halting communications...");
 
    terminalSerial->print('\f');
    terminalSerial->print("The Bluetooth connection was lost!");
    terminalSerial->print("\n\rWaiting for Bluetooth connection...");
    
    flushBuffersAndResetStates();
    
    logSerial->print("\n\rWaiting for Bluetooth connection...\n\r");
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
    byte msgLen = terminalSerial->available();
    
    for(int i = 0; i < msgLen && i < MSG_LENGTH_MAX; i++)
    {
        msgBuf[i] = terminalSerial->read();
    }
   
    return msgLen; 
}

void echoTerminalMessage(byte msgBuf[], byte msgLen)
{
    for(int i = 0; i < msgLen; i++)
    {
        if((char)msgBuf[i] == '\r')
        {
             terminalSerial->print('\n');
        }
        
        terminalSerial->print((char)msgBuf[i]);
    }
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
                logSerial->println("WARNING: Received null flag, but did not receive null value. Continuing string read.");
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
    logSerial->println("Received string from Bluetooth connection, printing it to terminal");
    
    sendBluetoothMsgToTerminal();
    
    flushBluetoothMsgBuffer();
    resetBluetoothState();
}

void sendBluetoothMsgToTerminal()
{
    terminalSerial->print("Connected device: ");
    
    for(int i = 0; i < bluetoothMsgLen; i++)
    {
        terminalSerial->print(bluetoothMsg[i]);
    }
  
    terminalSerial->print("\n\r");
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
    logSerial->print("Received string from terminal, sending it across Bluetooth\r\n");
    
    sendTerminalMsgToBluetooth();
    flushTerminalMsgBuffer();
    resetTerminalState(); 
}

void sendTerminalMsgToBluetooth()
{
    for(int i = 0; i < terminalMsgLen; i++) {
        bluetooth.sendByteWithFlag('S', (byte)terminalMsg[i]);
    }
    
    bluetooth.sendByteWithFlag('N', 255);
}

//////////////////
// UNIT TESTING //
//////////////////

#if TESTING == 1 || TESTING == 2

#include <ArduinoUnit.h>
#include <Mocks.h>
#include <MemoryFree.h>

#define LIGHT_SUCCESS 8
#define LIGHT_RUNNING 9
#define LIGHT_FAILED  10

MockSerial mockLogSerial = MockSerial();
MockSerial mockTerminalSerial = MockSerial();
MockSoftwareSerial mockBluetoothSerial = MockSoftwareSerial();

TestSuite suite;

int memoryReportCount = 0;

//Some global variables so memory doesn't get out of hand
String correctMsg = "";
String sentMsg = "";
byte* helloBytes = (byte*)"hello";
char* helloChars = "hello";

void setup() {
    logSerial = &mockLogSerial;
    terminalSerial = &mockTerminalSerial;
    bluetooth = Bluetooth("AndroidArduinoBTRS232", mockBluetoothSerial, false);
    
    setUpStatusLightsIO();
}

void setUpStatusLightsIO() {
    pinMode(LIGHT_SUCCESS, OUTPUT);
    pinMode(LIGHT_RUNNING, OUTPUT);
    pinMode(LIGHT_FAILED, OUTPUT); 
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

//Testing utility functions

void reportFreeMemory() {
    memoryReportCount++;
    Serial.print(memoryReportCount);
    Serial.print(" Free Memory: ");
    Serial.println(freeMemory());
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

boolean areByteArraysEqual(byte buffer1[], byte buffer2[], int len) {
    boolean areEqual = true;
 
    for(int i = 0; i < len; i++) {
        if(buffer1[i] != buffer2[i]) {
            areEqual = false;
        }   
    }
 
    return areEqual;   
}
#endif

//The tests

#if TESTING == 1
test(canFlushAndResetFlush) {
    reportFreeMemory();
    
    fillBuffer(incomingMsgBuf);
    fillBuffer((byte*)terminalMsg);
    fillBuffer((byte*)bluetoothMsg);
    
    flushBuffersAndResetStates();
    
    assertTrue(isBufferEmpty(incomingMsgBuf));
    assertTrue(isBufferEmpty((byte*)terminalMsg));
    assertTrue(isBufferEmpty((byte*)bluetoothMsg));
}

test(canFlushAndResetReset) {
    reportFreeMemory();
    
    terminalState = 17; //Set to bogus states
    bluetoothState = 17;
    
    flushBuffersAndResetStates();
    
    assertEquals(bluetoothState, RECEIVING_FLAG);
    assertEquals(terminalState, RECEIVING_CHARS);
}

test(canFlushAndResetResetMsgLens) {
    reportFreeMemory();
    
    terminalMsgLen = 42;
    bluetoothMsgLen = 42;
    
    flushBuffersAndResetStates();
    
    assertEquals(terminalMsgLen, 0);
    assertEquals(bluetoothMsgLen, 0)
}

test(canPrintConnectedMsgCorrectly) {
    reportFreeMemory();
    
    printConnectedMessage();
    
    String correctMsg = "\n\rConnected! Communications ready on the terminal\n\n\r";
    String sentMsg = mockLogSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockLogSerial.reset();
}

test(canPrintDisconnectedMsgCorrectly) {
    reportFreeMemory();
    
    printDisconnectedMessage();
    
    correctMsg = "Disconnected! Halting communications...\n\rWaiting for Bluetooth connection...\n\r";
    sentMsg = mockLogSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockLogSerial.reset();
}

test(canFlushAndResetAfterPrintingDisconnectedMsg) {
    reportFreeMemory();
    
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
    
    assertEquals(terminalState, RECEIVING_CHARS);
    assertEquals(bluetoothState, RECEIVING_FLAG);
    
    mockLogSerial.reset();
}

test(canReadBluetoothMsg) {
    reportFreeMemory();
    
    mockBluetoothSerial.set_input_buffer("helloo");
    bluetooth.process();
    
    byte testBuffer[6];
    int charsRead = tryToReadBluetoothMsgInto(testBuffer);
    
    assertEquals(charsRead, 6);
    assertTrue(areByteArraysEqual((byte*)"helloo", testBuffer, 6));
    
    mockBluetoothSerial.reset();
}

test(canReadTerminalMsg) {
    reportFreeMemory();
    
    mockTerminalSerial.set_input_buffer(helloChars);
    
    byte testBuffer[5];
    int charsRead = tryToReadTerminalMsgInto(testBuffer);
    
    assertEquals(charsRead, 5);
    assertTrue(areByteArraysEqual(helloBytes, testBuffer, 5));
    
    mockTerminalSerial.reset();
}

test(canReadBlankTerminalMsg) {
    reportFreeMemory();
    
    mockTerminalSerial.set_input_buffer("");
    
    byte testBuffer[0];
    int charsRead = tryToReadTerminalMsgInto(testBuffer);
    
    assertEquals(charsRead, 0);
    assertTrue(areByteArraysEqual((byte*)"", testBuffer, 0));
    
    mockTerminalSerial.reset();
}

test(canAppendLetterOnTerminalMsg) {
    reportFreeMemory();
    
    flushTerminalMsgBuffer();
    
    appendLetterOnTerminalMsg('a');
    
    assertEquals(terminalMsg[0], 'a');
}

test(canIncrementTerminalMsgLenWhenAppending) {
    reportFreeMemory();
    
    flushTerminalMsgBuffer();
    
    appendLetterOnTerminalMsg('a');
    assertEquals(terminalMsgLen, 1);
    
    appendLetterOnTerminalMsg('b');
    assertEquals(terminalMsgLen, 2);
}

test(canNotAffectTerminalMsgWhenFull) {
    reportFreeMemory();
    
    flushTerminalMsgBuffer();
    
    byte oldBuffer[MSG_LENGTH_MAX];
    
    fillBuffer(oldBuffer);
    fillBuffer((byte*)terminalMsg);
    terminalMsgLen = MSG_LENGTH_MAX;
    
    appendLetterOnTerminalMsg('a');
    
    assertTrue(areByteArraysEqual(oldBuffer, (byte*)terminalMsg, MSG_LENGTH_MAX));
}

test(canNotIncrementTerminalMsgLenWhenFull) {
    reportFreeMemory();
    
    flushTerminalMsgBuffer();
    
    byte oldBuffer[MSG_LENGTH_MAX];
    
    fillBuffer(oldBuffer);
    fillBuffer((byte*)terminalMsg);
    terminalMsgLen = MSG_LENGTH_MAX;
    
    appendLetterOnTerminalMsg('a');
    
    assertEquals(terminalMsgLen, MSG_LENGTH_MAX);
}

test(canEchoTerminalMsg) {
    reportFreeMemory();
    
    echoTerminalMessage(helloBytes, 5);
    
    correctMsg = helloChars;
    sentMsg = mockTerminalSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockTerminalSerial.reset();
}

test(canDetectLetterStringFlag) {
    reportFreeMemory();
    
    assertTrue(isLetterStringFlag('S'));   
}

test(canDetectNotLetterStringFlag) {
    reportFreeMemory();
    
    assertTrue(!isLetterStringFlag('g'));   
}

test(canDetectLetterEndFlag) {
    reportFreeMemory();
    
    assertTrue(isLetterEndFlag('N'));   
}

test(canDetectNotLetterEndFlag) {
    reportFreeMemory();
    
    assertTrue(!isLetterEndFlag('g'));   
}

test(canDetectLetterEndCode) {
    
    
    assertTrue(isLetterEndCode((char)255));   
}

test(canDetectNotLetterEndCode) {
    reportFreeMemory();
    
    assertTrue(!isLetterEndCode('g'));   
}

test(canAppendLetterOnBluetoothMsg) {
    reportFreeMemory();
    
    flushBluetoothMsgBuffer();
    
    appendLetterOnBluetoothMsg('a');
    
    assertEquals(bluetoothMsg[0], 'a');
}

test(canIncrementBluetoothMsgLenWhenAppending) {
    reportFreeMemory();
    
    flushBluetoothMsgBuffer();
    
    appendLetterOnBluetoothMsg('a');
    assertEquals(bluetoothMsgLen, 1);
    
    appendLetterOnBluetoothMsg('b');
    assertEquals(bluetoothMsgLen, 2);
}

test(canNotAffectBluetoothMsgWhenFull) {
    reportFreeMemory();
    
    flushBluetoothMsgBuffer();
    
    byte oldBuffer[MSG_LENGTH_MAX];
    
    fillBuffer(oldBuffer);
    fillBuffer((byte*)bluetoothMsg);
    bluetoothMsgLen = MSG_LENGTH_MAX;
    
    appendLetterOnBluetoothMsg('a');
    
    assertTrue(areByteArraysEqual(oldBuffer, (byte*)bluetoothMsg, MSG_LENGTH_MAX));
}

test(canNotIncrementBluetoothMsgLenWhenFull) {
    reportFreeMemory();
    
    flushBluetoothMsgBuffer();
    
    byte oldBuffer[MSG_LENGTH_MAX];
    
    fillBuffer(oldBuffer);
    fillBuffer((byte*)bluetoothMsg);
    bluetoothMsgLen = MSG_LENGTH_MAX;
    
    appendLetterOnBluetoothMsg('a');
    
    assertEquals(bluetoothMsgLen, MSG_LENGTH_MAX);
}

test(canSendMessageToTerminalSerial) {
    reportFreeMemory();
    
    bluetoothMsg[0] = helloChars[0];
    bluetoothMsg[1] = helloChars[1];
    bluetoothMsg[2] = helloChars[2];
    bluetoothMsg[3] = helloChars[3];
    bluetoothMsg[4] = helloChars[4];
    bluetoothMsgLen = 5;
    
    sendBluetoothMsgToTerminal();
    
    correctMsg = "Connected device: hello\n\r";
    sentMsg = mockTerminalSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockTerminalSerial.reset();   
    mockLogSerial.reset();
}

test(canStopAndSendBluetoothOutputCorrectLogMsg) {
    reportFreeMemory();
    bluetoothMsg[0] = helloChars[0];
    bluetoothMsg[1] = helloChars[1];
    bluetoothMsg[2] = helloChars[2];
    bluetoothMsg[3] = helloChars[3];
    bluetoothMsg[4] = helloChars[4];
    bluetoothMsgLen = 5;

    stopAndSendBluetoothMsg();
    correctMsg = "Received string from Bluetooth connection, printing it to terminal\r\n";
    sentMsg = mockLogSerial._out_buf;
    assertTrue(correctMsg == sentMsg); 
    mockTerminalSerial.reset();
    mockLogSerial.reset();
}

test(canStopAndSendSendBluetoothMsgToTerminal) {
    reportFreeMemory();

    bluetoothMsg[0] = helloChars[0];
    bluetoothMsg[1] = helloChars[1];
    bluetoothMsg[2] = helloChars[2];
    bluetoothMsg[3] = helloChars[3];
    bluetoothMsg[4] = helloChars[4];
    bluetoothMsgLen = 5;

    sendBluetoothMsgToTerminal();

    correctMsg = "Connected device: hello\n\r";
    sentMsg = mockTerminalSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 

    mockTerminalSerial.reset();   
    mockLogSerial.reset();
}

test(canSendMessageToBluetoothSerial) {
    reportFreeMemory();
    
    terminalMsg[0] = helloChars[0];
    terminalMsg[1] = helloChars[1];
    terminalMsg[2] = helloChars[2];
    terminalMsg[3] = helloChars[3];
    terminalMsg[4] = helloChars[4];
    terminalMsgLen = 5;
    
    sendTerminalMsgToBluetooth();
    
    correctMsg = "ShSeSlSlSoN";
    correctMsg.concat(String((char)255));
    sentMsg = mockBluetoothSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockBluetoothSerial.reset();   
    mockLogSerial.reset();
}

test(canStopAndSendOutputCorrectLogMsg) {
    reportFreeMemory();
    
    terminalMsg[0] = helloChars[0];
    terminalMsg[1] = helloChars[1];
    terminalMsg[2] = helloChars[2];
    terminalMsg[3] = helloChars[3];
    terminalMsg[4] = helloChars[4];
    terminalMsgLen = 5;

    stopAndSendTerminalMsg();

    correctMsg = "Received string from terminal, sending it across Bluetooth\r\n";
    sentMsg = mockLogSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 

    mockBluetoothSerial.reset();
    mockLogSerial.reset();
}

test(canStopAndSendSendTerminalMsgToBluetooth) {
    reportFreeMemory();
    
    terminalMsg[0] = helloChars[0];
    terminalMsg[1] = helloChars[1];
    terminalMsg[2] = helloChars[2];
    terminalMsg[3] = helloChars[3];
    terminalMsg[4] = helloChars[4];
    terminalMsgLen = 5;

    stopAndSendTerminalMsg();
    
    correctMsg = "ShSeSlSlSoN";
    correctMsg.concat(String((char)255));
    sentMsg = mockBluetoothSerial._out_buf;
    
    assertTrue(correctMsg == sentMsg); 
 
    mockBluetoothSerial.reset();
    mockLogSerial.reset();
}

test(canAppendCharactersToMessageInReceivingState) {
    reportFreeMemory();
    
    flushBuffersAndResetStates();
    
    runTerminalStateMachine('a');
    
    assertEquals(terminalMsg[0], 'a');
    assertEquals(terminalMsgLen, 1);
}

test(canResetStateIfStateIsBogus) {
    reportFreeMemory();
    
    flushBuffersAndResetStates();
    
    terminalState = 42;
    runTerminalStateMachine('a');
    
    assertEquals(terminalState, RECEIVING_CHARS);
}

test(canSendMessageAfterReceivingState) {
    reportFreeMemory();
    
    flushBuffersAndResetStates();
    
    terminalMsg[0] = helloChars[0];
    terminalMsg[1] = helloChars[1];
    terminalMsg[2] = helloChars[2];
    terminalMsg[3] = helloChars[3];
    terminalMsg[4] = helloChars[4];
    terminalMsgLen = 5;
    
    runTerminalStateMachine(RETURN_CHAR);
    
    correctMsg = "ShSeSlSlSoN";
    correctMsg.concat(String((char)255));
    sentMsg = mockBluetoothSerial._out_buf;

    assertTrue(correctMsg == sentMsg); 
 
    mockBluetoothSerial.reset();
    mockLogSerial.reset();
}
#endif

#if TESTING == 2

test(canAdvanceToReceivingStringCharState) {
    reportFreeMemory();
    
    flushBuffersAndResetStates();
    
    runBluetoothStateMachine('S');
    
    assertEquals(bluetoothState, RECEIVING_STRING_CHAR);
}

test(canAdvanceToReceivingEndCodeState) {
    reportFreeMemory();
    
    flushBuffersAndResetStates();
    
    terminalState = 42;
    runTerminalStateMachine('a');
    
    assertEquals(terminalState, RECEIVING_CHARS);
}

test(canResetStateInReceivingStringChar) {
    reportFreeMemory();
    
    flushBuffersAndResetStates();
    runBluetoothStateMachine('S');
    runBluetoothStateMachine('h');
    
    assertEquals(bluetoothState, RECEIVING_FLAG);
}

test(canResetStateInReceivingEndCode) {
    reportFreeMemory();
    
    flushBuffersAndResetStates();
    runBluetoothStateMachine('N');
    runBluetoothStateMachine('g');
    
    assertEquals(bluetoothState, RECEIVING_FLAG);
    
    mockLogSerial.reset();
}

test(canOutputWarningInEndCodeState) {
    reportFreeMemory();
    
    flushBuffersAndResetStates();
    runBluetoothStateMachine('N');
    runBluetoothStateMachine('g');
    
    correctMsg = "WARNING: Received null flag, but did not receive null value. Continuing string read.\r\n";
    sentMsg = mockLogSerial._out_buf;
    
    assertTrue(correctMsg == sentMsg);
    
    mockLogSerial.reset();
}

test(canAppendLetterToBluetoothMsgInReceivingChar) {
    reportFreeMemory();
    
    flushBuffersAndResetStates();
    runBluetoothStateMachine('S');
    runBluetoothStateMachine('h');
    
    assertEquals(bluetoothMsg[0], 'h');
    
    mockLogSerial.reset();
}

test(canStopAndSendBluetoothMsgAfterState) {
    reportFreeMemory();
    
    flushBuffersAndResetStates();
    
    bluetoothMsg[0] = helloChars[0];
    bluetoothMsg[1] = helloChars[1];
    bluetoothMsg[2] = helloChars[2];
    bluetoothMsg[3] = helloChars[3];
    bluetoothMsg[4] = helloChars[4];
    bluetoothMsgLen = 5;
    
    runBluetoothStateMachine('N');
    runBluetoothStateMachine((char)255);
    
    correctMsg = "Connected device: hello\n\r";
    sentMsg = mockTerminalSerial._out_buf;

    Serial.println(sentMsg);

    assertTrue(correctMsg == sentMsg); 
 
    mockBluetoothSerial.reset();
    mockLogSerial.reset();
}

#endif;
