#include <SoftwareSerial.h>
#include <Bluetooth.h>

#define TESTING 0

#if TESTING == 1
    #include <ArduinoUnit.h>
    #include <Mocks.h>
#endif

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
    logSerial->println("Received string from terminal, sending it across Bluetooth");
    
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
