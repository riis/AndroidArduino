#include <Bluetooth.h>
#include <SoftwareSerial.h>

#define ENGINE_TEMP      "05"
#define ENGINE_RPM       "0C"
#define VEHICLE_SPEED    "0D"
#define THROTTLE_POS     "11"
#define RUN_TIME         "1F"
#define FUEL_LEVEL       "2F"
#define AMBIENT_TEMP     "46"
#define ABS_THROTTLE_B   "47"
#define HYBRID_LIFE      "5B"
#define OIL_TEMP         "5C"

//Bluetooth
#define RX 62
#define TX 7

SoftwareSerial bluetoothSerial(RX, TX);
Bluetooth bluetooth("AndroidArduinoCANBT", bluetoothSerial, true, 3);

boolean lastConnectionState = false;

void setup() {
    Serial.begin(9600);
    Serial1.begin(9600); 

    if(!bluetooth.beginBluetooth()) {
        Serial.println("\n\rHalting program...");
        while(true) { 
        }
    }

    Serial.println("Waiting for connection to OBD board...");

    Serial1.print("AT Z\r");
    waitForPrompt();
    Serial1.print("AT SP A0\r");
    waitForPrompt();

    Serial.println("Waiting for Bluetooth connection...");
}

void waitForPrompt() {
    char testChar = 0;
    while(testChar != '>') {
        if(Serial1.available()) {
            testChar = Serial1.read();
        } 
    }
}

byte doLater = 0;
void loop() {
    bluetooth.process();

    if(bluetooth.isConnected()) {
        if(!lastConnectionState) {
            printConnectedMessage();
            lastConnectionState = true;
        }
        
        Serial.print('\f');
        requestDataAndSend(ENGINE_TEMP);
        requestDataAndSend(ENGINE_RPM);
        requestDataAndSend(VEHICLE_SPEED);
        requestDataAndSend(THROTTLE_POS);
        requestDataAndSend(RUN_TIME);
        requestDataAndSend(ABS_THROTTLE_B);
    
        if(doLater > 10) {
            doLater = 0; 
            requestDataAndSend(HYBRID_LIFE);
            requestDataAndSend(OIL_TEMP);
            requestDataAndSend(FUEL_LEVEL);
            requestDataAndSend(AMBIENT_TEMP);
        }
    
        doLater++;
    } else {
        if(lastConnectionState) {
            printDisconnectedMessage();
            lastConnectionState = false;
        } 
    }
}

void printConnectedMessage() {
    Serial.print("Bluetooth connected!\n\r");
}

void printDisconnectedMessage() {
    Serial.print("Disconnected! Halting communications...");    
    Serial.print("\n\rWaiting for Bluetooth connection...\n\r");
}

void requestDataAndSend(String data) {
    Serial1.print("01 " + data + "\r");

    boolean foundPrompt = false;

    char line[256];
    int lineLen = 0;

    while(!foundPrompt) {
        while(Serial1.available()) {
            char x = Serial1.read();
            if(x == '>') {
                foundPrompt = true;
            } 
            else {
                line[lineLen] = x;
                lineLen++;

                if(x == '\r') {
                    actOnLine(line, lineLen, data);
                    lineLen = 0;
                }
            }
        }
    }
}

void actOnLine(char line[256], int lineLen, String PID) {
    if(charArrayEquals(line, lineLen, "01 " + PID + "\r") ||
        charArrayEquals(line, lineLen, "NO DATA\r") ||
        charArrayEquals(line, lineLen, "SEARCHING...\r") ||
        charArrayEquals(line, lineLen, "\r")) {

        return;
    }

    char hexByteStrings[8][2];
    for(int i = 0; i < 4; i++) {
        hexByteStrings[i][0] = '0';
        hexByteStrings[i][1] = '0'; 
    }

    int numBytes = (lineLen / 3);
    for(int i = 0; i < numBytes; i++) {
        hexByteStrings[i][0] = line[(i * 3)];
        hexByteStrings[i][1] = line[(i * 3) + 1]; 
    }
    
    sendDataToAndroid(PID, hexByteStrings);

//    byte bytes[8];
//    for(int i = 0; i < 8; i++) {
//        bytes[i] = parseHexByte(hexByteStrings[i]); 
//    }
//
//    parseDataAndReport(data, bytes);
}

void sendDataToAndroid(String PID, char bytes[8][2]) {
    String dataString = PID + "~" + bytes[2][0] + bytes[2][1] + "~" + bytes[3][0] + bytes[3][1];
    bluetooth.sendStringWithFlags(dataString);
}

boolean charArrayEquals(char* array1, int array1Len, char* array2) {
    for(int i = 0; i < array1Len; i++) {
        if(array1[i] != array2[i]) {
            return false; 
        }
    }

    return true;
}

boolean charArrayEquals(char* array1, int array1Len, String array2) {
    for(int i = 0; i < array1Len; i++) {
        if(array1[i] != array2[i]) {
            return false; 
        }
    }

    return true;
}

byte parseHexByte(char hexByte[2]) {
    byte digit1 = hexDigitToByte(hexByte[0]);
    byte digit2 = hexDigitToByte(hexByte[1]); 

    byte wholeDigit = ((digit1 * 16) + digit2);

    return wholeDigit;
}

byte hexDigitToByte(char hexDigit) {
    switch(hexDigit) {
    case '0': 
        return 0;
    case '1': 
        return 1;
    case '2': 
        return 2;
    case '3': 
        return 3;
    case '4': 
        return 4;
    case '5': 
        return 5;
    case '6': 
        return 6;
    case '7': 
        return 7;
    case '8': 
        return 8;
    case '9': 
        return 9;
    case 'A': 
        return 10;
    case 'B': 
        return 11;
    case 'C': 
        return 12;
    case 'D': 
        return 13;
    case 'E': 
        return 14;
    case 'F': 
        return 15;
    default: 
        return -1; 
    }
}

void parseDataAndReport(String data, byte bytes[8]) {
    int engineData = calculatePIDvalue(data, bytes);
    String dataString = data + "~" + String(engineData);
    bluetooth.sendStringWithFlags(dataString);
}

int calculatePIDvalue(String PID, byte bytes[8]) {
    if(PID == ENGINE_TEMP)   // (A - 40)
        return (bytes[2] - 40);

    else if(PID == ENGINE_RPM)            // ((A * 256) + B) / 4
        return ((bytes[2] * 256) + bytes[3]) / 4;

    else if(PID == VEHICLE_SPEED)         // A 
        return (bytes[2] * 0.621371);

    else if(PID == THROTTLE_POS)		    // (A * 100) / 255
        return (bytes[2] * 100) / 255;

    else if(PID == RUN_TIME)        // (A * 256) + B
        return (bytes[2] * 256) + bytes[3];

    else if(PID == FUEL_LEVEL)      // (A * 100) / 255
        return (bytes[2] * 100) / 255;

    else if(PID == AMBIENT_TEMP)          // (A - 40)
        return (bytes[2] - 40);

    else if(PID == ABS_THROTTLE_B)        // (A * 100) / 255
        return (bytes[2] * 100) / 255;

    else if(PID == HYBRID_LIFE)     // (A * 100) / 255
        return (bytes[2] * 100) / 255;

    else if(PID == OIL_TEMP)       // (A - 40)
        return (bytes[2] - 40);

    else
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

//if(Serial.available()) {
//  Serial1.write(Serial.read()); 
//}
//
//if(Serial1.available()) {
//  char x = Serial1.read();
//  if(x == '\r') {Serial.write('\n');}
//  Serial.write(x); 
//}


