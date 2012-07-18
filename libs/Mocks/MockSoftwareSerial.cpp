#include "MockSoftwareSerial.h"

MockSoftwareSerial::MockSoftwareSerial() : SoftwareSerial::SoftwareSerial(0, 1) {
    reset();
}

void MockSoftwareSerial::end() {

}

void MockSoftwareSerial::begin(int baud) {
    _baud = baud;
}

int MockSoftwareSerial::available() {
    return _in_buf.length();
}

int MockSoftwareSerial::read() {
    if(_in_buf.length() > 0) {
		int toRead = (int)_in_buf[0];
		_in_buf = _in_buf.substring(1);
        return toRead;
    } else {
        return -1;
    }
}

size_t MockSoftwareSerial::write(byte value) {
    _out_buf.concat(String((char)value));
}

void MockSoftwareSerial::write(byte* array_ptr, int length) {
    for (int i = 0; i < length; i++) {
        _out_buf.concat(String((char)*(array_ptr + i)));
    }
}

void MockSoftwareSerial::set_input_buffer(String newBuffer) {
    _in_buf = newBuffer;
}

void MockSoftwareSerial::reset(void) {
    _out_buf = String();
    _in_buf = String();
    _baud = 0;
}
