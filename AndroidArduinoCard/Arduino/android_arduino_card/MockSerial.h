#ifndef MOCKSERIAL_H_
#define MOCKSERIAL_H_

#include <Arduino.h>

class MockSerial : public HardwareSerial {
public:

    uint8_t * _out_buf; //The output buffer array.
    uint16_t _out_ptr;  //The next open output buffer position.
    uint8_t * _in_buf;  //The input buffer array.
    uint16_t _in_ptr;   //The next input buffer position which will be returned by a call to read().
    uint32_t _baud;     //The current baud rate.
    uint16_t _size;     //The size of the input data.

    MockSerial(); //Creates a new mock serial buffer with its own buffer.

    void set_input_buffer(uint8_t * new_buf, uint16_t size); //Set the input buffer data.
    void reset(void); //Reset the serial buffers.

    void end(void); //End the serial communications. This is a no-op.
    void begin(uint32_t baud); //Begin serial communications.
  
    int available(void); //Check the number of bytes available in the input buffer.
    int read(void); //Read a byte from the input buffer.
  
    size_t write(uint8_t value); //Write a byte to the output buffer.
    void write(uint8_t * values, uint32_t length); //Write a byte array to the output buffer.
};

#endif
