#include <SoftwareSerial.h>
#include <stdlib.h>
#include <SPI.h>
#include <mcp2515_defs.h>
#include <CANInterface.h>

#include <SoftwareSerial.h>
#include <Bluetooth.h>

//Bluetooth
#define RX 62
#define TX 7

//SoftwareSerial bluetoothSerial(RX, TX);
//Bluetooth bluetooth("AndroidArduinoCANBT", bluetoothSerial, true);

//Joystick
#define UP    A1/*Analog Pin 1*/
#define DOWN  A3/*Analog Pin 3*/
#define LEFT  A5/*Analog Pin 5*/
#define RIGHT A2/*Analog Pin 2*/
#define CLICK A4/*Analog Pin 4*/
#define JSP   0/*Joystick Pressed*/
#define JSNP  1/*Joystick Not Pressed*/

int CANCS = 53;/* SPI Enable for CAN */

int inputchar [32];

float engine_data;
int timeout = 0;
char message_ok = 0;
char buffer[32];
unsigned long timestamp = 0;

tCAN message;

void setup(void)
{
    init_SPI_CS();
    init_JoyStick();

    Serial.begin(9600);/* Setup Serial communication to Computer*/
//    
//    if(!bluetooth.beginBluetooth())
//    {
//        Serial.println("\n\rHalting program...");
//        while(true) { }
//    }
    
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
    
    digitalWrite(CANCS, LOW);
}

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

