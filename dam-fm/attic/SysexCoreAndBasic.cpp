/*
  This module implements a method of extending Firmata's processing of Sysex command codes.
*/

#include "SysexCore.h"
#include "SysexI2C.h"

int samplingInterval = 19;          // how often to run the main loop (in ms)
Servo servos[MAX_SERVOS];
int analogInputsToReport = 0; // bitwise array to store pin reporting

byte pinConfig[TOTAL_PINS];         // configuration of every pin
byte portConfigInputs[TOTAL_PORTS]; // each bit: 1 = pin in INPUT, 0 = anything else
int pinState[TOTAL_PINS];           // any value that has been written

/* digital input ports */
byte reportPINs[TOTAL_PORTS];       // 1 = report this port, 0 = silence
byte previousPINs[TOTAL_PORTS];     // previous 8 bits sent

// Initialize our part of the Sysex command jump table

void setCoreHooks(sysexCallbackFunction *h) {
	h[SAMPLING_INTERVAL] = processSamplingInterval;
	h[SERVO_CONFIG] = processServoConfig;
	h[EXTENDED_ANALOG] = processExtendedAnalog;
	h[PIN_STATE_QUERY] = processPinStateQuery;
	h[CAPABILITY_QUERY] = processCapabilityQuery;
	h[ANALOG_MAPPING_QUERY] = processAnalogMappingQuery;
}

// Define the functions that implement the core Sysex capabilities

void processSamplingInterval(byte command, byte argc, byte *argv) {
	if (argc > 1) {
		samplingInterval = argv[0] + (argv[1] << 7);
		if (samplingInterval < MINIMUM_SAMPLING_INTERVAL) {
			samplingInterval = MINIMUM_SAMPLING_INTERVAL;
		}
	} else {
		//Firmata.sendString("Not enough data");
	}
}

void processServoConfig(byte command, byte argc, byte *argv) {
	if (argc > 4) {
		// these vars are here for clarity, they'll optimized away by the compiler
		byte pin = argv[0];
		int minPulse = argv[1] + (argv[2] << 7);
		int maxPulse = argv[3] + (argv[4] << 7);

		if (IS_PIN_SERVO(pin)) {
			if (servos[PIN_TO_SERVO(pin)].attached())
				servos[PIN_TO_SERVO(pin)].detach();
			servos[PIN_TO_SERVO(pin)].attach(PIN_TO_DIGITAL(pin), minPulse, maxPulse);
			setPinModeCallback(pin, SERVO);
		}
	}
}

void processExtendedAnalog(byte command, byte argc, byte *argv) {
	if (argc > 1) {
		int val = argv[1];
		if (argc > 2) val |= (argv[2] << 7);
		if (argc > 3) val |= (argv[3] << 14);
		analogWriteCallback(argv[0], val);
	}
}

void processPinStateQuery(byte command, byte argc, byte *argv) {
	if (argc > 0) {
		byte pin = argv[0];
		Firmata.write(START_SYSEX);
		Firmata.write(PIN_STATE_RESPONSE);
		Firmata.write(pin);
		if (pin < TOTAL_PINS) {
			Firmata.write((byte)pinConfig[pin]);
			Firmata.write((byte)pinState[pin] & 0x7F);
			if (pinState[pin] & 0xFF80) Firmata.write((byte)(pinState[pin] >> 7) & 0x7F);
			if (pinState[pin] & 0xC000) Firmata.write((byte)(pinState[pin] >> 14) & 0x7F);
		}
		Firmata.write(END_SYSEX);
	}
}

void processCapabilityQuery(byte command, byte argc, byte *argv) {
	Firmata.write(START_SYSEX);
	Firmata.write(CAPABILITY_RESPONSE);
	for (byte pin = 0; pin < TOTAL_PINS; pin++) {
		if (IS_PIN_DIGITAL(pin)) {
			Firmata.write((byte)INPUT);
			Firmata.write(1);
			Firmata.write((byte)OUTPUT);
			Firmata.write(1);
		}
		if (IS_PIN_ANALOG(pin)) {
			Firmata.write(ANALOG);
			Firmata.write(10);
		}
		if (IS_PIN_PWM(pin)) {
			Firmata.write(PWM);
			Firmata.write(8);
		}
		if (IS_PIN_SERVO(pin)) {
			Firmata.write(SERVO);
			Firmata.write(14);
		}
		if (IS_PIN_I2C(pin)) {
			Firmata.write(I2C);
			Firmata.write(1);  // to do: determine appropriate value
		}
		Firmata.write(127);
	}
	Firmata.write(END_SYSEX);

}

