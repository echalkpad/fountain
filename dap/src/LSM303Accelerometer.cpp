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

#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <linux/i2c.h>
#include <linux/i2c-dev.h>
#include <sys/ioctl.h>
#include <stropts.h>
#include <stdio.h>
#include "LSM303Accelerometer.h"
//#include <iostream>
#include <math.h>
using namespace std;


#define ACC_X_LSB 	0x02
#define ACC_X_MSB 	0x03
#define ACC_Y_LSB 	0x04
#define ACC_Y_MSB 	0x05
#define ACC_Z_LSB 	0x06
#define ACC_Z_MSB 	0x07
#define TEMP	  	0x08  //Temperature
#define RANGE	  	0x35  //bits 3,2,1
#define BANDWIDTH 	0x20  //bits 7,6,5,4
#define MODE_CONFIG 0x30  //bits 1,0

LSM303Accelerometer::LSM303Accelerometer(int aBus, int anAddress, char *dName) {
	bus = aBus;
	deviceName = dName;
	deviceAddress = anAddress;
	snprintf(busName, MAX_BUS_NAME_SIZE, "/dev/i2c-%d", bus);
}
/*
 * Open the I2C adapter and point to the device of interest.
 * return status == 0 if okay, status < 0 if error
 */
int LSM303Accelerometer::openDevice(void) {
	int status = open(busName, O_RDWR);

	if (status < 0) {
			printf("Failed to open %s on I2C Bus %s.  Error: %i\n",deviceName,busName,status);
			return(status);
	}

	handle = status;
	status = ioctl(handle, I2C_SLAVE, deviceAddress);

	if (status < 0){
			printf("I2C_SLAVE address %#x failed.  Error: %i\n",deviceAddress,status);
			return(status);
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
    char buf[1] = { 0x20 | 0x80};
    if(write(handle, buf, 1) !=1){
    	printf("Failed to Reset Address in readFullSensorState()\n");
    	return 1;
    }

    int numberBytes = LSM303_I2C_BUFFER_SIZE;
	numberBytes = 16;
    int bytesRead = read(handle, this->dataBuffer, numberBytes);
    if (bytesRead == -1){
    	printf("Failure to read Byte Stream in readFullSensorState()\n");
    	return 1;
    }
    printf("Number of bytes read was %i.\n",bytesRead);

   for (int i=0; i<numberBytes; i++){
           printf("Byte %#04x is %#04x\n", i, dataBuffer[i]);
    }

 //  if (this->dataBuffer[0]!=0x03){
 //  	printf("MAJOR FAILURE: DATA WITH LSM303 HAS LOST SYNC!\n");
 //  	return 1;
 //  }

/*
    this->accelerationX = convertAcceleration(ACC_X_MSB, ACC_X_LSB);
    this->accelerationY = convertAcceleration(ACC_Y_MSB, ACC_Y_LSB);
    this->accelerationZ = convertAcceleration(ACC_Z_MSB, ACC_Z_LSB);
    this->calculatePitchAndRoll();
*/
    //cout << "Pitch:" << this->getPitch() << "   Roll:" << this->getRoll() <<  endl;
    return 0;
}
/*
 * Close the I2C adapter.
 */
int LSM303Accelerometer::closeDevice(void) {
	return close(handle);
}