void loop(void) {
//    bluetooth.process();
    
    CAN.bit_modify(CANCTRL, (1<<REQOP2)|(1<<REQOP1)|(1<<REQOP0), 0);
    
//    if(bluetooth.isConnected()) {
//        sendECURequest(ENGINE_RPM);
//        sendECURequest(ENGINE_COOLANT_TEMP);
//        sendECURequest(VEHICLE_SPEED);
//        sendECURequest(MAF_SENSOR);
//        sendECURequest(O2_VOLTAGE);
//        sendECURequest(THROTTLE);
        
        
        tCAN message;
        float engine_data;
        int timeout = 0;
        unsigned long timestamp = 0;
        char message_ok = 0;
        if (CAN.check_message())
        {
            if (CAN.get_message(&message, &timestamp))
            {
                if(dontIgnoreMessage(message.id)) {
                    digitalWrite(CANCS, HIGH);
                    Serial.print("Message ID: ");
                    Serial.print(message.id, HEX);
                  
                    Serial.print(", \tMessage Payload: ");
                    for(int i = 0; i < 8; i++)
                    {
                        Serial.print(message.data[i],HEX);
                        Serial.print(' ');
                    }
                    Serial.println();
                }
            } 
        }

        digitalWrite(CANCS, LOW);
    
    delay(10);
    if(!digitalRead(DOWN))
    {
        Serial.println("Program Done");
        while(1){}
    }
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
    message.data[2] = pid;
    message.data[3] = 0x00;
    message.data[4] = 0x00;
    message.data[5] = 0x00;
    message.data[6] = 0x00;
    message.data[7] = 0x00;
    
    CAN.send_message(&message);

    while(timeout < 4000)
    {
        timeout++;
        if (CAN.check_message()) 
        {
            if (CAN.get_message(&message, &timestamp)) 
            {
                if((message.id >= PID_REPLY) && (message.data[2] == pid))	// Check message is the reply and its the right PID
                {
                    switch(message.data[2])
                    {   /* Details from http://en.wikipedia.org/wiki/OBD-II_PIDs */
                    case ENGINE_RPM:  			//   ((A*256)+B)/4    [RPM]
                        engine_data =  ((message.data[3]*256) + message.data[4])/4;
                        Serial.print("Engine RPM: ");
                        Serial.println(engine_data, DEC);
//                        bluetooth.sendStringWithFlags(String("Engine RPM: ") + String((int)engine_data));
                        timeout = 4000;
                        break;

                    case ENGINE_COOLANT_TEMP: 	// 	A-40			  [degree C]
                        engine_data =  message.data[3] - 40;
                        Serial.print("Engine Temp (C): ");
                        Serial.println(engine_data, DEC);
//                        bluetooth.sendStringWithFlags(String("Engine Temp (C): ") + String((int)engine_data));
                        timeout = 4000;
                        break;

                    case VEHICLE_SPEED: 		// A				  [km]
                        engine_data =  message.data[3];
                        Serial.print("Vehicle KMH: ");
                        Serial.println(engine_data, DEC);
//                        bluetooth.sendStringWithFlags(String("Vehicle KMH: ") + String((int)engine_data));
                        timeout = 4000;

                        break;

                    case MAF_SENSOR:   			// ((256*A)+B) / 100  [g/s]
                        engine_data =  ((message.data[3]*256) + message.data[4])/100;
                        Serial.print("MAF Sensor (g/s): ");
                        Serial.println(engine_data, DEC);
//                        bluetooth.sendStringWithFlags(String("MAF Sensor (g/s): ") + String((int)engine_data));
                        timeout = 4000;
                        break;

                    case O2_VOLTAGE:    		// A * 0.005   (B-128) * 100/128 (if B==0xFF, sensor is not used in trim calc)
                        engine_data = message.data[3]*0.005;
                        Serial.print("O2 Voltage (v): ");
                        Serial.println(engine_data, DEC);
//                        bluetooth.sendStringWithFlags(String("O2 Voltage (v): ") + String((int)engine_data));
                        timeout = 4000;
                        break;

                    case THROTTLE:				// Throttle Position
                        engine_data = (message.data[3]*100)/255;
                        Serial.print("Throttle Position (%): ");
                        Serial.println(engine_data, DEC);
//                        bluetooth.sendStringWithFlags(String("Throttle Position (%): ") + String((int)engine_data));
                        timeout = 4000;
                        break;

                    }
                    message_ok = 1;
                }
            }
        }
    }
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

boolean dontIgnoreMessage(uint16_t messageId) {
    switch(messageId) {
        case 0x0100:
        case 0x0101:
        case 0x0102:
        case 0x0106:
        case 0x0107:
        case 0x0108:
        case 0x0109:
        case 0x010A:
        case 0x010B:
        case 0x010E:
        case 0x0110:
        case 0x0112:
        case 0x0113:
        case 0x0114:
        case 0x0115:
        case 0x0116:
        case 0x0117:
        case 0x0118:
        case 0x0119:
        case 0x011A:
        case 0x011B:
        case 0x011D:
        case 0x0120:
        case 0x0121:
        case 0x0122:
        case 0x0123:
        case 0x0124:
        case 0x0125:
        case 0x0126:
        case 0x0127:
        case 0x0128:
        case 0x0129:
        case 0x012A:
        case 0x012B:
        case 0x012C:
        case 0x012D:
        case 0x012E:
        case 0x0130:
        case 0x0131:
        case 0x0132:
        case 0x0133:
        case 0x0134:
        case 0x0135:
        case 0x0136:
        case 0x0137:
        case 0x0138:
        case 0x0139:
        case 0x013A:
        case 0x013B:
        case 0x013C:
        case 0x013D:
        case 0x013E:
        case 0x013F:
        case 0x0140:
        case 0x0141:
        case 0x0142:
        case 0x0143:
        case 0x0144:
        case 0x014C:
        case 0x014D:
        case 0x014E:
        case 0x014F:
        case 0x0150:
        case 0x0153:
        case 0x0154:
        case 0x0155:
        case 0x0156:
        case 0x0157:
        case 0x0158:
        case 0x0159:
        case 0x0160:
        case 0x0166:
        case 0x0169:
        case 0x016A:
        case 0x016B:
        case 0x016C:
        case 0x016D:
        case 0x016E:
        case 0x016F:
        case 0x0170:
        case 0x0171:
        case 0x0172:
        case 0x0173:
        case 0x0174:
        case 0x0175:
        case 0x0176:
        case 0x0177:
        case 0x0178:
        case 0x0179:
        case 0x017A:
        case 0x017B:
        case 0x017C:
        case 0x017D:
        case 0x017E:
        case 0x0180:
        case 0x0184:
        case 0x0185:
        case 0x0186:
        case 0x0187:
        case 0x01A1:
        case 0x01C0:
            return false;
            break;
        default:
            return true;
    }
}

//    /*CAN Data setup*/
//    float engine_data;
//    int timeout = 0;
//    char message_ok = 0;
//    char buffer[32];
//    unsigned long timestamp = 0;
//    /*CAN Structure*/
//    tCAN message;
//    while(1)
//    {
//        CAN.bit_modify(CANCTRL, (1<<REQOP2)|(1<<REQOP1)|(1<<REQOP0), 0);
//        sendECURequest(ENGINE_RPM);
//        sendECURequest(ENGINE_COOLANT_TEMP);
//        sendECURequest(VEHICLE_SPEED);
//        sendECURequest(MAF_SENSOR);
//        sendECURequest(O2_VOLTAGE);
//        sendECURequest(THROTTLE);
//        
//        if (CAN.check_message())
//        {
//            if (CAN.get_message(&message, &timestamp))
//            {digitalWrite(CANCS, HIGH);
//                printMessageId(message.id);
//                printFormatedTime(timestamp);
//
//                Serial.print(", \tMessage Payload: ");
//                for(int i = 0; i < 8; i++)
//                {
//                    Serial.print(message.data[i],HEX);
//                    Serial.print(' ');
//                }
//                Serial.println();
//            } 
//        }
//
//        digitalWrite(CANCS, LOW);
//        if(Serial.available() >= 1)
//        {
//            if(Serial.read() == 'W')
//            {
//                /*Serial write format looks like this:
//                 "W 0x000 0x0 0x0000000000000000"
//                 do that (With the spaces) and you are good, otherwise we have a problem, no quotes.
//                 There is no format error detection so be careful.
//                 Note that its "W ID Data Length Payload" In HEX NOT Decimal....this is also key, no "15" enter F
//                 Its case sensitive, so no 'f' either, don't complain, fix it if you like code is above, scroll up.*/
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
//
//                message.id = PID_REQUEST;
//                message.header.rtr = 0;
//                message.header.length = 8;
//                message.data[0] = 0x02;
//                message.data[1] = 0x09;
//                message.data[2] = 0x0C;
//                message.data[3] = 0x00;
//                message.data[4] = 0x00;
//                message.data[5] = 0x00;
//                message.data[6] = 0x00;
//                message.data[7] = 0x00;
//
//                Serial.println("======================");
//                Serial.println("Sending message to CAN");
//                Serial.println(CAN.send_message(&message));
//                Serial.println("======================");
//            } 
//        }
//
//        delay(10);
//        if(!digitalRead(DOWN))
//        {
//            break;
//        }
//    }
//    delay(5000);
//    Serial.println("Program Done");

