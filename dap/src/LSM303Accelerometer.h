/*
 * LSM303Accelerometer.h
 *
 *  Created on: Mar 16, 2014
 *      Author: finson
 */

#ifndef LSM303ACCELEROMETER_H_
#define LSM303ACCELEROMETER_H_

#define LSM303_I2C_BUFFER_SIZE 0x80
#define MAX_BUS_NAME_SIZE 64

#define MAX_REGISTER_ADDRESS 0x7F

#define ODR_MASK 0xF0
#define LPEN_MASK 0x08
#define XYZENABLE_MASK 0x07

enum LSM303_RANGE {
	PLUSMINUS_1_G 		= 0,
	PLUSMINUS_1POINT5_G = 1,
	PLUSMINUS_2G 		= 2,
	PLUSMINUS_3G 		= 3,
	PLUSMINUS_4G 		= 4,
	PLUSMINUS_8G 		= 5,
	PLUSMINUS_16G 		= 6
};

enum LSM303_BANDWIDTH {
	BW_10HZ 	= 0,
	BW_20HZ 	= 1,
	BW_40HZ 	= 2,
	BW_75HZ 	= 3,
	BW_150HZ 	= 4,
	BW_300HZ 	= 5,
	BW_600HZ 	= 6,
	BW_12OOHZ 	= 7,
	BW_HIGHPASS = 8,
	BW_BANDPASS = 9
};

enum LSM303_MODECONFIG {
	MODE_LOW_NOISE = 0,
	MODE_LOW_POWER = 3
};


#define FIRST_REGISTER    0x20
#define CTRL_REG1_A       0x20  // DLH, DLM, DLHC
#define CTRL_REG2_A       0x21  // DLH, DLM, DLHC
#define CTRL_REG3_A       0x22  // DLH, DLM, DLHC
#define CTRL_REG4_A       0x23  // DLH, DLM, DLHC
#define CTRL_REG5_A       0x24  // DLH, DLM, DLHC
#define CTRL_REG6_A       0x25  // DLHC
#define REFERENCE_A       0x26  // DLH, DLM, DLHC
#define STATUS_REG_A      0x27  // DLH, DLM, DLHC

#define OUT_X_L_A         0x28
#define OUT_X_H_A         0x29
#define OUT_Y_L_A         0x2A
#define OUT_Y_H_A         0x2B
#define OUT_Z_L_A         0x2C
#define OUT_Z_H_A         0x2D



class LSM303Accelerometer {

private:
	int bus;
	char busName[MAX_BUS_NAME_SIZE];
	int handle;

	int deviceAddress;
	char *deviceName;

	char bfOutputDataRate;
	char bfLowPowerEnable;
	char bfAxisEnable;

	int accelerationX;
	int accelerationY;
	int accelerationZ;

	double pitch;  //in degrees
	double roll;   //in degrees

	float temperature; //accurate to 0.5C
	LSM303_RANGE range;
	LSM303_BANDWIDTH bandwidth;
	LSM303_MODECONFIG modeConfig;

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


#endif /* LSM303ACCELEROMETER_H_ */
