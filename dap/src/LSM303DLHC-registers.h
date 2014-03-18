
#ifndef LSM303DLHC_H_
#define LSM303DLHC_H_

#define ODR_MASK 0xF0
#define LPEN_MASK 0x08
#define XYZENABLE_MASK 0x07

enum LSM303_RANGE {
	PLUSMINUS_1_G 		= 0,
	PLUSMINUS_1POINT5_G = 1,
	PLUSMINUS_2G 		= 2,
	PLUSMINUS_3G 		= 3,
	PLUSMINUS_4G 		= 4,
	PLUSMINUS_8G 		= 5,
	PLUSMINUS_16G 		= 6
};

enum LSM303_BANDWIDTH {
	BW_10HZ 	= 0,
	BW_20HZ 	= 1,
	BW_40HZ 	= 2,
	BW_75HZ 	= 3,
	BW_150HZ 	= 4,
	BW_300HZ 	= 5,
	BW_600HZ 	= 6,
	BW_1200HZ 	= 7,
	BW_HIGHPASS = 8,
	BW_BANDPASS = 9
};

enum LSM303_MODECONFIG {
	MODE_LOW_NOISE = 0,
	MODE_LOW_POWER = 3
};


#define FIRST_REGISTER    0x20
#define CTRL_REG1_A       0x20  // DLH, DLM, DLHC
#define CTRL_REG2_A       0x21  // DLH, DLM, DLHC
#define CTRL_REG3_A       0x22  // DLH, DLM, DLHC
#define CTRL_REG4_A       0x23  // DLH, DLM, DLHC
#define CTRL_REG5_A       0x24  // DLH, DLM, DLHC
#define CTRL_REG6_A       0x25  // DLHC
#define REFERENCE_A       0x26  // DLH, DLM, DLHC
#define STATUS_REG_A      0x27  // DLH, DLM, DLHC

#define OUT_X_L_A         0x28
#define OUT_X_H_A         0x29
#define OUT_Y_L_A         0x2A
#define OUT_Y_H_A         0x2B
#define OUT_Z_L_A         0x2C
#define OUT_Z_H_A         0x2D

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

#endif
