/*
  SysexCore.h
    This module implements Firmata's processing of the "core" Sysex command codes.
*/

#ifndef SysexCore_h
#define SysexCore_h

#include <Firmata.h>
#include <Servo.h>
#include "DirectCore.h"

#define MINIMUM_SAMPLING_INTERVAL 10

void setSysexCoreHooks(sysexCallbackFunction *h);

void processAnalogMappingQuery(byte command, byte argc, byte *argv);
void processCapabilityQuery(byte command, byte argc, byte *argv);
void processExtendedAnalog(byte command, byte argc, byte *argv);
void processPinStateQuery(byte command, byte argc, byte *argv);
void processSamplingInterval(byte command, byte argc, byte *argv);
void processServoConfig(byte command, byte argc, byte *argv);

extern sysexCallbackFunction hooks[127];	// command jump table, 1 pointer per possible command

extern int samplingInterval;
extern Servo servos[MAX_SERVOS];

extern byte pinConfig[TOTAL_PINS];         // configuration of every pin
extern int pinState[TOTAL_PINS];           // any value that has been written

#endif  /* SysexCore_h */
