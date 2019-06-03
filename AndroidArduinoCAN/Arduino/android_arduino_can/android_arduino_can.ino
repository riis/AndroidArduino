
**
//USER REQUEST FOR LATEST FILE
**

#include <SoftwareSerial.h>
#include <stdlib.h>
#include <SPI.h>
#include <mcp2515_defs.h>
#include <CANInterface.h>

#include <SoftwareSerial.h>
#include <Bluetooth.h>

//Bluetooth IO
#define RX 62
#define TX 7

//Joystick IO
#define UP    A1
#define DOWN  A3

//Misc
#define NUM_PARAMETERS 15
#define CANCS 53 //SPI Enable for CAN

SoftwareSerial bluetoothSerial(RX, TX);
Bluetooth bluetooth("AndroidArduinoCANBT", bluetoothSerial, true, 3);

boolean lastConnectionState; //True for connected and false for disconnected

void setup(void) {
    initSPICS();
    initJoyStick();
    
    pinMode(2, OUTPUT);

    Serial.begin(9600);

    Serial.println("Setting up CAN...");
    SPI.begin();
    if(CAN.Init(CANSPEED_500)) {
        Serial.println("CAN setup successful!\n\r");
    } 
    else {
        Serial.println("CAN setup failed, halting program...\n\r");
        while(true);
    }

    if(!bluetooth.beginBluetooth()) {
        Serial.println("\n\rHalting program...\n\r");
        while(true);
    }
    
    Serial.print("\n\rWaiting for a Bluetooth connection...\n\r");

    digitalWrite(CANCS, LOW);
}

void initSPICS(void) {
    pinMode(CANCS, OUTPUT);
    digitalWrite(CANCS, HIGH);

    pinMode(10, INPUT);
    digitalWrite(10, HIGH);
    pinMode(11, INPUT);
    digitalWrite(11, LOW);
    pinMode(12, INPUT);
    digitalWrite(12, LOW);
    pinMode(13, INPUT);
    digitalWrite(13, LOW);
}

void initJoyStick(void) {
    pinMode(UP,INPUT);
    pinMode(DOWN,INPUT);

    digitalWrite(UP, HIGH);
    digitalWrite(DOWN, HIGH);
}

boolean thingy = false;
unsigned long lastTime = 0;

void loop(void) {
    float engineData[NUM_PARAMETERS];
    bluetooth.process();

    if(millis() > lastTime + 250) {
        if(thingy) {
            digitalWrite(2, LOW);
        } else {
            digitalWrite(2, HIGH);
        }
        
        thingy = !thingy;
        lastTime = millis();
    }

    CAN.bit_modify(CANCTRL, (1<<REQOP2)|(1<<REQOP1)|(1<<REQOP0), 0);

    if(bluetooth.isConnected()) {
        if(!lastConnectionState) {
            printConnectedMessage();
            lastConnectionState = true;
        }
        
        gatherEngineData(engineData);
        sendEngineDataOverBluetooth(engineData);  
    } else {
        if(lastConnectionState) {
            printDisconnectedMessage();
            lastConnectionState = false;
        }   
    }

    if(!digitalRead(DOWN)) {
        Serial.println("Program Done");
        while(true);
    }
}

void printConnectedMessage() {
    Serial.print("Bluetooth connected!\n\r");
}

void printDisconnectedMessage() {
    Serial.print("Disconnected! Halting communications...");    
    Serial.print("\n\rWaiting for Bluetooth connection...\n\r");
}

void gatherEngineData(float engineData[NUM_PARAMETERS]) {    
    getPieceOfData(engineData, 0,  ENGINE_LOAD_VAL);   
    getPieceOfData(engineData, 1,  ENGINE_COOLANT_TEMP);   
    getPieceOfData(engineData, 2,  ENGINE_RPM);   
    getPieceOfData(engineData, 3,  VEHICLE_SPEED);   
    getPieceOfData(engineData, 4,  THROTTLE_POS);   
    getPieceOfData(engineData, 5,  ENGINE_RUNTIME);   
    getPieceOfData(engineData, 6,  FUEL_LEVEL_INPUT);   
    getPieceOfData(engineData, 7,  AMBIENT_TEMP);   
    getPieceOfData(engineData, 8,  ABS_THROTTLE_B);   
    getPieceOfData(engineData, 9,  ABS_THROTTLE_C);   
    getPieceOfData(engineData, 10,  ACC_PEDAL_POS_D);   
    getPieceOfData(engineData, 11, ACC_PEDAL_POS_E);   
    getPieceOfData(engineData, 12, ACC_PEDAL_POS_F);   
    getPieceOfData(engineData, 13, BATTERY_PACK_LIFE);   
    getPieceOfData(engineData, 14, ENGINE_OIL_TEMP);
}

