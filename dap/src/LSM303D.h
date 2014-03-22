
#ifndef LSM303D_H_
#define LSM303D_H_

#include <stdint.h>
#include "Accelerometer.h"
#include "Magnetometer.h"
#include "Thermometer.h"
#include "Sensor.h"

// LSM303D masks and constants

#define WHO_AM_I_VALUE	0x49

#define BDU_MASK		0x08	// Block Data Update
#define ACC_ENABLE_MASK	0x07	// XYZ Acceleration Enable

#define ADR_MASK		0xF0	// Acceleration Output Data Rate
#define ADR_OFF			0x00	// Power-down mode
#define ADR_3			0x01	// 3.125 Hz
#define ADR_6			0x02	// 6.25 Hz
#define ADR_12			0x03	// 12.5 Hz
#define ADR_25			0x04	// 25 Hz
#define ADR_50			0x05	// 50 Hz
#define ADR_100			0x06	// 100 Hz
#define ADR_200			0x07	// 200 Hz
#define ADR_400			0x08	// 400 Hz
#define ADR_800			0x09	// 800 Hz
#define ADR_1600		0x0A	// 1600 Hz

#define AFS_MASK		0x34	// Acceleration Full Scale
#define AFS_2G			0x00
#define AFS_4G			0x01
#define AFS_6G			0x02
#define AFS_8G			0x03
#define AFS_16G			0x04

#define TEMP_EN_MASK	0x80	// Temperature Enable
#define TEMP_ONLY_MASK	0x10	// Temp is on, Mag is off

#define MDR_MASK		0x1C	// Magnetic Output Data Rate
#define MDR_3			0x01	// 3.125 Hz
#define MDR_6			0x02	// 6.25 Hz
#define MDR_12			0x03	// 12.5 Hz
#define MDR_25			0x04	// 25 Hz
#define MDR_50			0x05	// 50 Hz
#define MDR_100			0x06	// 100 Hz

#define MFS_MASK		0x60	// Magnetic Full Scale
#define MFS_2			0x00
#define MFS_4			0x01
#define MFS_8			0x02
#define MFS_16			0x03

#define MD_MASK			0x03	// Magnetic Sensor Model Selection
#define MD_CONTINUOUS	0x00	// Continuous conversion
#define MD_SINGLE		0x01	// Single conversion
#define MD_OFF2			0x02	// Power off
#define MD_OFF3			0x03	// Power off

// LSM303D register addresses

//       reserved		0x00
//       reserved		0x01
//       reserved		0x02
//       reserved		0x03
//       reserved		0x04
#define TEMP_OUT_L		0x05
#define TEMP_OUT_H		0x06
#define STATUS_M		0x07
#define OUT_X_L_M		0x08
#define OUT_X_H_M		0x09
#define OUT_Y_L_M		0x0A
#define OUT_Y_H_M		0x0B
#define OUT_Z_L_M		0x0C
#define OUT_Z_H_M		0x0D
//       reserved		0x0E
#define WHO_AM_I		0x0F
//       reserved		0x10
//       reserved		0x11
#define INT_CTRL_M		0x12
#define INT_SRC_M		0x13
#define INT_THS_L_M		0x14
#define INT_THS_H_M		0x15
#define OFFSET_X_L_M	0x16
#define OFFSET_X_H_M	0x17
#define OFFSET_Y_L_M	0x18
#define OFFSET_Y_H_M	0x19
#define OFFSET_Z_L_M	0x1A
#define OFFSET_Z_H_M	0x1B
#define REFERENCE_X		0x1C
#define REFERENCE_Y		0x1D
#define REFERENCE_Z		0x1E
#define CTRL0			0x1F
#define CTRL1			0x20
#define CTRL2			0x21
#define CTRL3			0x22
#define CTRL4			0x23
#define CTRL5			0x24
#define CTRL6			0x25
#define CTRL7			0x26
#define STATUS_A		0x27
#define OUT_X_L_A		0x28
#define OUT_X_H_A		0x29
#define OUT_Y_L_A		0x2A
#define OUT_Y_H_A		0x2B
#define OUT_Z_L_A		0x2C
#define OUT_Z_H_A		0x2D
#define FIFO_CTRL		0x2E
#define FIFO_SRC		0x2F
#define IG_CFG1			0x30
#define IG_SRC1			0x31
#define IG_THS1			0x32
#define IG_DUR1			0x33
#define IG_CFG2			0x34
#define IG_SRC2			0x35
#define IG_THS2			0x36
#define IG_DUR2			0x37
#define CLICK_CFG		0x38
#define CLICK_SRC		0x39
#define CLICK_THS		0x3A
#define TIME_LIMIT		0x3B
#define TIME _LATENCY	0x3C
#define TIME_WINDOW		0x3D
#define Act_THS			0x3E
#define Act_DUR			0x3F

class LSM303D : public Accelerometer, public Magnetometer, public Thermometer, public Sensor {

private:
	double accFullScale;

public:
	LSM303D(int bus, uint8_t address, const char *name);

	int refreshSensorData();
	
};


#endif
