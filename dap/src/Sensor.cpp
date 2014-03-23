/*
 * Sensor.cpp
 *
 * Implementation of the base Sensor class.
 *
 */

//#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ioctl.h>
//#include <stropts.h>
#include <stdio.h>
//#include <math.h>
//using namespace std;

#include "Sensor.h"

Sensor::Sensor() {
		bus = 0;
		deviceAddress = 0;
		deviceName = "<default>";
		deviceLabel = "<default>";
		handle = 0;
		deviceTypeVerified = false;
	}

Sensor::Sensor(int aBus, uint8_t anAddress, const char *aName) {
	bus = aBus;
	deviceAddress = anAddress;
	deviceName = aName;
	deviceLabel = aName;

	// Connect to the I2C bus adapter

	char busName[16];
	snprintf(busName, 16, "/dev/i2c-%d", bus);
	int status = open(busName, O_RDWR);
	if (status < 0) {
		printf("Failed to open %s on I2C Bus %s.  Error: %i\n", deviceName,
				busName, status);
	}
	handle = status;
	deviceTypeVerified = false;
}
/*
 * Release the adapter handle.
 */
Sensor::~Sensor(void) {
	close(handle);
}

/*
 * Set the I2C adapter (master) so that it points to our device (slave),
 * then write a register address followed by one or more supplied values to the
 * device.
 *
 * return status == 0 if okay, status < 0 if error
 */
int Sensor::writeRegisters(uint8_t adr, uint8_t rv[], int count) {

	if (count < 1) {
		return -1;
	}

	// Tell the adapter which slave device to use

	int status = ioctl(handle, I2C_SLAVE, deviceAddress);
	if (status < 0) {
		printf("Failed to set I2C_SLAVE address %#x.  Error: %i\n",
				deviceAddress, status);
		return -2;
	}

	// Write the provided values

	registerBuffer[0] =
			(adr & I2C_ADDRESS_MASK) | ((count > 1) ? I2C_AUTOINCREMENT_MASK : 0);
	for (int idx = 0; idx < count; idx++) {
		registerBuffer[idx + 1] = rv[idx];
	}

	printf("[write] Starting register address: %#04x (as written %#04x).  Byte count: %i\n",adr,registerBuffer[0],count);

	status = write(handle, registerBuffer, count + 1);
	if (status != count + 1) {
		printf("Failed to set all control registers.\n");
		return -3;
	}
	return 0;
}

/*
 * Set the I2C adapter (master) so that it points to our device (slave),
 * then write a register address, then read one or more requested values from the
 * device.
 *
 * return status == 0 if okay, status < 0 if error
 */

int Sensor::readRegisters(uint8_t adr, uint8_t rv[], int count) {

	if (count < 1) {
		return -1;
	}

// Tell the adapter which slave device to use

	int status = ioctl(handle, I2C_SLAVE, deviceAddress);
	if (status < 0) {
		printf("Failed to set I2C_SLAVE address %#04x.  Error: %i\n",
				deviceAddress, status);
		return -2;
	}

// Write the address of the first register to read.

	uint8_t startAddress =
			(adr & I2C_ADDRESS_MASK) | ((count > 1) ? I2C_AUTOINCREMENT_MASK : 0);

	status = write(handle, &startAddress, 1);
	if (status != 1) {
		printf(
				"Failed to set I2C read start register address %#04x.  Status: %i\n",
				startAddress, status);
		return -3;
	}

//  Read as requested

	status = read(handle, rv, count);
	if (status != count) {
		printf(
				"Failed to read I2C device registers (%i starting at %#04x).  Status: %i\n",
				count, adr, status);
		return -4;
	}

	return 0;
}