void getPieceOfData(float engineData[NUM_PARAMETERS], int index, int pid) {
    int requestStatus = -1;
    float tempData = 0.0;
    
    tempData = sendECURequest(pid, &requestStatus);
    if(requestStatus == 0) {
        engineData[index] = tempData;
    }
}

void sendEngineDataOverBluetooth(float engineData[NUM_PARAMETERS]) {
    String engineDataString;

    for(int i = 0; i < NUM_PARAMETERS; i++) {
        engineDataString = String(CAN.PIDs[i]) + "~" + floatToString(engineData[i], 2);
        bluetooth.sendStringWithFlags(engineDataString);
        Serial.println(engineDataString);
    }
}

float sendECURequest(byte pid, int* requestStatus) {
    tCAN message;
    unsigned long timestamp = 0;

    //Timeout of 65 milliseconds, keeps refresh rate of 1Hz in worst case
    unsigned long timeout = (millis() + 65);

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
    (*requestStatus) = 0;

    while(millis() <= timeout) {
        if (CAN.check_message()) {
            if (CAN.get_message(&message, &timestamp)) {
                
                // Check message is the reply and its the right PID
                if((message.id >= PID_REPLY) && (message.data[2] == pid)) {
                    return CAN.calculatePIDvalue(pid, &message);
                }
            }
        }
    }

    (*requestStatus) = -1;
    return 0.0;
}

String floatToString(float value, int places) {
    String floatString = "";
    
    int digit;
    float tens = 0.1;
    int tenscount = 0;
    int i;
    float tempfloat = value;

    // calculate rounding term d: 0.5 / pow(10,places)  
    float d = 0.5;
    if (value < 0) {
        d = -0.5;
    }
    for (i = 0; i < places; i++)
        d /= 10.0;    
    // this small addition, combined with truncation, will round our values properly 
    tempfloat +=  d;

    // first get value tens to be the largest power of ten that's less than the value
    if (value < 0) {
        tempfloat *= -1.0;
    }
    while ((tens * 10.0) <= tempfloat) {
        tens *= 10.0;
        tenscount += 1;
    }


    // write out the negative if needed
    if (value < 0) {
        floatString.concat('-');
    }

    if (tenscount == 0) {
        floatString.concat(0);
    }

    for (i = 0; i < tenscount; i++) {
        digit = (int) (tempfloat / tens);
        floatString.concat(digit);
        
        tempfloat = tempfloat - ((float)digit * tens);
        tens /= 10.0;
    }

    // if no places after decimal, stop now and return
    if (places <= 0) {
        floatString;
    }

    // otherwise, write the point and continue on
    floatString.concat('.');  

    // now write out each decimal place by shifting digits
    //one by one into the ones place and writing the truncated value
    for (i = 0; i < places; i++) {
        tempfloat *= 10.0; 
        digit = (int) tempfloat;
        floatString.concat(digit);  

        tempfloat = tempfloat - (float) digit; 
    }
    
    return floatString;
}

///*converts char to decimal number*/
//int char2num(int chr)
//{
//    switch(chr)
//    {
//    case 48:
//        return 0;
//        break;
//    case 49:
//        return 1;
//        break;
//    case 50:
//        return 2;
//        break;
//    case 51:
//        return 3;
//        break;
//    case 52:
//        return 4;
//        break;
//    case 53:
//        return 5;
//        break;
//    case 54:
//        return 6;
//        break;
//    case 55:
//        return 7;
//        break;
//    case 56:
//        return 8;
//        break;
//    case 57:
//        return 9;
//        break;
//    case 65:
//        return 10;
//        break;
//    case 66:
//        return 11;
//        break;
//    case 67:
//        return 12;
//        break;
//    case 68:
//        return 13;
//        break;
//    case 69:
//        return 14;
//        break;
//    case 70:
//        return 15;
//        break;
//    default:
//        return 0;
//    }
//}
//
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


