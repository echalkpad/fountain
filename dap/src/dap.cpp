/*
 ============================================================================
 Name        : dap.c
 Author      : Doug Johnson
 Version     :
 Copyright   : GPL
 Description : I2C Data Acquisition
 ============================================================================
 */

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>
#include <linux/i2c.h>
#include <linux/i2c-dev.h>
#include <sys/ioctl.h>
#include <stropts.h>
#include <math.h>
#include "Sensor.h"

#include "IOLSM303DLHC.h"


#define ODR_MASK 0xF0
#define LPEN_MASK 0x08
#define XYZENABLE_MASK 0x07



int main(int argc, char* argv[]) {
	printf("DAP - I2C Data Acquisition Program\n");

	// Set up the control registers for our use

	uint8_t bfOutputDataRate = 0x5; // 100hz
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

	Sensor *d = new Sensor(1, 0x19,"LSM303DLHC");
	int status = d->writeRegisters(0x20,buf,7);

	printf("Device: %s at Bus %i, Address %#04x.\n", d->getDeviceName(),
			d->getBus(), d->getDeviceAddress());

	for (int idx = 0; idx < 50; idx++) {
		int status = d->readRegisters(0x20,buf,14);
		if (status < 0) {
			return EXIT_FAILURE;
		}

	    int accelerationX = d->convertAcceleration(buf[OUT_X_H_A-FIRST_REGISTER],buf[OUT_X_H_A-FIRST_REGISTER]);
	    int accelerationY = d->convertAcceleration(buf[OUT_Y_H_A-FIRST_REGISTER],buf[OUT_Y_H_A-FIRST_REGISTER]);
	    int accelerationZ = d->convertAcceleration(buf[OUT_Z_H_A-FIRST_REGISTER],buf[OUT_Z_H_A-FIRST_REGISTER]);


		printf("Accelerations X, Y, Z: %i, %i, %i.\n", accelerationX,
				accelerationY, accelerationZ);
		sleep(1);
	}

	return EXIT_SUCCESS;
}

