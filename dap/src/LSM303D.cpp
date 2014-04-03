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

	int status = readRegisters(WHO_AM_I,registerValues,1);
	if (status < 0) {
		printf("Error: Failure reading device identification register.  %i\n",status);
		return;
	}
	if (registerValues[0] != WHO_AM_I_VALUE) {
		printf("Error: Unrecognized device identifier in LSM303D: Reg: %#04x, Val: %#04x", WHO_AM_I, registerValues[0]);
		return;
	}

	deviceTypeVerified = true;

	// Set up the control registers for our use

	uint8_t bfOutputDataRate = ADR_100;		// Set accelerometer data rate
	uint8_t bfBlockDataUpdate = 0;			// Allow split data reads (for now!)
	uint8_t bfAccEnable = 0x7;				// Enable X, Y, and Z acceleration axes

	uint8_t bfAccFullScale = AFS_2G;		// Set accelerometer full scale value
	accFullScale = 2.0;

	uint8_t bfTempEnable = 1;				// Enable temperature output
	uint8_t bfTempOnly = 1;					// Enable temperature while mag is off

    registerValues[0] = 0;									// CTRL0
    registerValues[1] = ((bfOutputDataRate<<4) & ADR_MASK) |
    		((bfBlockDataUpdate<<3) & BDU_MASK) |
    		((bfAccEnable<<0) & ACC_ENABLE_MASK);;  		// CTRL1
    registerValues[2] = ((bfAccFullScale<<3) & AFS_MASK);	// CTRL2
    registerValues[3] = 0;									// CTRL3
    registerValues[4] = 0;									// CTRL4
    registerValues[5] = ((bfTempEnable<<7) & TEMP_EN_MASK);	// CTRL5
    registerValues[6] = 0;									// CTRL6
    registerValues[7] = ((bfTempOnly<<4) & TEMP_ONLY_MASK);	// CTRL7

	status = this->writeRegisters(CTRL0,registerValues,8);
	if (status < 0) {
		printf("Error: Failure writing control register contents.  %i\n",status);
		return;
	}

	//  Verify that they were set as requested

	uint8_t rBuf[8];
	status = readRegisters(CTRL0,rBuf,8);
	if (status < 0) {
		printf("Error: Failure reading control register contents.  %i\n",status);
		return;
	}
	for (int idx=0; idx<8; idx++) {
		if (rBuf[idx]!=registerValues[idx]) {
			printf("Error: Control register value mismatch.\n");
			printf("Out:");
			for (int j=0; j<8; j++) {
				printf(" %#04x",registerValues[j]);
			}
			printf("\nIn: ");
			for (int k=0; k<8; k++) {
				printf(" %#04x",rBuf[k]);
			}
			printf("\n");
			break;
		}
	}
}

int LSM303D::refreshSensorData() {

	int status = readRegisters(TEMP_OUT_L,registerValues,2);
	if (status < 0) {
		printf("Error: Temperature read error %i.",status);
		return -1;
	}

	temperature = ((registerValues[TEMP_OUT_H-TEMP_OUT_L]<<8) | (registerValues[TEMP_OUT_L-TEMP_OUT_L]));

	status = readRegisters(OUT_X_L_A,registerValues,6);
	if (status < 0) {
		printf("Error: Acceleration read error %i.",status);
		return -2;
	}

#ifdef DAP_VERBOSE
    printf("\nReg: ");
    for (int j=0; j<6; j++) {
    	printf("%#04x ",registerValues[j]);
    }
    printf("\n");
#endif

    int16_t iX =  (registerValues[OUT_X_H_A-OUT_X_L_A]<<8) | (registerValues[OUT_X_L_A-OUT_X_L_A]);
    int16_t iY =  (registerValues[OUT_Y_H_A-OUT_X_L_A]<<8) | (registerValues[OUT_Y_L_A-OUT_X_L_A]);
    int16_t iZ =  (registerValues[OUT_Z_H_A-OUT_X_L_A]<<8) | (registerValues[OUT_Z_L_A-OUT_X_L_A]);

#ifdef DAP_VERBOSE
    printf("Acc16: %i, %i, %i\n", iX,iY,iZ);
#endif

    accX = (iX*accFullScale)/32767;
    accY = (iY*accFullScale)/32767;
    accZ = (iZ*accFullScale)/32767;

    return 0;
}


