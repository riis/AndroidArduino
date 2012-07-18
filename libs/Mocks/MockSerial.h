#ifndef MOCKSERIAL_H_
#define MOCKSERIAL_H_

#include <Arduino.h>

class MockSerial : public HardwareSerial {
public:

    String _out_buf; //The output buffer array.
    //word  _out_ptr;  //The next open output buffer position.
    String _in_buf;  //The input buffer array.
    //word  _in_ptr;   //The next input buffer position which will be returned by a call to read().
    int   _baud;     //The current baud rate.
    //word  _size;     //The size of the input data.

    MockSerial(); //Creates a new mock serial buffer with its own buffer.

    void set_input_buffer(String newBuffer); //Set the input buffer data.
    void reset(void); //Reset the serial buffers.

    void end(void); //End the serial communications. This is a no-op.
    void begin(int baud); //Begin serial communications.
  
    int available(void); //Check the number of bytes available in the input buffer.
    int read(void); //Read a byte from the input buffer.
  
    size_t write(byte value); //Write a byte to the output buffer.
    void write(byte* values, int length); //Write a byte array to the output buffer.
};

#endif
