/*
 * Sensor.h
 *
 *	Interface to the base Sensor class.
 */

#ifndef Sensor_H_
#define Sensor_H_

#include <stdint.h>
#include "i2c-master.h"

class Sensor {

private:
	int bus;
	uint8_t deviceAddress;
	int handle;

	const char *deviceName;
	const char *deviceLabel;

	uint8_t registerBuffer[I2C_REGISTER_COUNT+1];


protected:
	bool deviceTypeVerified;

public:
	Sensor(int bus, uint8_t address, const char *name);
	Sensor();
	virtual ~Sensor();

	int writeRegisters(uint8_t adr, uint8_t registerValues[], int count);
	int readRegisters(uint8_t adr, uint8_t registerValues[], int count);
	virtual int refreshSensorData()=0;

	int getBus() const { return bus; }
	int getDeviceAddress() const{ return deviceAddress; }
	int getHandle() const { return handle; }

	const char* getDeviceName() const { return deviceName; }
	const char* getDeviceLabel() const { return deviceLabel; }

};


#endif
