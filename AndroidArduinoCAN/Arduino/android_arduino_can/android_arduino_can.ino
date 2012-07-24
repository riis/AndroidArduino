#include <SoftwareSerial.h>
#include <stdlib.h>
#include <SPI.h>
#include <mcp2515_defs.h>
#include <CANInterface.h>

/*LCD Screen Init*/
SoftwareSerial LCD = SoftwareSerial(5,6); /* Serial LCD is connected to digital pin 6 */

#define COMMAND 0xFE
#define CLEAR   0x01
#define LINE0   0x80
#define LINE1   0xC0
#define SCREEN  32

/*"Joystick" Init*/
#define UP    A1/*Analog Pin 1*/
#define DOWN  A3/*Analog Pin 3*/
#define LEFT  A5/*Analog Pin 5*/
#define RIGHT A2/*Analog Pin 2*/
#define CLICK A4/*Analog Pin 4*/
#define JSP   0/*Joystick Pressed*/
#define JSNP  1/*Joystick Not Pressed*/

/*Status LED's*/
#ifndef LED_2
int LED_2 = 8;
#endif
#ifndef LED_3
int LED_3 = 7;
#endif

int SDCS = 9; /* SPI Enable for SD Card */
int CANCS = 53;/* SPI Enable for CAN */

int inputchar [32];

void init_SPI_CS(void)
{
    pinMode(CANCS,OUTPUT);/* Initialize the CAN bus card SPI CS Pin*/
    digitalWrite(CANCS, HIGH); /* Turns the CAN bus card communication off*/
    pinMode(10,INPUT); /* all these need to be set to inputs cuz the card isn't compatible with the Arduino Mega....*/
    digitalWrite(10,HIGH);
    pinMode(11, INPUT);
    digitalWrite(11, LOW);
    pinMode(12, INPUT);
    digitalWrite(12, LOW);
    pinMode(13, INPUT);
    digitalWrite(13, LOW);
}

void init_Status_LED(void)
{
    pinMode(LED_2, OUTPUT); /*Status LED*/
    pinMode(LED_3, OUTPUT); /*communication LED*/
    digitalWrite(LED_2,HIGH);
}

/*converts char to decimal number*/
int char2num(int chr)
{
    switch(chr)
    {
    case 48:
        return 0;
        break;
    case 49:
        return 1;
        break;
    case 50:
        return 2;
        break;
    case 51:
        return 3;
        break;
    case 52:
        return 4;
        break;
    case 53:
        return 5;
        break;
    case 54:
        return 6;
        break;
    case 55:
        return 7;
        break;
    case 56:
        return 8;
        break;
    case 57:
        return 9;
        break;
    case 65:
        return 10;
        break;
    case 66:
        return 11;
        break;
    case 67:
        return 12;
        break;
    case 68:
        return 13;
        break;
    case 69:
        return 14;
        break;
    case 70:
        return 15;
        break;
    default:
        return 0;
    }
}

void init_JoyStick(void)
{
    pinMode(UP,INPUT);
    pinMode(DOWN,INPUT);
    pinMode(LEFT,INPUT);
    pinMode(RIGHT,INPUT);
    pinMode(CLICK,INPUT);
    digitalWrite(UP, HIGH);/*enabled input pull-up resistor*/
    digitalWrite(DOWN, HIGH);/*enabled input pull-up resistor*/
    digitalWrite(LEFT, HIGH);/*enabled input pull-up resistor*/
    digitalWrite(RIGHT, HIGH);/*enabled input pull-up resistor*/
    digitalWrite(CLICK, HIGH);/*enabled input pull-up resistor*/
}

void printFormatedTime(unsigned long timestamp) {
    Serial.print("Time: ");
    Serial.print(timestamp/3600000, DEC);
    Serial.print(":");
    Serial.print(timestamp/60000, DEC);
    Serial.print(":");
    Serial.print(timestamp/1000, DEC);
}

void sendECURequest(unsigned char pid) {
    tCAN message;
    float engine_data;
    int timeout = 0;
    unsigned long timestamp = 0;
    char message_ok = 0;
    // Prepare message
    message.id = PID_REQUEST;
    message.header.rtr = 0;
    message.header.length = 8;
    message.data[0] = 0x02;
    message.data[1] = 0x01;
    message.data[2] = 0x66;
    message.data[3] = 0x00;
    message.data[4] = 0x00;
    message.data[5] = 0x00;
    message.data[6] = 0x00;
    message.data[7] = 0x00;
    
    Serial.println(CAN.send_message(&message));

    while(timeout < 10000)
    {
        timeout++;
        if (CAN.check_message()) 
        {
            if (CAN.get_message(&message, &timestamp)) 
            {
                if(message.id == PID_REPLY) {
                    Serial.println("HOLY SHIT");
                }
                
                if((message.id == PID_REPLY) && (message.data[2] == pid))	// Check message is the reply and its the right PID
                {
                    Serial.println("I GOT A REPLY MESSAGE");
                    switch(message.data[2])
                    {   /* Details from http://en.wikipedia.org/wiki/OBD-II_PIDs */
                    case ENGINE_RPM:  			//   ((A*256)+B)/4    [RPM]
                        engine_data =  ((message.data[3]*256) + message.data[4])/4;
                        Serial.print("Engine RPM: ");
                        Serial.println(engine_data, DEC);
                        break;

                    case ENGINE_COOLANT_TEMP: 	// 	A-40			  [degree C]
                        engine_data =  message.data[3] - 40;
                        Serial.print("Engine Temp (C): ");
                        Serial.println(engine_data, DEC);

                        break;

                    case VEHICLE_SPEED: 		// A				  [km]
                        engine_data =  message.data[3];
                        Serial.print("Vehicle KMH: ");
                        Serial.println(engine_data, DEC);

                        break;

                    case MAF_SENSOR:   			// ((256*A)+B) / 100  [g/s]
                        engine_data =  ((message.data[3]*256) + message.data[4])/100;
                        Serial.print("MAF Sensor (g/s): ");
                        Serial.println(engine_data, DEC);
                        break;

                    case O2_VOLTAGE:    		// A * 0.005   (B-128) * 100/128 (if B==0xFF, sensor is not used in trim calc)
                        engine_data = message.data[3]*0.005;
                        Serial.print("O2 Voltage (v): ");
                        Serial.println(engine_data, DEC);

                    case THROTTLE:				// Throttle Position
                        engine_data = (message.data[3]*100)/255;
                        Serial.print("Throttle Position (%): ");
                        Serial.println(engine_data, DEC);
                        break;

                    }
                    message_ok = 1;
                }

            }
        }
    }
}

