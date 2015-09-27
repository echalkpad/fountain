/*
  MsgBasic.h
*/

#ifndef MsgBasic_h
#define MsgBasic_h

#include <Firmata.h>
#include <Servo.h>

#define MINIMUM_SAMPLING_INTERVAL 10

void analogWriteCallback(byte pin, int value);
void digitalWriteCallback(byte port, int value);
void reportAnalogCallback(byte analogPin, int value);
void reportDigitalCallback(byte port, int value);
void setPinModeCallback(byte pin, int mode);
void checkDigitalInputs(void);
void outputPort(byte portNumber, byte portValue, byte forceSend);

// extern int samplingInterval;
extern Servo servos[MAX_SERVOS];

extern byte pinConfig[TOTAL_PINS];         // configuration of every pin
extern byte portConfigInputs[TOTAL_PORTS]; // each bit: 1 = pin in INPUT, 0 = anything else
extern int pinState[TOTAL_PINS];           // any value that has been written
extern int analogInputsToReport; // bitwise array to store pin reporting

extern byte reportPINs[TOTAL_PORTS];       // 1 = report this port, 0 = silence
extern byte previousPINs[TOTAL_PORTS];     // previous 8 bits sent

#endif  /* MsgBasic_h */
