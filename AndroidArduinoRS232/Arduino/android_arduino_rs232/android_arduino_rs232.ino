#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define STRING_END 0
#define RETURN_CHAR 13
#define MSG_LENGTH_MAX 256

//States for the Android state machine
#define RECEIVING_FLAG 1
#define RECEIVING_STRING_CHAR 2
#define RECEIVING_END_CODE    3

//States for the terminal state machine
#define RECEIVING_CHARS 1

AndroidAccessory acc("RIIS",
		     "AndroidArduinoRS232",
		     "MegaADK Arduino Board",
		     "1.0",
		     "http://www.riis.com",
		     "0000000012345678");

int androidState;
int terminalState;

boolean saidConnected;

byte incomingMsgBuf[MSG_LENGTH_MAX];

int terminalMsgLen;
char terminalMsg[MSG_LENGTH_MAX];

int androidMsgLen;
char androidMsg[MSG_LENGTH_MAX];

void setup()
{
    flushBuffersAndResetStates();
    saidConnected = false;
    
    Serial.begin(115200);
    Serial.println("Powering up Android connection...");
    acc.powerOn();

    Serial.println("Initializing other serial port...");    
    Serial1.begin(115200);
    Serial1.print('\f');
    Serial1.print("Waiting for Android device...\n\r");
    
    Serial.println("Waiting for Android device...");
}

void flushBuffersAndResetStates()
{  
    flushIncomingMsgBuffer();
    flushAndroidMsgBuffer();
    flushTerminalMsgBuffer();
    
    resetAndroidState();
    resetTerminalState();
}

void flushIncomingMsgBuffer()
{
    for(int i = 0; i < MSG_LENGTH_MAX; i++) {    
        incomingMsgBuf[i] = 0; 
    } 
}

void flushAndroidMsgBuffer()
{
    for(int i = 0; i < MSG_LENGTH_MAX; i++) {    
        androidMsg[i] = 0; 
    }
    androidMsgLen = 0;
}

void flushTerminalMsgBuffer()
{
    for(int i = 0; i < MSG_LENGTH_MAX; i++) {    
        terminalMsg[i] = 0; 
    }
    terminalMsgLen = 0;
}

void resetAndroidState()
{
    androidState = RECEIVING_FLAG;
}

void resetTerminalState()
{
    terminalState = RECEIVING_CHARS;
}

void loop()
{
    if(acc.isConnected())
    {
        printConnectedMessage();

        flushIncomingMsgBuffer();
        byte msgLen = tryToReadAndroidMsgInto(incomingMsgBuf);
	if(msgLen > 0)
        {
            for(int i = 0; i < msgLen; i++)
            {
                byte msg = incomingMsgBuf[i];
                runAndroidStateMachine((char)msg);
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
}

void printConnectedMessage()
{
    if(!saidConnected)
    {
        Serial.print("\n\rConnected! Communications ready on the terminal\n\n\r");
        
        Serial1.print("Android device connected, monitoring for messages\n\r");
        Serial1.print("To send messages, type a line, then press enter.\n\n\r");
        
        saidConnected = true;
    }
}

byte tryToReadAndroidMsgInto(byte msgBuf[])
{
    int len = acc.read(msgBuf, sizeof(msgBuf), 14000);
    return len;
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

void echoTerminalMessage(byte msgBuf[], byte msgLen)
{
    for(int i = 0; i < msgLen; i++)
    {
        if((char)msgBuf[i] == '\r')
        {
             Serial1.print('\n');
        }
        
        Serial1.print((char)msgBuf[i]);
    }
}

void runAndroidStateMachine(char letter)
{
    switch(androidState)
    {
        case RECEIVING_FLAG:
            if(isLetterStringFlag(letter))
            {
                androidState = RECEIVING_STRING_CHAR;
            }
            else if(isLetterEndFlag(letter))
            {
                androidState = RECEIVING_END_CODE;
            }
            break;
        case RECEIVING_STRING_CHAR:
            if(!isLetterEndCode(letter))
            {
                resetAndroidState();
                appendLetterOnAndroidMsg(letter);
            }
            else
            {
                stopAndSendAndroidMsg();
            }
            break;
        case RECEIVING_END_CODE:
            if(isLetterEndCode(letter))
            {
                stopAndSendAndroidMsg();
            }
            else
            {
                resetAndroidState();
                Serial.println("WARNING: Received null flag, but did not receive null value. Continuing string read.");
            }
        default:
          resetAndroidState();
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

void appendLetterOnAndroidMsg(char letter)
{
    if(androidMsgLen < MSG_LENGTH_MAX)
    {
        androidMsg[androidMsgLen] = letter;
        androidMsgLen++;
    }
}

void stopAndSendAndroidMsg()
{
    Serial.println("Android sent string, printing it to terminal");
    
    sendAndroidMsgToTerminal();
    
    flushAndroidMsgBuffer();
    resetAndroidState();
}

void sendAndroidMsgToTerminal()
{
    Serial1.print("Android sent:");
    
    for(int i = 0; i < androidMsgLen; i++)
    {
        Serial1.print(androidMsg[i]);
    }
  
    Serial1.print("\n\r");
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
    Serial.println("Received string from terminal, sending it to Android");
    
    sendTerminalMsgToAndroid();
    flushTerminalMsgBuffer();
    resetTerminalState(); 
}

void sendTerminalMsgToAndroid()
{
    byte flagBuf[] = {'S'};
    byte msgBuf[1];
    
    for(int i = 0; i < terminalMsgLen; i++)
    {
        acc.write(flagBuf, 1);
        
        msgBuf[0] = (byte)terminalMsg[i];
        acc.write(msgBuf, 1);       
    }
    
    //Send a null character to terminate the string
    flagBuf[0] = 'N';
    acc.write(flagBuf, 1);
    
    msgBuf[0] = 0;
    acc.write(msgBuf, 1);
}

