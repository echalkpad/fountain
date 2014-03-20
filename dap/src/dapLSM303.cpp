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
//#include <fcntl.h>
//#include <linux/i2c.h>
//#include <linux/i2c-dev.h>
//#include <sys/ioctl.h>
//#include <stropts.h>
//#include <math.h>

#include "Sensor.h"
#include "LSM303DLHC.h"

int main(int argc, char* argv[]) {
	printf("DAP - I2C Data Acquisition Program\n");
	printf("Test program for sensor class development.\n");

	LSM303DLHC *d = new LSM303DLHC(1, 0x19,"LSM303DLHC");

	printf("Device: %s at Bus %i, Address %#04x.\n", d->getDeviceName(),
			d->getBus(), d->getDeviceAddress());

	for (int idx = 0; idx < 50; idx++) {
		d->refreshSensorData();
		printf("\nAcc: X, Y, Z: %i, %i, %i.  Temp: %f\n",d->getAccelerationX(),d->getAccelerationY(),d->getAccelerationZ(),d->getTemperature());
		sleep(1);
	}

	return EXIT_SUCCESS;
}

