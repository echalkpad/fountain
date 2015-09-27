/*
  SysexCore.h
*/

#ifndef SysexCore_h
#define SysexCore_h

#include <Firmata.h>
#include <Servo.h>

// Define the "core" Sysex command values

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
#define PIN_STATE_QUERY         0x6D // ask for a pin's current mode and value
#define SAMPLING_INTERVAL       0x7A // set the poll rate of the main loop
#define SERVO_CONFIG            0x70 // set max angle, minPulse, maxPulse, freq

#define MINIMUM_SAMPLING_INTERVAL 10

void analogWriteCallback(byte pin, int value);
void digitalWriteCallback(byte port, int value);
void reportAnalogCallback(byte analogPin, int value);
void reportDigitalCallback(byte port, int value);
void setPinModeCallback(byte pin, int mode);
void checkDigitalInputs(void);
void outputPort(byte portNumber, byte portValue, byte forceSend);

void setCoreHooks(sysexCallbackFunction *h);

void processSamplingInterval(byte command, byte argc, byte *argv);
void processServoConfig(byte command, byte argc, byte *argv);
void processExtendedAnalog(byte command, byte argc, byte *argv);
void processPinStateQuery(byte command, byte argc, byte *argv);
void processCapabilityQuery(byte command, byte argc, byte *argv);
void processAnalogMappingQuery(byte command, byte argc, byte *argv);

extern int samplingInterval;
extern Servo servos[MAX_SERVOS];

extern byte pinConfig[TOTAL_PINS];         // configuration of every pin
extern byte portConfigInputs[TOTAL_PORTS]; // each bit: 1 = pin in INPUT, 0 = anything else
extern int pinState[TOTAL_PINS];           // any value that has been written
extern int analogInputsToReport; // bitwise array to store pin reporting

extern byte reportPINs[TOTAL_PORTS];       // 1 = report this port, 0 = silence
extern byte previousPINs[TOTAL_PORTS];     // previous 8 bits sent

#endif  /* SysexCore_h */
