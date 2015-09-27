/*
  SysexI2C.h
*/

#ifndef SysexI2C_h
#define SysexI2C_h

#include <Firmata.h>
#include <Wire.h>
#include "SysexCore.h"

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

#define I2C_CONFIG              0x78 // config I2C settings such as delay times and power pins
#define I2C_REQUEST             0x76 // send an I2C read/write request

#define MAX_QUERIES 8
#define REGISTER_NOT_SPECIFIED -1



// move the following defines to Firmata.h?

#define I2C_WRITE B00000000
#define I2C_READ B00001000
#define I2C_READ_CONTINUOUSLY B00010000
#define I2C_STOP_READING B00011000
#define I2C_READ_WRITE_MODE_MASK B00011000
#define I2C_10BIT_ADDRESS_MODE_MASK B00100000

void setI2CHooks(sysexCallbackFunction *h);

void processI2CConfig(byte command, byte argc, byte *argv);
void processI2CRequest(byte command, byte argc, byte *argv);

void readAndReportContinuousI2CData();
void readAndReportI2CData(byte address, int theRegister, byte numBytes);
  
void enableI2CPins();
void disableI2CPins();

extern boolean isI2CEnabled;

#endif  /* SysexI2C_h */
