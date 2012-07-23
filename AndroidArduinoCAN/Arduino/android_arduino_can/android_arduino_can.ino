#include <Canbus.h>
  
char buffer[512];  //Data will be temporarily stored to this buffer before being written to the file
 
void setup() {
    Serial.begin(115200);
    
    if(Canbus.init(CANSPEED_500))  /* Initialise MCP2515 CAN controller at the specified speed */
    {
        Serial.print("CAN Init ok");
    } 
    else
    {
        Serial.print("Can't init CAN");
    } 
   
    delay(1000);
}
 

void loop() {
    if(Canbus.ecu_req(ENGINE_RPM,buffer) == 1)
    {
        Serial.print("Engine RPM: ");
        Serial.println(buffer);
    } 
   
    if(Canbus.ecu_req(VEHICLE_SPEED,buffer) == 1)
    {
        Serial.print("Vehicle Speed: ");
        Serial.print(buffer);
    }
  
    if(Canbus.ecu_req(ENGINE_COOLANT_TEMP,buffer) == 1)
    {
        Serial.print("Engine Coolant Temp: ");
        Serial.print(buffer);
    }
  
    if(Canbus.ecu_req(THROTTLE,buffer) == 1)
    {
        Serial.print("Throttle: ");
        Serial.print(buffer);
    }  
}
