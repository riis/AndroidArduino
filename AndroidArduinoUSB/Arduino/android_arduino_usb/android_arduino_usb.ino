#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define DO_NOTHING  0
#define LED_OFF     1
#define LED_ON      2
#define LED1        3
#define LED2        4
#define LED3        5

#define IDLE             0
#define RECEIVED_COMMAND 1

AndroidAccessory acc("RIIS",
		     "AndroidArduinoUSB",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.riis.com",
		     "0000000012345678");

int redLED = 8;
int yellowLED = 10;
int greenLED = 12;

byte msgBuf[1];

int state;
int currentCommand;
int currentLEDNum;

boolean saidConnected = false;

void setup()
{
  pinMode(redLED, OUTPUT);
  pinMode(yellowLED, OUTPUT); 
  pinMode(greenLED, OUTPUT);
  
  resetState();
  
  Serial.begin(115200);
  
  Serial.println("Powering up...");
  acc.powerOn();
  
  Serial.println("Waiting for Android device...");
}

void resetState()
{
  state = IDLE;
  currentCommand = DO_NOTHING;
  currentLEDNum = LED1;
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
      
      runStateMachine(msg);
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

void runStateMachine(byte msg)
{
  switch(state)
  {
    case IDLE:
      if(isMessageACommand(msg))
      {
        currentCommand = msg;
        state = RECEIVED_COMMAND;
      } else {
        resetState(); 
      }
      break;
    case RECEIVED_COMMAND:
      if(isMessageAnLEDNum(msg))
      {
        currentLEDNum = msg;
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

