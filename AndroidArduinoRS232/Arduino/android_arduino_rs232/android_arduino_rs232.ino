#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define STRING_END 0
#define STRING_LENGTH 256

//States for the state machine
#define RECEIVING_FLAG 1
#define RECEIVING_STRING_CHAR 2
#define RECEIVING_END_CODE    3
#define RECEIVING_TESTING_VALUE 4

#define TESTING_CODE_FAILURE -1
#define TESTING_CODE_SUCCESS 1
#define TESTING_CODE_WORKING 0

AndroidAccessory acc("RIIS",
		     "AndroidArduinoRS232",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.riis.com",
		     "0000000012345678");

int state;

boolean testing = false;
byte testingCode;

boolean saidConnected;
byte msgBuf[1];

char stringBuf[STRING_LENGTH];
byte curCharIndex;

void setup()
{
    resetStringAndState();
    saidConnected = false;
    
    Serial.begin(115200);
    Serial.println("Powering up...");
    acc.powerOn();

    Serial.println("Initializing other serial port...");    
    Serial1.begin(115200);
    
    Serial.println("Waiting for Android device...");
}

void resetStringAndState()
{
    state = RECEIVING_FLAG;
  
    for(int i = 0; i < STRING_LENGTH; i++) {    
        stringBuf[i] = 0; 
    }
    curCharIndex = 0;
}

void loop()
{
    if (acc.isConnected())
    {
        printConnectedMessage();

	if(tryToReadMessageInto(msgBuf))
        {
            byte msg = msgBuf[0];
            printReceivedMessage(msg);
            if(testing)
            {
                printTestStatusCode();
            }
            
            runStateMachine((char)msg);
        }
    }
}

void printConnectedMessage()
{
    if(!saidConnected)
    {
        Serial.println();
        Serial.println("Connected!");
        Serial.println();
        saidConnected = true;
    }
}

boolean tryToReadMessageInto(byte msgBuffer[])
{
    int len = acc.read(msgBuffer, sizeof(msgBuffer), 1);
    return (len >= 1);
}

void printReceivedMessage(byte msg)
{
    Serial.print("Recieved ");
    Serial.print(msg);
    Serial.println(" from Android device");
}

void printTestStatusCode()
{
    byte[] msgBuffer = {'T', testingCode};
    acc.write(msgBuffer, sizeof(msgBuffer), 2);
}

void runStateMachine(char letter)
{
    testingByte = TESTING_CODE_WORKING;
    switch(state)
    {
        case RECEIVING_FLAG:
            if(isTestingFlag(letter))
            {
                state = RECEIVING_TESTING_VALUE;
            }
            if(isLetterStringFlag(letter))
            {
                state = RECEIVING_STRING_CHAR;
            }
            else if(isLetterEndFlag(letter))
            {
                state = RECEIVING_END_CODE;
            }
            break;
        case RECEIVING_TESTING_VALUE:
            setTestingVal(letter);
            state = RECEIVING_FLAG;
            break;
        case RECEIVING_STRING_CHAR:
            if(!isLetterEndCode(letter))
            {
                state = RECEIVING_FLAG;
                appendLetterOnString(letter);
            }
            else
            {
                stopAndSendString();
            }
            break;
        case RECEIVING_END_CODE:
            if(isLetterEndCode(letter))
            {
                stopAndSendString();
            }
            else
            {
                state = RECEIVING_FLAG;
                Serial.println("WARNING: Received null flag, but did not receive null value. Continuing string read.");
                testingByte = TESTING_CODE_FAILURE;
            }
        default:
          state = RECEIVING_FLAG;
          break;
    }
}

boolean isTestingFlag(char letter)
{
    return (letter == 'T');
}

void setTestingVal(char letter)
{
    if(letter == 1)
    {
        testing = true;
    }
    else
    {
        testing = false;
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

void appendLetterOnString(char letter)
{
    if(curCharIndex < STRING_LENGTH)
    {
        stringBuf[curCharIndex] = letter;
        curCharIndex++;
    }
}

void stopAndSendString()
{
    state = RECEIVING_FLAG;
    Serial.println("String terminated, reversing and sending...");
    reverseAndSendString();
    resetStringAndState();
    Serial.println();
}

void reverseAndSendString()
{
    for(int i = curCharIndex - 1; i >= 0; i--)
    {
        Serial1.print(stringBuf[i]);
    }
  
    Serial1.println();
    testingByte = TESTING_CODE_SUCCESS;
}

