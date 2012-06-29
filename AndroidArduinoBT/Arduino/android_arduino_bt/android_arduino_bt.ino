#include <SoftwareSerial.h>

//LED command codes
#define LED_OFF 0
#define LED_ON  1

//LED pin numbers
#define RED_LED 8
#define YLW_LED 10
#define GRN_LED 12

//State machine states
#define RECEIVING_COMMAND_FLAG 0
#define RECEIVING_COMMAND_DATA 1

//RX and TX pin numbers
#define RX 11
#define TX 3

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
 
  Serial.println("BlueTooth device ready, connect to \"AndroidArduinoBT\".");
  Serial.println();
  Serial.println("Waiting for messages...");
  Serial.println();
}

void setUpIO()
{
  pinMode(RED_LED, OUTPUT);
  pinMode(YLW_LED, OUTPUT); 
  pinMode(GRN_LED, OUTPUT);
  
  pinMode(RX, INPUT);
  pinMode(TX, OUTPUT);
}

void resetState()
{
  state = RECEIVING_COMMAND_FLAG;
  currentCommand = LED_OFF;
  currentLEDNum = RED_LED;
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
  byte msg;
  
  if(blueToothSerial.available()) {
    msg = (byte) blueToothSerial.read();
    
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

