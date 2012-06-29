#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define LED_OFF 0
#define LED_ON  1

#define RED_LED 8
#define YLW_LED 10
#define GRN_LED 12

#define RECEIVING_COMMAND_FLAG 0
#define RECEIVING_COMMAND_DATA 1

AndroidAccessory acc("RIIS",
		     "AndroidArduinoUSB",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.riis.com",
		     "0000000012345678");

byte msgBuf[1];

int state;
int currentCommand;
int currentLEDNum;

boolean saidConnected = false;

void setup()
{
  pinMode(RED_LED, OUTPUT);
  pinMode(YLW_LED, OUTPUT); 
  pinMode(GRN_LED, OUTPUT);
  
  resetState();
  
  Serial.begin(115200);
  
  Serial.println("Powering up...");
  acc.powerOn();
  
  Serial.println("Waiting for Android device...");
}

void resetState()
{
  state = RECEIVING_COMMAND_FLAG;
  currentCommand = LED_OFF;
  currentLEDNum = RED_LED;
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
      
      runStateMachine(msg);
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

void runStateMachine(byte msg)
{
  switch(state)
  {
    case RECEIVING_COMMAND_FLAG:
      if(isMessageAnLEDFlag(msg))
      {
        setCurrentLEDNum(msg);
        state = RECEIVING_COMMAND_DATA;
      } else {
        resetState(); 
      }
      break;
    case RECEIVING_COMMAND_DATA:
      if(isMessageACommand(msg))
      {
        currentCommand = msg;
        executeCommand();
      }
      
      resetState(); 
      break;
    default:
      resetState();
      break;
  }
}

boolean isMessageAnLEDFlag(byte msg)
{
  char msgChar = (char) msg;
  return (msgChar == 'r' || msgChar == 'y' || msgChar == 'g');
}

void setCurrentLEDNum(byte msg)
{
  char msgChar = (char) msg;
  
  if(msgChar == 'r')
  {
    currentLEDNum = RED_LED;
  }
  else if(msgChar == 'y')
  {
    currentLEDNum = YLW_LED;
  }
  else if(msgChar == 'g')
  {
    currentLEDNum = GRN_LED;
  }
}

boolean isMessageACommand(byte msg)
{
  return (msg == LED_OFF || msg == LED_ON);
}

void executeCommand()
{
  Serial.print("Turning pin ");
  Serial.print(currentLEDNum);
  
  if(currentCommand == LED_ON)
  {
    Serial.println(" on...");
    digitalWrite(currentLEDNum, HIGH);
  }
  else if(currentCommand == LED_OFF)
  {
    Serial.println(" off...");
    digitalWrite(currentLEDNum, LOW);
  }
  
  Serial.println();
}

