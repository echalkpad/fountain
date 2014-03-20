/*
 * Accelerometer.h
 *
 * Define the methods implemented by any class that wants to
 * provide 3-axis acceleration data.
 * (This is a Java programmer imitating a Java interface.  May change!)
 *
 *  Created on: Mar 20, 2014
 *      Author: finson
 */

#ifndef ACCELEROMETER_H_
#define ACCELEROMETER_H_

class Accelerometer {
protected:
	double accX;
	double accY;
	double accZ;

public:
	Accelerometer(double ax,double ay, double az);
	Accelerometer();
	virtual ~Accelerometer();

	virtual int getAccelerationX();
	virtual int getAccelerationY();
	virtual int getAccelerationZ();

};

#endif
