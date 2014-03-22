/*
 * LSM303D.cpp
 *
 * Implement interfaces to the STMicroelectronics LSM303D accelerometer and
 * magnetometer module.
 *
 *  Created on: Mar 20, 2014
 *      Author: finson
 */

#include <stdio.h>
#include "LSM303D.h"

LSM303D::LSM303D(int bus, uint8_t address, const char *name) : Sensor(bus,address,name)
{
	// Verify that the device is the one we think it is.

	int status = readRegisters(WHO_AM_I,registerBuffer,1);
	if (status < 0) {
		return;
	}
	if (registerBuffer[0] != WHO_AM_I_VALUE) {
		printf("Error: Unrecognized device identifier in LSM303D: Reg: %#04x, Val: %#04x", WHO_AM_I, registerBuffer[0]);
		return;
	}

	deviceTypeVerified = true;

	// Set up the control registers for our use

	uint8_t bfOutputDataRate = ADR_100;		// Set accelerometer data rate
	uint8_t bfBlockDataUpdate = 1;			// Prevent split data reads
	uint8_t bfAccEnable = 0x7;				// Enable X, Y, and Z acceleration axes

	uint8_t bfAccFullScale = AFS_2G;		// Set accelerometer full scale value
	accFullScale = 2.0;

	uint8_t bfTempEnable = 1;				// Enable temperature output
	uint8_t bfTempOnly = 1;					// Enable temperature while mag is off

    registerBuffer[0] = 0;									// CTRL0
    registerBuffer[1] = ((bfOutputDataRate<<4) & ADR_MASK) |
    		((bfBlockDataUpdate<<3) & BDU_MASK) |
    		((bfAccEnable<<0) & ACC_ENABLE_MASK);;  		// CTRL1
    registerBuffer[2] = ((bfAccFullScale<<3) & AFS_MASK);	// CTRL2
    registerBuffer[3] = 0;									// CTRL3
    registerBuffer[4] = 0;									// CTRL4
    registerBuffer[5] = ((bfTempEnable<<7) & TEMP_EN_MASK);	// CTRL5
    registerBuffer[6] = 0;									// CTRL6
    registerBuffer[7] = ((bfTempOnly<<4) & TEMP_ONLY_MASK);	// CTRL7

	status = this->writeRegisters(CTRL0,registerBuffer,8);

	printf("Device: %s at Bus %i, Address %#04x.\n", getDeviceName(),
			getBus(), getDeviceAddress());

}

int LSM303D::refreshSensorData() {

	int status = readRegisters(TEMP_OUT_L,registerBuffer,2);
	if (status < 0) {
		printf("Error: Temperature read error %i.",status);
		return -1;
	}

	temperature = ((registerBuffer[TEMP_OUT_H-TEMP_OUT_L]<<8) | (registerBuffer[TEMP_OUT_L-TEMP_OUT_L]));

	status = readRegisters(OUT_X_L_A,registerBuffer,6);
	if (status < 0) {
		printf("Error: Acceleration read error %i.",status);
		return -2;
	}

    printf("\nReg: ");
    for (int j=0; j<6; j++) {
    	printf("%#04x ",registerBuffer[j]);
    }
    printf("\n");

    int16_t iX =  (registerBuffer[OUT_X_H_A-OUT_X_L_A]<<8) | (registerBuffer[OUT_X_L_A-OUT_X_L_A]);
    int16_t iY =  (registerBuffer[OUT_Y_H_A-OUT_X_L_A]<<8) | (registerBuffer[OUT_Y_L_A-OUT_X_L_A]);
    int16_t iZ =  (registerBuffer[OUT_Z_H_A-OUT_X_L_A]<<8) | (registerBuffer[OUT_Z_L_A-OUT_X_L_A]);

    printf("Acc16: %i, %i, %i\n", iX,iY,iZ);

    accX = (iX*accFullScale)/32767; // should be INT16_MAX, rather than 32767
    accY = (iY*accFullScale)/32767;
    accZ = (iZ*accFullScale)/32767;

    return 0;
}


