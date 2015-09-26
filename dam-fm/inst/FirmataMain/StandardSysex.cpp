/*
  This module implements a way of extending Firmata's processing of Sysex command codes.
*/

#include "StandardSysex.h"

// Define the function that initializes the jump table

void setHooks(sysexCallbackFunction *h) {
	h[SAMPLING_INTERVAL] = processSamplingInterval;
	h[I2C_CONFIG] = processI2CConfig;
	h[I2C_REQUEST] = processI2CRequest;
	h[SERVO_CONFIG] = processServoConfig;
	h[EXTENDED_ANALOG] = processExtendedAnalog;
	h[PIN_STATE_QUERY] = processPinStateQuery;
	h[CAPABILITY_QUERY] = processCapabilityQuery;
	h[ANALOG_MAPPING_QUERY] = processAnalogMappingQuery;
}

// Define the functions that implement the "standard" Sysex capabilities
//
//

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

void processI2CConfig(byte command, byte argc, byte *argv) {
	delayTime = (argv[0] + (argv[1] << 7));

	if (delayTime > 0) {
		i2cReadDelayTime = delayTime;
	}

	if (!isI2CEnabled) {
		enableI2CPins();
	}
}

void processI2CRequest(byte command, byte argc, byte *argv) {
	byte mode;
    byte slaveAddress;
    byte slaveRegister;
    byte data;
    unsigned int delayTime;

	mode = argv[1] & I2C_READ_WRITE_MODE_MASK;
	if (argv[1] & I2C_10BIT_ADDRESS_MODE_MASK) {
		Firmata.sendString("10-bit addressing mode is not yet supported");
		return;
	}
	else {
		slaveAddress = argv[0];
	}

	switch (mode) {
	case I2C_WRITE:
		Wire.beginTransmission(slaveAddress);
		for (byte i = 2; i < argc; i += 2) {
			data = argv[i] + (argv[i + 1] << 7);
#if ARDUINO >= 100
			Wire.write(data);
#else
			Wire.send(data);
#endif
		}
		Wire.endTransmission();
		delayMicroseconds(70);
		break;
	case I2C_READ:
		if (argc == 6) {
			// a slave register is specified
			slaveRegister = argv[2] + (argv[3] << 7);
			data = argv[4] + (argv[5] << 7);  // bytes to read
			readAndReportData(slaveAddress, (int)slaveRegister, data);
		}
		else {
			// a slave register is NOT specified
			data = argv[2] + (argv[3] << 7);  // bytes to read
			readAndReportData(slaveAddress, (int)REGISTER_NOT_SPECIFIED, data);
		}
		break;
	case I2C_READ_CONTINUOUSLY:
		if ((queryIndex + 1) >= MAX_QUERIES) {
			// too many queries, just ignore
			Firmata.sendString("too many queries");
			break;
		}
		queryIndex++;
		query[queryIndex].addr = slaveAddress;
		query[queryIndex].reg = argv[2] + (argv[3] << 7);
		query[queryIndex].bytes = argv[4] + (argv[5] << 7);
		break;
	case I2C_STOP_READING:
		byte queryIndexToSkip;
		// if read continuous mode is enabled for only 1 i2c device, disable
		// read continuous reporting for that device
		if (queryIndex <= 0) {
			queryIndex = -1;
		} else {
			// if read continuous mode is enabled for multiple devices,
			// determine which device to stop reading and remove it's data from
			// the array, shifiting other array data to fill the space
			for (byte i = 0; i < queryIndex + 1; i++) {
				if (query[i].addr = slaveAddress) {
					queryIndexToSkip = i;
					break;
				}
			}

			for (byte i = queryIndexToSkip; i < queryIndex + 1; i++) {
				if (i < MAX_QUERIES) {
					query[i].addr = query[i + 1].addr;
					query[i].reg = query[i + 1].addr;
					query[i].bytes = query[i + 1].bytes;
				}
			}
			queryIndex--;
		}
		break;
	default:
		break;
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
case PIN_STATE_QUERY:
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

