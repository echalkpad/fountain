/*
  SysexI2C.h
    This module implements Firmata's I2C capabilities using Sysex commands.
*/

#ifndef SysexI2C_h
#define SysexI2C_h

#include <Firmata.h>
#include <Wire.h>
#include "SysexCore.h"

#define MAX_QUERIES 8
#define REGISTER_NOT_SPECIFIED -1

// move the following defines to Firmata.h?

#define I2C_WRITE B00000000
#define I2C_READ B00001000
#define I2C_READ_CONTINUOUSLY B00010000
#define I2C_STOP_READING B00011000
#define I2C_READ_WRITE_MODE_MASK B00011000
#define I2C_10BIT_ADDRESS_MODE_MASK B00100000

void setSysexI2CHooks(sysexCallbackFunction *h);

void processI2CConfig(byte command, byte argc, byte *argv);
void processI2CRequest(byte command, byte argc, byte *argv);

void readAndReportContinuousI2CData();
void readAndReportI2CData(byte address, int theRegister, byte numBytes);

void enableI2CPins();
void disableI2CPins();

extern boolean isI2CEnabled;

#endif  /* SysexI2C_h */