void processAnalogMappingQuery(byte command, byte argc, byte *argv) {
	Firmata.write(START_SYSEX);
	Firmata.write(ANALOG_MAPPING_RESPONSE);
	for (byte pin = 0; pin < TOTAL_PINS; pin++) {
		Firmata.write(IS_PIN_ANALOG(pin) ? PIN_TO_ANALOG(pin) : 127);
	}
	Firmata.write(END_SYSEX);
}


void analogWriteCallback(byte pin, int value)
{
  if (pin < TOTAL_PINS) {
    switch(pinConfig[pin]) {
    case SERVO:
      if (IS_PIN_SERVO(pin))
        servos[PIN_TO_SERVO(pin)].write(value);
        pinState[pin] = value;
      break;
    case PWM:
      if (IS_PIN_PWM(pin))
        analogWrite(PIN_TO_PWM(pin), value);
        pinState[pin] = value;
      break;
    }
  }
}

// -----------------------------------------------------------------------------
/* sets the pin mode to the correct state and sets the relevant bits in the
 * two bit-arrays that track Digital I/O and PWM status
 */
void setPinModeCallback(byte pin, int mode)
{
  if (pinConfig[pin] == I2C && isI2CEnabled && mode != I2C) {
    // disable i2c so pins can be used for other functions
    // the following if statements should reconfigure the pins properly
    disableI2CPins();
  }
  if (IS_PIN_SERVO(pin) && mode != SERVO && servos[PIN_TO_SERVO(pin)].attached()) {
    servos[PIN_TO_SERVO(pin)].detach();
  }
  if (IS_PIN_ANALOG(pin)) {
    reportAnalogCallback(PIN_TO_ANALOG(pin), mode == ANALOG ? 1 : 0); // turn on/off reporting
  }
  if (IS_PIN_DIGITAL(pin)) {
    if (mode == INPUT) {
      portConfigInputs[pin/8] |= (1 << (pin & 7));
    } else {
      portConfigInputs[pin/8] &= ~(1 << (pin & 7));
    }
  }
  pinState[pin] = 0;
  switch(mode) {
  case ANALOG:
    if (IS_PIN_ANALOG(pin)) {
      if (IS_PIN_DIGITAL(pin)) {
        pinMode(PIN_TO_DIGITAL(pin), INPUT); // disable output driver
        digitalWrite(PIN_TO_DIGITAL(pin), LOW); // disable internal pull-ups
      }
      pinConfig[pin] = ANALOG;
    }
    break;
  case INPUT:
    if (IS_PIN_DIGITAL(pin)) {
      pinMode(PIN_TO_DIGITAL(pin), INPUT); // disable output driver
      digitalWrite(PIN_TO_DIGITAL(pin), LOW); // disable internal pull-ups
      pinConfig[pin] = INPUT;
    }
    break;
  case OUTPUT:
    if (IS_PIN_DIGITAL(pin)) {
      digitalWrite(PIN_TO_DIGITAL(pin), LOW); // disable PWM
      pinMode(PIN_TO_DIGITAL(pin), OUTPUT);
      pinConfig[pin] = OUTPUT;
    }
    break;
  case PWM:
    if (IS_PIN_PWM(pin)) {
      pinMode(PIN_TO_PWM(pin), OUTPUT);
      analogWrite(PIN_TO_PWM(pin), 0);
      pinConfig[pin] = PWM;
    }
    break;
  case SERVO:
    if (IS_PIN_SERVO(pin)) {
      pinConfig[pin] = SERVO;
      if (!servos[PIN_TO_SERVO(pin)].attached()) {
          servos[PIN_TO_SERVO(pin)].attach(PIN_TO_DIGITAL(pin));
      }
    }
    break;
  case I2C:
    if (IS_PIN_I2C(pin)) {
      // mark the pin as i2c
      // the user must call I2C_CONFIG to enable I2C for a device
      pinConfig[pin] = I2C;
    }
    break;
  default:
    Firmata.sendString("Unknown pin mode"); // TODO: put error msgs in EEPROM
  }
  // TODO: save status to EEPROM here, if changed
}

// sets bits in a bit array (int) to toggle the reporting of the analogIns
void reportAnalogCallback(byte analogPin, int value)
{
  if (analogPin < TOTAL_ANALOG_PINS) {
    if(value == 0) {
      analogInputsToReport = analogInputsToReport &~ (1 << analogPin);
    } else {
      analogInputsToReport = analogInputsToReport | (1 << analogPin);
    }
  }
  // TODO: save status to EEPROM here, if changed
}


