
#ifndef LSM303DLHC_H_
#define LSM303DLHC_H_

#include <stdint.h>
#include "Accelerometer.h"
#include "Thermometer.h"
#include "Sensor.h"

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
	BW_1200HZ 	= 7,
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


class LSM303DLHC : public Accelerometer, public Thermometer, public Sensor {
private:
	double accFullScale;
	uint8_t registerValues[I2C_REGISTER_COUNT];

public:
	LSM303DLHC(int bus, uint8_t address, const char *name);
	int refreshSensorData();

};


#endif
