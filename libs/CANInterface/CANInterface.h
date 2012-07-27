/*
  Test.h - Test library for Wiring - description
  Copyright (c) 2006 John Doe.  All right reserved.
*/

// ensure this library description is only included once
#ifndef CANInterface_h
#define CANInterface_h

// include types & constants of Wiring core API
#include <Arduino.h>
#include "mcp2515_defs.h"
#include "SPI.h"
#include "global.h"
#include <inttypes.h>

#define LED2 8
#define LED3 7

#define	MCP2515_INT 	2

#define CANSPEED_125 	7		// CAN speed at 125 kbps
#define CANSPEED_250  	3		// CAN speed at 250 kbps
#define CANSPEED_500	1		// CAN speed at 500 kbps
#define	CANSPEED_95		6		// CAN speed at 95.238 kbps

#define ENGINE_LOAD_VAL		0x04
#define ENGINE_COOLANT_TEMP 0x05
#define ENGINE_RPM          0x0C
#define VEHICLE_SPEED       0x0D
#define THROTTLE_POS        0x11
#define ENGINE_RUNTIME		0x1F
#define FUEL_LEVEL_INPUT	0x2F
#define AMBIENT_TEMP		0x46
#define ABS_THROTTLE_B		0x47
#define ABS_THROTTLE_C		0x48
#define ACC_PEDAL_POS_D		0x49
#define ACC_PEDAL_POS_E		0x4A
#define ACC_PEDAL_POS_F		0x4B
#define BATTERY_PACK_LIFE	0x5B
#define ENGINE_OIL_TEMP		0x4C
						   
#define VIN_MODE			0x09
#define VIN_PID				0x02

#define PID_REQUEST         0x7DF
#define PID_REPLY			0x7E8

#define CAN_CS				53

typedef struct
{
	uint16_t id;
	struct {
		int8_t rtr : 1;
		uint8_t length : 4;
	} header;
	uint8_t data[8];
} tCAN;

// library interface description
class CANInterface
{
  // user-accessible "public" interface
  public:
    //CANInterface(void);
    bool Init(int);
    void CAN_Int();
    void doSomething(void);
    void write_register( uint8_t address, uint8_t data );
    uint8_t read_register(uint8_t address);
    uint8_t check_message(void) ;
    void bit_modify(uint8_t address, uint8_t mask, uint8_t data);
    uint8_t read_status(uint8_t type);
    uint8_t check_free_buffer(void);
    uint8_t get_message(tCAN *message, unsigned long *timestamp);
    uint8_t send_message(tCAN *message);
	
	float calculatePIDvalue(byte PID, tCAN *message);

	byte PIDs[15];
  // library-accessible "private" interface
  private:
    int value;
    void doSomethingSecret(void);
};

extern CANInterface CAN;

#endif