void printMessageId(uint16_t messageId) {
    Serial.print("Message Id - Mode: ");
    Serial.print((messageId & 0x0F00) >> 8, HEX);
    Serial.print(" Value: ");
    Serial.println(messageId & 0x00FF, HEX);
}

void setup(void)
{
    init_SPI_CS();
    init_JoyStick();

    Serial.begin(9600);/* Setup Serial communication to Computer*/
    Serial.println("Press Up to Begin");

    while (digitalRead(UP));

    SPI.begin();
    /*Set CANSPEED to have baud rate for 95 kbps GMLAN medium speed baudrate.*/
    if(CAN.Init(CANSPEED_500))/*SPI must be configured firstnew 95 kbps...*/
    {
        Serial.println("CAN Running");
    }
    else
    {
        Serial.println("CAN Failure");
    }

    /*CAN Data setup*/
    float engine_data;
    int timeout = 0;
    char message_ok = 0;
    char buffer[32];
    unsigned long timestamp = 0;
    while(1)
    {
        /*CAN Structure*/
        tCAN message;

        CAN.bit_modify(CANCTRL, (1<<REQOP2)|(1<<REQOP1)|(1<<REQOP0), 0);

        if (CAN.check_message())
        {
            if (CAN.get_message(&message, &timestamp))
            {
                digitalWrite(CANCS, HIGH);
                if(message.id >= PID_REPLY) {
                    Serial.println("HOLY SHIT");
                }
                printMessageId(message.id);
                //printFormatedTime(timestamp);
//                sendECURequest(VEHICLE_SPEED);
//                Serial.print("Message ID: ");
//                Serial.print(message.id,HEX);
//                Serial.print(", \tMessage Payload: ");
//                for(int i = 0; i < 8; i++)
//                {
//                    Serial.print(message.data[i],HEX);
//                    Serial.print(' ');
//                }
//                Serial.println();
            } 
        }

        digitalWrite(CANCS, LOW);
        if(Serial.available() >= 1)
        {
            if(Serial.read() == 'W')
            {
                /*Serial write format looks like this:
                 "W 0x000 0x0 0x0000000000000000"
                 do that (With the spaces) and you are good, otherwise we have a problem, no quotes.
                 There is no format error detection so be careful.
                 Note that its "W ID Data Length Payload" In HEX NOT Decimal....this is also key, no "15" enter F
                 Its case sensitive, so no 'f' either, don't complain, fix it if you like code is above, scroll up.*/
//                for(uint8_t i = 0;i < 30; i++)
//                {
//                    inputchar[i] = Serial.read();
//                }
//
//                message.id = (uint16_t)(char2num(inputchar[4])<<8)+(char2num(inputchar[5])<<4)+(char2num(inputchar[6]));
//                message.header.length = char2num(inputchar[10]);
//                message.header.rtr = 0;
//
//                for(uint8_t i = 0;i < message.header.length;i++)
//                {
//                    /*29....Yes this is effed up. but basically, there are 30 elements in the array so 0-29
//                     are the indices.14th position is MSB of payload, 29th is LSB and all that needs
//                     to go into message.data to be sent over the friendly CAN bus....*/
//                    message.data[i] = (uint8_t)((char2num(inputchar[29-(i*2+1)])<<4)+(char2num(inputchar[29-(i*2)])));
//                }

                message.id = PID_REQUEST;
                message.header.rtr = 0;
                message.header.length = 8;
                message.data[0] = 0x02;
                message.data[1] = 0x01;
                message.data[2] = 0x33;
                message.data[3] = 0x00;
                message.data[4] = 0x00;
                message.data[5] = 0x00;
                message.data[6] = 0x00;
                message.data[7] = 0x00;

                Serial.println("======================");
                Serial.println("Sending message to CAN");
                Serial.println(CAN.send_message(&message));
                Serial.println("======================");
            } 
        }

        delay(10);
        if(!digitalRead(DOWN))
        {
            break;
        }
    }
    delay(5000);
    Serial.println("Program Done");
}

void loop(void) { 
}

