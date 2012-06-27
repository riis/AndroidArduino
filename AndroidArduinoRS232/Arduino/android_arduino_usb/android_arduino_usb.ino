#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define STRING_END 0
#define STRING_LENGTH 256

#define IDLE 0
#define RECEIVING_STRING 1

AndroidAccessory acc("RIIS",
		     "AndroidArduinoRS232",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.riis.com",
		     "0000000012345678");

int state = IDLE;

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
  state = IDLE;
  
  for(int i = 0; i < STRING_LENGTH; i++) {
    stringBuf[i] = 0; 
  }
  curCharIndex = 0;
}

void loop()
{
  if (acc.isConnected())
  {
    if(!saidConnected)
    {
      Serial.println();
      Serial.println("Connected!");
      Serial.println();
      saidConnected = true;
    }
    
    if(tryToReadMessageInto(msgBuf))
    {
      byte msg = msgBuf[0];
      printReceivedMessage(msg);
      
      runStateMachine((char)msg);
    }
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

void runStateMachine(char letter)
{
  switch(state)
  {
    case IDLE:
      appendCharOnString(letter);
      state = RECEIVING_STRING;
      break;
    case RECEIVING_STRING:
      if(isLetterNotEndCode(letter))
      {
        appendCharOnString(letter);
      }
      else
      {
        state = IDLE;
        Serial.println("String terminated, reversing and sending...");
        reverseAndSendString();
        resetStringAndState();
        Serial.println();
      }
      break;
    default:
      state = IDLE;
      break;
  }
}

void appendCharOnString(char letter)
{
  if(curCharIndex < STRING_LENGTH)
  {
    stringBuf[curCharIndex] = letter;
    curCharIndex++;
  }
}

boolean isLetterNotEndCode(char letter)
{
  byte charNum = (byte) letter;
  return (charNum != STRING_END); 
}

void reverseAndSendString()
{
  for(int i = curCharIndex - 1; i >= 0; i--)
  {
    Serial1.print(stringBuf[i]);
  }
  
  Serial1.println();
}

