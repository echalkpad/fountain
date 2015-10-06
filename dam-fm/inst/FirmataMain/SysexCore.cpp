/*
    This module implements Firmata's processing of the "core" Sysex command codes.
*/

#include <Firmata.h>
#include "SysexCore.h"
#include "SysexI2C.h"

int samplingInterval = 19;          // how often to run the main loop (in ms)

sysexCallbackFunction hooks[127] = {0};

// Initialize our part of the Sysex command jump table

void setSysexCoreHooks(sysexCallbackFunction *h) {
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
