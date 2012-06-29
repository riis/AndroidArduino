#include <SoftwareSerial.h>

#define STRING_END 0
#define STRING_LENGTH 256

//States for the state machine
#define RECEIVING_STRING_FLAG 1
#define RECEIVING_STRING_CHAR 2
#define RECEIVING_END_CODE    3

//RX and TX pin numbers
#define RX 11
#define TX 3

SoftwareSerial blueToothSerial(RX, TX);

int state;

byte msgBuf[1];

char stringBuf[STRING_LENGTH];
byte curCharIndex;

void setup()
{
  setUpIO();
  resetStringAndState();
  
  Serial.begin(115200);  
  
  Serial.println("Powering up the BlueTooth device...");
  setUpBlueToothConnection();
  Serial.println("BlueTooth device ready, connect to \"AndroidArduinoBT\".");

  Serial.println("Initializing other serial port...");  
  Serial1.begin(115200);
  
  Serial.println();
  Serial.println("Waiting for messages...");
  Serial.println();
}

void setUpIO()
{  
  pinMode(RX, INPUT);
  pinMode(TX, OUTPUT);
}

void resetStringAndState()
{
  state = RECEIVING_STRING_FLAG;
  
  for(int i = 0; i < STRING_LENGTH; i++) {
    stringBuf[i] = 0; 
  }
  curCharIndex = 0;
}

void setUpBlueToothConnection()
{
  blueToothSerial.begin(38400);

  blueToothSerial.print("\r\n+STWMOD=0\r\n"); //Set the BlueTooth to slave mode
  blueToothSerial.print("\r\n+STNA=AndroidArduinoBT\r\n"); //Set the BlueTooth name to "AndroidArduinoBT"
  blueToothSerial.print("\r\n+STOAUT=1\r\n"); //Permit a paired device to connect
  blueToothSerial.print("\r\n+STAUTO=0\r\n"); //No auto conneection
  
  delay(2000);  
  blueToothSerial.print("\r\n+INQ=1\r\n"); //Make the BlueTooth device inquirable 

  delay(2000);
  blueToothSerial.flush();
}

void loop()
{
  char msg;
  
  if(blueToothSerial.available()) {
    msg = blueToothSerial.read();

    printReceivedMessage(msg); 
    runStateMachine(msg);
  }
}

void printReceivedMessage(char msg)
{
  Serial.print("Recieved ");
  Serial.print(msg);
  Serial.println(" from Android device");
}

void runStateMachine(char letter)
{
  switch(state)
  {
    case RECEIVING_STRING_FLAG:
      if(isLetterStringFlag(letter))
      {
        state = RECEIVING_STRING_CHAR;
      }
      else if(isLetterEndFlag(letter))
      {
        state = RECEIVING_END_CODE;
      }
      break;
    case RECEIVING_STRING_CHAR:
      if(!isLetterEndCode(letter))
      {
        state = RECEIVING_STRING_FLAG;
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
        state = RECEIVING_STRING_FLAG;
        Serial.println("WARNING: Received null flag, but did not receive null value. Continuing string read.");
      }
    default:
      state = RECEIVING_STRING_FLAG;
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
  state = RECEIVING_STRING_FLAG;
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
}

