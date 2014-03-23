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
	
	char filename[] = "test.csv";
	printf("Will write samples to %s\n\n",filename);
	
	FILE *datafile;
	datafile=fopen(filename, "w");
	fprintf(datafile, "device,sample,X,Y,Z\n");

	LSM303DLHC *d1 = new LSM303DLHC(1, 0x19,"LSM303DLHC");
	printf("Device initialization complete: %s at Bus %i, Address %#04x. Handle: %i\n", d1->getDeviceName(),
			d1->getBus(), d1->getDeviceAddress(),d1->getHandle());

//	LSM303D *d2 = new LSM303D(1,0x1D,"LSM303D");
//	printf("Device initialization complete: %s at Bus %i, Address %#04x. Handle: %i\n", d2->getDeviceName(),
//		d2->getBus(), d2->getDeviceAddress(),d2->getHandle());

	for (int idx = 0; idx < 50; idx++) {

		d1->refreshSensorData();
//		d2->refreshSensorData();

		printf("%-10s Acc: X, Y, Z: %f, %f, %f.  Temp: %f\n",
				d1->getDeviceName(),d1->getAccelerationX(),d1->getAccelerationY(),d1->getAccelerationZ(),d1->getTemperature());
		fprintf(datafile,"%i,%s,%f,%f,%f\n",
				d1->getDeviceName(),idx,d1->getAccelerationX(),d1->getAccelerationY(),d1->getAccelerationZ());

//		printf("%-10s Acc: X, Y, Z: %f, %f, %f.  Temp: %f\n",
//				d2->getDeviceName(),d2->getAccelerationX(),d2->getAccelerationY(),d2->getAccelerationZ(),d2->getTemperature());
//		fprintf(datafile,"%i,%s,%f,%f,%f\n",
//				d2->getDeviceName(),idx,d2->getAccelerationX(),d2->getAccelerationY(),d2->getAccelerationZ());

		sleep(1);
	}

	return EXIT_SUCCESS;
}

