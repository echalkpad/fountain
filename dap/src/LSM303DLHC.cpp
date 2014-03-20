/*
 * LSM303DLHC.cpp
 *
 * Implement interfaces to the STMicroelectronics LSM303D accelerometer and
 * magnetometer module.
 *
 *  Created on: Mar 20, 2014
 *      Author: finson
 */

#include <stdio.h>
#include "i2c-master.h"
#include "LSM303DLHC.h"

LSM303DLHC::LSM303DLHC(int bus, uint8_t address, const char *name) : Sensor(bus,address,name)
{

	// Set up the control registers for our use

	uint8_t bfOutputDataRate = 0x4; // 50hz
	uint8_t bfLowPowerEnable = 0x0; // Normal power mode
	uint8_t bfAxisEnable = 0x7;		// Enable X, Y, and Z acceleration axes

    uint8_t buf[I2C_REGISTER_COUNT];
    buf[0] = ((bfOutputDataRate<<4) & ODR_MASK) |  // CTRL_REG1_A
    		((bfLowPowerEnable<<3) & LPEN_MASK) |
    		((bfAxisEnable<<0) & XYZENABLE_MASK);
    buf[1] = 0;  // CTRL_REG2_A
    buf[2] = 0;  // CTRL_REG3_A
    buf[3] = 0;  // CTRL_REG4_A
    buf[4] = 0;  // CTRL_REG5_A
    buf[5] = 0;  // CTRL_REG6_A
    buf[6] = 0;  // REFERENCE_A

	int status = this->writeRegisters(FIRST_REGISTER,buf,7);

	printf("Device: %s at Bus %i, Address %#04x.\n", getDeviceName(),
			getBus(), getDeviceAddress());

}

int LSM303DLHC::refreshSensorData() {

	int status = readRegisters(FIRST_REGISTER,registerBuffer,14);
	if (status < 0) {
		return -1;
	}

    accX = (registerBuffer[OUT_X_H_A-FIRST_REGISTER]<<8) | registerBuffer[OUT_X_L_A-FIRST_REGISTER];
    accY = (registerBuffer[OUT_Y_H_A-FIRST_REGISTER]<<8) | registerBuffer[OUT_Y_L_A-FIRST_REGISTER];
    accZ = (registerBuffer[OUT_Z_H_A-FIRST_REGISTER]<<8) | registerBuffer[OUT_Z_L_A-FIRST_REGISTER];

    return 0;
}


