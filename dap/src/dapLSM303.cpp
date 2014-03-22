/*
 ============================================================================
 Name        : dap.c
 Author      : Doug Johnson
 Version     :
 Copyright   : GPL
 Description : I2C Data Acquisition
 ============================================================================
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

#include "LSM303DLHC.h"
#include "LSM303D.h"

int main(int argc, char* argv[]) {
	printf("DAP - I2C Data Acquisition Program\n");
	printf("Test program for sensor class development.\n");

	LSM303DLHC *d1 = new LSM303DLHC(1, 0x19,"LSM303DLHC");
	LSM303D *d2 = new LSM303D(1,0x1D,"LSM303D");

	for (int idx = 0; idx < 50; idx++) {

		d1->refreshSensorData();
		d2->refreshSensorData();

		printf("%-10s Acc: X, Y, Z: %f, %f, %f.  Temp: %f\n",
				d1->getDeviceName(),d1->getAccelerationX(),d1->getAccelerationY(),d1->getAccelerationZ(),d1->getTemperature());
		printf("%-10s Acc: X, Y, Z: %f, %f, %f.  Temp: %f\n",
				d2->getDeviceName(),d2->getAccelerationX(),d2->getAccelerationY(),d2->getAccelerationZ(),d2->getTemperature());

		sleep(1);
	}

	return EXIT_SUCCESS;
}

