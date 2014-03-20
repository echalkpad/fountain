/*
 * Sensor.h
 *
 *	Interface to the base Sensor class.
 */

#ifndef Sensor_H_
#define Sensor_H_

#include <stdint.h>
#include "i2cmaster.h"

class Sensor {

private:
	int bus;
	uint8_t deviceAddress;
	int handle;

	char *deviceName;
	char *deviceLabel;

	uint8_t registerBuffer[I2C_REGISTER_COUNT+1];

public:
	Sensor(int bus, uint8_t address, char *name);
	~Sensor();

	int writeRegisters(uint8_t adr, uint8_t registerValues[], int count);
	int readRegisters(uint8_t adr, uint8_t registerValues[], int count);

	int getBus() const { return bus; }
	int getDeviceAddress() const{ return deviceAddress; }
	int getHandle() const { return handle; }

	char* getDeviceName() const { return deviceName; }
	char* getDeviceLabel() const { return deviceLabel; }



	int16_t convertAcceleration(char upper,char lower){
		return (upper<<8) | lower;
	}


};


#endif
