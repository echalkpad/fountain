/*
  StandardSysex.h
*/

#ifndef StandardSysex_h
#define StandardSysex_h

#include <Firmata.h>

// Define the "standard" Sysex command values

// **** - implemented in Firmata library
// ---- - not implemented

// ****#define REPORT_FIRMWARE         0x79 // report name and version of the firmware
// ****#define STRING_DATA             0x71 // a string message with 14-bits per char
// ----#define ENCODER_DATA            0x61 // reply with encoders current positions
// ----#define ONEWIRE_DATA            0x73 // send an OneWire read/write/reset/select/skip/search request
// ----#define SCHEDULER_DATA          0x7B // send a createtask/deletetask/addtotask/schedule/querytasks/querytask request to the scheduler
// ----#define SHIFT_DATA              0x75 // a bitstream to/from a shift register
// ----#define STEPPER_DATA            0x72 // control a stepper motor
// ----#define SYSEX_NON_REALTIME      0x7E // MIDI Reserved for non-realtime messages
// ----#define SYSEX_REALTIME          0x7F // MIDI Reserved for realtime messages

#define ANALOG_MAPPING_RESPONSE 0x6A // reply with mapping info
#define CAPABILITY_RESPONSE     0x6C // reply with supported modes and resolution
#define I2C_REPLY               0x77 // a reply to an I2C read request
#define PIN_STATE_RESPONSE      0x6E // reply with pin's current mode and value

#define ANALOG_MAPPING_QUERY    0x69 // ask for mapping of analog to pin numbers
#define CAPABILITY_QUERY        0x6B // ask for supported modes and resolution of all pins
#define EXTENDED_ANALOG         0x6F // analog write (PWM, Servo, etc) to any pin
#define I2C_CONFIG              0x78 // config I2C settings such as delay times and power pins
#define I2C_REQUEST             0x76 // send an I2C read/write request
#define PIN_STATE_QUERY         0x6D // ask for a pin's current mode and value
#define SAMPLING_INTERVAL       0x7A // set the poll rate of the main loop
#define SERVO_CONFIG            0x70 // set max angle, minPulse, maxPulse, freq


// move the following defines to Firmata.h?
#define I2C_WRITE B00000000
#define I2C_READ B00001000
#define I2C_READ_CONTINUOUSLY B00010000
#define I2C_STOP_READING B00011000
#define I2C_READ_WRITE_MODE_MASK B00011000
#define I2C_10BIT_ADDRESS_MODE_MASK B00100000

void setSysexHooks(sysexCallbackFunction *h);

void processSamplingInterval(byte command, byte argc, byte *argv);
void processI2CConfig(byte command, byte argc, byte *argv);
void processI2CRequest(byte command, byte argc, byte *argv);
void processServoConfig(byte command, byte argc, byte *argv);
void processExtendedAnalog(byte command, byte argc, byte *argv);
void processPinStateQuery(byte command, byte argc, byte *argv);
void processCapabilityQuery(byte command, byte argc, byte *argv);
void processAnalogMappingQuery(byte command, byte argc, byte *argv);

#endif  /* StandardSysex_h */
