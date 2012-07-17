#include "MockSerial.h"

MockSerial::MockSerial() : HardwareSerial::HardwareSerial(Serial) {
    reset();
}

void MockSerial::end() {

}

void MockSerial::begin(uint32_t baud) {
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

size_t MockSerial::write(uint8_t value) {
    _out_buf[_out_ptr] = value;
    _out_ptr++;
}

void MockSerial::write(uint8_t * array_ptr, uint32_t length) {
    for (uint32_t i = 0; i < length; i++) {
        _out_buf[_out_ptr + i] = *(array_ptr + i);
    }
    _out_ptr += length;
}

void MockSerial::set_input_buffer(uint8_t * new_buf, uint16_t size) {
    _in_buf = new_buf;
    _in_ptr = 0;
    _size = size;
}

void MockSerial::reset(void) {
    uint8_t tmp1[1024] = { 0x00 };
    uint8_t tmp2[1024] = { 0x00 };
    _out_buf = tmp1;
    _out_ptr = 0; // Current buffer position
    _in_buf = tmp2;
    _in_ptr = 0;
    _baud = 0;
    _size = 0;
}