void digitalWriteCallback(byte port, int value)
{
  byte pin, lastPin, mask=1, pinWriteMask=0;

  if (port < TOTAL_PORTS) {
    // create a mask of the pins on this port that are writable.
    lastPin = port*8+8;
    if (lastPin > TOTAL_PINS) lastPin = TOTAL_PINS;
    for (pin=port*8; pin < lastPin; pin++) {
      // do not disturb non-digital pins (eg, Rx & Tx)
      if (IS_PIN_DIGITAL(pin)) {
        // only write to OUTPUT and INPUT (enables pullup)
        // do not touch pins in PWM, ANALOG, SERVO or other modes
        if (pinConfig[pin] == OUTPUT || pinConfig[pin] == INPUT) {
          pinWriteMask |= mask;
          pinState[pin] = ((byte)value & mask) ? 1 : 0;
        }
      }
      mask = mask << 1;
    }
    writePort(port, (byte)value, pinWriteMask);
  }
}

void reportDigitalCallback(byte port, int value)
{
  if (port < TOTAL_PORTS) {
    reportPINs[port] = (byte)value;
  }
  // do not disable analog reporting on these 8 pins, to allow some
  // pins used for digital, others analog.  Instead, allow both types
  // of reporting to be enabled, but check if the pin is configured
  // as analog when sampling the analog inputs.  Likewise, while
  // scanning digital pins, portConfigInputs will mask off values from any
  // pins configured as analog
}

void outputPort(byte portNumber, byte portValue, byte forceSend)
{
  // pins not configured as INPUT are cleared to zeros
  portValue = portValue & portConfigInputs[portNumber];
  // only send if the value is different than previously sent
  if(forceSend || previousPINs[portNumber] != portValue) {
    Firmata.sendDigitalPort(portNumber, portValue);
    previousPINs[portNumber] = portValue;
  }
}

/* -----------------------------------------------------------------------------
 * check all the active digital inputs for change of state, then add any events
 * to the Serial output queue using Serial.print() */
void checkDigitalInputs(void)
{
  /* Using non-looping code allows constants to be given to readPort().
   * The compiler will apply substantial optimizations if the inputs
   * to readPort() are compile-time constants. */
  if (TOTAL_PORTS > 0 && reportPINs[0]) outputPort(0, readPort(0, portConfigInputs[0]), false);
  if (TOTAL_PORTS > 1 && reportPINs[1]) outputPort(1, readPort(1, portConfigInputs[1]), false);
  if (TOTAL_PORTS > 2 && reportPINs[2]) outputPort(2, readPort(2, portConfigInputs[2]), false);
  if (TOTAL_PORTS > 3 && reportPINs[3]) outputPort(3, readPort(3, portConfigInputs[3]), false);
  if (TOTAL_PORTS > 4 && reportPINs[4]) outputPort(4, readPort(4, portConfigInputs[4]), false);
  if (TOTAL_PORTS > 5 && reportPINs[5]) outputPort(5, readPort(5, portConfigInputs[5]), false);
  if (TOTAL_PORTS > 6 && reportPINs[6]) outputPort(6, readPort(6, portConfigInputs[6]), false);
  if (TOTAL_PORTS > 7 && reportPINs[7]) outputPort(7, readPort(7, portConfigInputs[7]), false);
  if (TOTAL_PORTS > 8 && reportPINs[8]) outputPort(8, readPort(8, portConfigInputs[8]), false);
  if (TOTAL_PORTS > 9 && reportPINs[9]) outputPort(9, readPort(9, portConfigInputs[9]), false);
  if (TOTAL_PORTS > 10 && reportPINs[10]) outputPort(10, readPort(10, portConfigInputs[10]), false);
  if (TOTAL_PORTS > 11 && reportPINs[11]) outputPort(11, readPort(11, portConfigInputs[11]), false);
  if (TOTAL_PORTS > 12 && reportPINs[12]) outputPort(12, readPort(12, portConfigInputs[12]), false);
  if (TOTAL_PORTS > 13 && reportPINs[13]) outputPort(13, readPort(13, portConfigInputs[13]), false);
  if (TOTAL_PORTS > 14 && reportPINs[14]) outputPort(14, readPort(14, portConfigInputs[14]), false);
  if (TOTAL_PORTS > 15 && reportPINs[15]) outputPort(15, readPort(15, portConfigInputs[15]), false);
}

