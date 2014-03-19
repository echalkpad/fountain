/*
 * LSM303Accelerometer.h
 *
 *	Select the appropriate register set.
 *
 *  Created on: Mar 16, 2014
 *      Author: finson
 */

#ifndef LSM303ACCELEROMETER_H_
#define LSM303ACCELEROMETER_H_

#include "i2c-master.h"

#define SENSOR_ID LSM303D
#include SENSOR_HEADER(SENSOR_ID)

class LSM303Accelerometer {

private:
	int bus;
	char busName[MAX_BUS_NAME_SIZE];
	int handle;

	int deviceAddress;
	char *deviceName;
	char *sensorID;

	char bfOutputDataRate;
	char bfLowPowerEnable;
	char bfAxisEnable;

	int accelerationX;
	int accelerationY;
	int accelerationZ;

	double pitch;  //in degrees
	double roll;   //in degrees

	float temperature; //accurate to 0.5C

	char registerBuffer[REGISTER_COUNT];

	LSM303_RANGE range;
	LSM303_BANDWIDTH bandwidth;
	LSM303_MODECONFIG modeConfig;

	void initializeRegisters();

public:
	LSM303Accelerometer(int bus, int address, char *name);

	int openDevice(void);
	int closeDevice(void);

	int readFullSensorState(void);
	int16_t convertAcceleration(char upper,char lower);

	int getBus() const
	{
		return bus;
	}

	const char* getBusName() const
	{
		return busName;
	}

	int getDeviceAddress() const
	{
		return deviceAddress;
	}

	char* getDeviceName() const
	{
		return deviceName;
	}

	int getHandle() const
	{
		return handle;
	}

	int getAccelerationX() const
	{
		return accelerationX;
	}

	int getAccelerationY() const
	{
		return accelerationY;
	}

	int getAccelerationZ() const
	{
		return accelerationZ;
	}
};


#endif
