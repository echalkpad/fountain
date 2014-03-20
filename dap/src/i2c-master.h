/*
 * i2c-master.h
 *
 *	Define various I2C-related constants.
 *  Created on: Mar 16, 2014
 *      Author: finson
 */

#ifndef I2CMASTER_H_
#define I2CMASTER_H_

#define I2C_ADDRESS_MASK 0x7F
#define MAX_I2C_REGISTER_ADDRESS 0x7F
#define I2C_REGISTER_COUNT (MAX_I2C_REGISTER_ADDRESS+1)
#define I2C_AUTOINCREMENT_MASK 0x80

#define SENSOR_HEADER(ID) COMBO(ID)
#define COMBO(S)  S ## _HEADER

#define LSM303DLHC_HEADER "IO-LSM303DLHC.h"
#define LSM303D_HEADER "IO-LSM303D.h"

#endif /* I2CMASTER_H_ */
