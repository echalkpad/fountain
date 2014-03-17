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


typedef struct device {
	char *name;
	int bus;
	int address;
	int handle;
} device;

device accel;

char *busname[] = {"/dev/i2c-0","/dev/i2c-1","/dev/i2c-2"};

int main(int argc, char* argv[]) {
	printf("DAP - I2C Data Acquisition Program\n");

	// print some info about the targeted device

	accel.name = "LSM303 Accelerometer";
	accel.bus = 1;
	accel.address = 0x19;

	printf("Device: %s at Bus %i, Address %#x.\n",accel.name,accel.bus,accel.address);

	int status = openDevice(accel);
	if (status != 0) {
		printf("Open error: %i\n", status);
		return EXIT_FAILURE;
	}
	printf("Device opened.  Handle: %i\n",accel.handle);

	// set control registers

	// 0x20 CTRL_REF1_A - power, rate

	return EXIT_SUCCESS;
}

int openDevice(device d) {
	int status = open(busname[d.bus], O_RDWR);

	if (status < 0) {
			printf("Failed to open %s on I2C Bus %s.  Error: %i\n", d.name,busname[d.bus],status);
			return(status);
	}

	d.handle = status;
	status = ioctl(d.handle, I2C_SLAVE, d.address);

	if (status < 0){
			printf("I2C_SLAVE address %#x failed.  Error: %i\n",d.address,status);
			return(status);
	}

	return 0;
}
