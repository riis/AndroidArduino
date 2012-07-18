#include "MockSerial.h"

MockSerial::MockSerial() : HardwareSerial::HardwareSerial(Serial) {
    reset();
}

void MockSerial::end() {

}

void MockSerial::begin(int baud) {
    _baud = baud;
}

int MockSerial::available() {
    return _in_buf.length();
}

int MockSerial::read() {
    if(_in_buf.length() > 0) {
		int toRead = (int)_in_buf[0];
		_in_buf = _in_buf.substring(1);
        return toRead;
    } else {
        return -1;
    }
}

size_t MockSerial::write(byte value) {
    _out_buf.concat(String((char)value));
}

void MockSerial::write(byte* array_ptr, int length) {
    for (int i = 0; i < length; i++) {
        _out_buf.concat(String((char)*(array_ptr + i)));
    }
}

void MockSerial::set_input_buffer(String newBuffer) {
    _in_buf = newBuffer;
}

void MockSerial::reset(void) {
    _out_buf = String();
    _in_buf = String();
    _baud = 0;
}

/*
MockSerial::MockSerial() : HardwareSerial::HardwareSerial(Serial) {
    reset();
}

void MockSerial::end() {

}

void MockSerial::begin(int baud) {
    _baud = baud;
}

int MockSerial::available() {
    return _size - _in_ptr;
}

int MockSerial::read() {
    if (_in_ptr < _size) {
        return (int) _in_buf[_in_ptr++];
    } else {
        return -1;
    }
}

size_t MockSerial::write(byte value) {
    _out_buf[_out_ptr] = value;
    _out_ptr++;
}

void MockSerial::write(byte * array_ptr, int length) {
    for (int i = 0; i < length; i++) {
        _out_buf[_out_ptr + i] = *(array_ptr + i);
    }
    _out_ptr += length;
}

void MockSerial::set_input_buffer(byte * new_buf, word size) {
    _in_buf = new_buf;
    _in_ptr = 0;
    _size = size;
}

void MockSerial::reset(void) {
    byte tmp1[1024] = { 0x00 };
    byte tmp2[1024] = { 0x00 };
    _out_buf = tmp1;
    _out_ptr = 0; // Current buffer position
    _in_buf = tmp2;
    _in_ptr = 0;
    _baud = 0;
    _size = 0;
} */
