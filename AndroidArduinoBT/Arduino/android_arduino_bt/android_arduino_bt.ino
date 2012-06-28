#include <SoftwareSerial.h>

//Definitions for the command structure to control the LEDs
#define DO_NOTHING  0
#define LED_OFF     1
#define LED_ON      2
#define LED1        3
#define LED2        4
#define LED3        5

//Definitions for the states of the state machine
#define IDLE             0
#define RECEIVED_COMMAND 1

//RX and TX pin numbers
#define RX 11
#define TX 3

int redLED = 8;
int yellowLED = 10;
int greenLED = 12;

int state;
int currentCommand;
int currentLEDNum;

SoftwareSerial blueToothSerial(RX, TX);

void setup()
{
  setUpIO();  
  resetState();
  
  Serial.begin(115200);
  
  Serial.println("Powering up the BlueTooth device...");
  setUpBlueToothConnection();
 
  Serial.println("BlueTooth device ready, connect to \"AndroidArduinoBT.\"");
  Serial.println();
  Serial.println("Waiting for messages...");
  Serial.println();
}

void setUpIO()
{
  pinMode(redLED, OUTPUT);
  pinMode(yellowLED, OUTPUT); 
  pinMode(greenLED, OUTPUT);
  pinMode(RX, INPUT);
  pinMode(TX, OUTPUT);
}

void resetState()
{
  state = IDLE;
  currentCommand = DO_NOTHING;
  currentLEDNum = LED1;
}

void setUpBlueToothConnection()
{
  blueToothSerial.begin(38400);

  blueToothSerial.print("\r\n+STWMOD=0\r\n"); //Set the BlueTooth to slave mode
  blueToothSerial.print("\r\n+STNA=AndroidArduinoBT\r\n"); //Set the BlueDTooth name to "AndroidArduinoBT"
  blueToothSerial.print("\r\n+STOAUT=1\r\n"); //Permit a paired device to connect
  blueToothSerial.print("\r\n+STAUTO=0\r\n"); //No auto conneection
  
  delay(2000);  
  blueToothSerial.print("\r\n+INQ=1\r\n"); //Make the BlueTooth device inquirable 

  delay(2000);
  blueToothSerial.flush();
}

void loop()
{
  byte msg;
  
  if(blueToothSerial.available()) {
    msg = blueToothSerial.read();
    
    printReceivedMessage(msg);
    runStateMachine(msg);
  }
}

void printReceivedMessage(byte msg)
{
  Serial.print("Recieved ");
  Serial.print(msg);
  Serial.println(" from master");
}

void runStateMachine(byte msgByte)
{
  switch(state)
  {
    case IDLE:
      if(isMessageACommand(msgByte))
      {
        currentCommand = msgByte;
        state = RECEIVED_COMMAND;
      } else {
        resetState(); 
      }
      break;
    case RECEIVED_COMMAND:
      if(isMessageAnLEDNum(msgByte))
      {
        currentLEDNum = msgByte;
        executeCommand();
        resetState();
      } else {
        resetState(); 
      }
      break;
    default:
      state = IDLE;
      break;
  }
}

void executeCommand()
{
  if(currentCommand == LED_ON)
  {
    switch(currentLEDNum)
    {
      case LED1:
        Serial.println("Turning red LED on...");
        digitalWrite(redLED, HIGH);
        break;
      case LED2:
        Serial.println("Turning yellow LED on...");
        digitalWrite(yellowLED, HIGH);
        break; 
      case LED3:
        Serial.println("Turning green LED on...");
        digitalWrite(greenLED, HIGH);
        break; 
      default:
        break;
    }
    Serial.println();
  }
  
  if(currentCommand == LED_OFF)
  {
    switch(currentLEDNum)
    {
      case LED1:
        Serial.println("Turning red LED off...");
        digitalWrite(redLED, LOW);
        break;
      case LED2:
        Serial.println("Turning yellow LED off...");
        digitalWrite(yellowLED, LOW);
        break; 
      case LED3:
        Serial.println("Turning green LED off...");
        digitalWrite(greenLED, LOW);
        break; 
      default:
        break;
    }
    Serial.println();
  }
}

boolean isMessageACommand(byte msg)
{
  return (msg == LED_OFF || msg == LED_ON);
}

boolean isMessageAnLEDNum(byte msg)
{
  return (msg == LED1 || msg == LED2 || msg == LED3);
}

