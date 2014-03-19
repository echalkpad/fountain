/*
 * i2c-master.h
 *
 *	Define various I2C-related constants.
 *  Created on: Mar 16, 2014
 *      Author: finson
 */

#ifndef I2C-MASTER_H_
#define I2C-MASTER_H_

#define MAX_BUS_NAME_SIZE 64
#define MAX_REGISTER_ADDRESS 0x7F
#define REGISTER_COUNT (MAX_REGISTER_ADDRESS+1)

#define SENSOR_HEADER(ID) COMBO(ID)
#define COMBO(S)  S ## _HEADER

#define LSM303DLHC_HEADER "IO-LSM303DLHC.h"
#define LSM303D_HEADER "IO-LSM303D.h"

#endif /* I2C-MASTER_H_ */
