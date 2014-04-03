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
#include "LSM303DLHC.h"

LSM303DLHC::LSM303DLHC(int bus, uint8_t address, const char *name) : Sensor(bus,address,name)
{

	// Set up the control registers for our use

	uint8_t bfOutputDataRate = 0x4; // 50hz
	uint8_t bfLowPowerEnable = 0x0; // Normal power mode
	uint8_t bfAxisEnable = 0x7;		// Enable X, Y, and Z acceleration axes

	accFullScale = 2.0;

    registerValues[0] = ((bfOutputDataRate<<4) & ODR_MASK) |  // CTRL_REG1_A
    		((bfLowPowerEnable<<3) & LPEN_MASK) |
    		((bfAxisEnable<<0) & XYZENABLE_MASK);
    registerValues[1] = 0;  // CTRL_REG2_A
    registerValues[2] = 0;  // CTRL_REG3_A
    registerValues[3] = 0;  // CTRL_REG4_A
    registerValues[4] = 0;  // CTRL_REG5_A
    registerValues[5] = 0;  // CTRL_REG6_A
    registerValues[6] = 0;  // REFERENCE_A

	this->writeRegisters(FIRST_REGISTER,registerValues,7);
}

int LSM303DLHC::refreshSensorData() {

	int status = readRegisters(FIRST_REGISTER,registerValues,14);
	if (status < 0) {
		printf("Error: Acceleration read error %i.",status);
		return -1;
	}

    printf("\nReg: ");
    for (int j=0; j<14; j++) {
    	printf("%#04x ",registerValues[j]);
    }
    printf("\n");

    int16_t iX = (registerValues[OUT_X_H_A-FIRST_REGISTER]<<8) | (registerValues[OUT_X_L_A-FIRST_REGISTER]);
    int16_t iY = (registerValues[OUT_Y_H_A-FIRST_REGISTER]<<8) | (registerValues[OUT_Y_L_A-FIRST_REGISTER]);
    int16_t iZ = (registerValues[OUT_Z_H_A-FIRST_REGISTER]<<8) | (registerValues[OUT_Z_L_A-FIRST_REGISTER]);

    printf("Acc16: %i, %i, %i\n", iX,iY,iZ);

    accX = (iX*accFullScale)/32767;
    accY = (iY*accFullScale)/32767;
    accZ = (iZ*accFullScale)/32767;

    return 0;
}


