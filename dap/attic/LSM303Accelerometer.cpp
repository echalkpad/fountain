/*
 * LSM303Accelerometer.cpp
 *
 * Implementation of a class to interface with the LSM303 3 Axis Accelerometer
 * over the I2C bus
 *
 *  Created on: Mar 16, 2014
 *  Author: Doug Johnson
 *  Originally cloned from BMA180Accelerometer.cpp by Derek Molloy.
 *  School of Electronic Engineering, Dublin City University
 *  www.eeng.dcu.ie/~molloyd/
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL I
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <fcntl.h>
//#include <linux/i2c.h>
#include <linux/i2c-dev.h>
//#include <math.h>
#include <stdio.h>
//#include <stdlib.h>
//#include <stropts.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <unistd.h>
#include "i2c-master.h"

#include "IO-LSM303D.h"
#include "LSM303AccMagSensor.h"

using namespace std;

#define XSTRINGIFY(s) STRINGIFY(s)
#define STRINGIFY(s) #s

LSM303Accelerometer::LSM303Accelerometer(int aBus, int anAddress, char *dName) {
	bus = aBus;
	deviceName = dName;
	sensorID = XSTRINGIFY(SENSOR_ID);
	deviceAddress = anAddress;
	snprintf(busName, MAX_BUS_NAME_SIZE, "/dev/i2c-%d", bus);
	handle = -1;

	bfOutputDataRate = 0x5; // 100hz
	bfLowPowerEnable = 0x0; // Normal power mode
	bfAxisEnable = 0x7;		// Enable X, Y, and Z acceleration axes

	accelerationX = 0;
	accelerationY = 0;
	accelerationZ = 0;

	pitch = 0.0;
	roll = 0.0;
	temperature = 0.0;

}
/*
 * Open the I2C adapter and point to the device of interest.
 * return status == 0 if okay, status < 0 if error
 */
int LSM303Accelerometer::openDevice(void) {

	// Connect to the I2C bus adapter

	int status = open(busName, O_RDWR);
	if (status < 0) {
			printf("Failed to open %s on I2C Bus %s.  Error: %i\n",deviceName,busName,status);
			return(status);
	}
	handle = status;

	// Tell the adapter which slave device we are using

	status = ioctl(handle, I2C_SLAVE, deviceAddress);
	if (status < 0){
			printf("I2C_SLAVE address %#x failed.  Error: %i\n",deviceAddress,status);
			return(status);
	}

	// Set up the control registers for our use

    char buf[8];
    buf[0] = 0x80 | CTRL_REG1_A;  // autoincrement, starting with register 0x20
    buf[1] = ((bfOutputDataRate<<4) & ODR_MASK) |  // CTRL_REG1_A
    		((bfLowPowerEnable<<3) & LPEN_MASK) |
    		((bfAxisEnable<<0) & XYZENABLE_MASK);
    buf[2] = 0;  // CTRL_REG2_A
    buf[3] = 0;  // CTRL_REG3_A
    buf[4] = 0;  // CTRL_REG4_A
    buf[5] = 0;  // CTRL_REG5_A
    buf[6] = 0;  // CTRL_REG6_A
    buf[7] = 0;  // REFERENCE_A


    if(write(handle, buf, 8) !=8){
    	printf("Failed to set all control registers.\n");
    	return 1;
    }

	return 0;
}
/*
 * Read the sensor data registers.  Note that openDevice() must have been called
 * successfully before using this method.
 */

int LSM303Accelerometer::readFullSensorState(){

    // According to the LSM303 datasheet on page 59, you need to send the first address
    // in write mode and then a stop/start condition is issued. Data bytes are
    // transferred with automatic address increment.
    char startAddress = 0x80 | FIRST_REGISTER;  // autoincrement, starting with first control register
    if(write(handle, &startAddress, 1) !=1){
    	printf("Failed to Reset Address in readFullSensorState()\n");
    	return 1;
    }

	char statusBuffer[MAX_REGISTER_ADDRESS+1];

    int numberBytes = 14;
    int bytesRead = read(handle, statusBuffer, numberBytes);
    if (bytesRead != numberBytes){
    	printf("Failure to read Byte Stream in readFullSensorState()\n");
    	return 1;
    }
//    printf("Number of bytes read was %i.\n",bytesRead);

//   for (int i=0; i<numberBytes; i++){
//           printf("Byte %#04x is %#04x\n", i, statusBuffer[i]);
//    }

   if (statusBuffer[0] != 0x57){
   	printf("MAJOR FAILURE: DATA WITH LSM303 HAS LOST SYNC!\n");
   	return 1;
   }

    this->accelerationX = convertAcceleration(statusBuffer[OUT_X_H_A-FIRST_REGISTER],statusBuffer[OUT_X_H_A-FIRST_REGISTER]);
    this->accelerationY = convertAcceleration(statusBuffer[OUT_Y_H_A-FIRST_REGISTER],statusBuffer[OUT_Y_H_A-FIRST_REGISTER]);
    this->accelerationZ = convertAcceleration(statusBuffer[OUT_Z_H_A-FIRST_REGISTER],statusBuffer[OUT_Z_H_A-FIRST_REGISTER]);
    /*
    this->calculatePitchAndRoll();
*/
    return 0;
}
/*
 * Close the I2C adapter.
 */
int LSM303Accelerometer::closeDevice(void) {
	return close(handle);
}


int16_t LSM303Accelerometer::convertAcceleration(char upper,char lower){
	return (upper<<8) | lower;
}

