/*
 * Firmata is a generic protocol for communicating with microcontrollers
 * from software on a host computer. It is intended to work with
 * any host computer software package.
 *
 * To download a host software package, please clink on the following link
 * to open the download page in your default browser.
 *
 * http://firmata.org/wiki/Download
 */

/*
  Copyright (C) 2006-2008 Hans-Christoph Steiner.  All rights reserved.
  Copyright (C) 2010-2011 Paul Stoffregen.  All rights reserved.
  Copyright (C) 2009 Shigeru Kobayashi.  All rights reserved.
  Copyright (C) 2009-2011 Jeff Hoefs.  All rights reserved.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  See file LICENSE.txt for further informations on licensing terms.

  formatted using the GNU C formatting and indenting

  Oct 2015  DWJohnson.  Refactor StandardFirmata to separate FirmataMain and the various
            functions that implement the program's capabilities.  The primary reason for
            this is to facilitate adding extended Sysex commands, but it also helps
            separate the top level application flow from the details of each functional
            capability.
*/


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

// #define ANALOG_MAPPING_RESPONSE 0x6A // reply with mapping info
// #define CAPABILITY_RESPONSE     0x6C // reply with supported modes and resolution
// #define I2C_REPLY               0x77 // a reply to an I2C read request
// #define PIN_STATE_RESPONSE      0x6E // reply with pin's current mode and value

// #define ANALOG_MAPPING_QUERY    0x69 // ask for mapping of analog to pin numbers
// #define CAPABILITY_QUERY        0x6B // ask for supported modes and resolution of all pins
// #define EXTENDED_ANALOG         0x6F // analog write (PWM, Servo, etc) to any pin
// #define PIN_STATE_QUERY         0x6D // ask for a pin's current mode and value
// #define SAMPLING_INTERVAL       0x7A // set the poll rate of the main loop
// #define SERVO_CONFIG            0x70 // set max angle, minPulse, maxPulse, freq

#include <netinet/in.h>
#include <Firmata.h>
#include <Servo.h>
#include <Wire.h>

#include "FirmataMainOptions.h"
#include "DirectCore.h"
#include "SysexCore.h"
#include "SysexI2C.h"

/*==============================================================================
 * GLOBAL VARIABLES
 *============================================================================*/

/* timer variables */
unsigned long currentMillis;        // store the current value from millis()
unsigned long previousMillis;       // for comparison with currentMillis

/*==============================================================================
 * SETUP()
 *============================================================================*/

void setup()
{

  Firmata.setFirmwareVersion(FIRMATA_MAJOR_VERSION, FIRMATA_MINOR_VERSION);

  // Set up the direct callbacks

  Firmata.attach(ANALOG_MESSAGE, analogWriteCallback);
  Firmata.attach(DIGITAL_MESSAGE, digitalWriteCallback);
  Firmata.attach(REPORT_ANALOG, reportAnalogCallback);
  Firmata.attach(REPORT_DIGITAL, reportDigitalCallback);
  Firmata.attach(SET_PIN_MODE, setPinModeCallback);
  Firmata.attach(START_SYSEX, sysexCallback);
  Firmata.attach(SYSTEM_RESET, systemResetCallback);

  // Set up the Sysex extended process function jump table

  setSysexCoreHooks(hooks);
  setSysexI2CHooks(hooks);

  Firmata.begin(57600);
  systemResetCallback();  // reset to default config
}

/*==============================================================================
 * LOOP()
 *============================================================================*/
void loop()
{
  byte pin, analogPin;

  /* DIGITALREAD - as fast as possible, check for changes and output them to the
   * FTDI buffer using Serial.print()  */

  checkDigitalInputs();

  /* SERIALREAD - processing incoming messagse as soon as possible, while still
   * checking digital inputs.  */

  while(Firmata.available()) {
    Firmata.processInput();
  }

  /* SEND FTDI WRITE BUFFER - make sure that the FTDI buffer doesn't go over
   * 60 bytes. use a timer to sending an event character every 4 ms to
   * trigger the buffer to dump. */

  currentMillis = millis();
  if (currentMillis - previousMillis > samplingInterval) {
    previousMillis += samplingInterval;
    /* ANALOGREAD - do all analogReads() at the configured sampling interval */
    for(pin=0; pin<TOTAL_PINS; pin++) {
      if (IS_PIN_ANALOG(pin) && pinConfig[pin] == ANALOG) {
        analogPin = PIN_TO_ANALOG(pin);
        if (analogInputsToReport & (1 << analogPin)) {
          Firmata.sendAnalog(analogPin, analogRead(analogPin));
        }
      }
    }

    // report i2c data for all devices with read continuous mode enabled

    readAndReportContinuousI2CData();

  }
}
