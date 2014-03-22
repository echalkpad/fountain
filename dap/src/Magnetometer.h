/*
 * Magnetometer.h
 *
 * Define the methods implemented by any class that wants to
 * provide 3-axis magnetic field data.
 * (This is a Java programmer imitating a Java interface.  May change!)
 *
 *  Created on: Mar 20, 2014
 *      Author: finson
 */

#ifndef MAGNETOMETER_H_
#define MAGNETOMETER_H_

class Magnetometer {
protected:
	double magX;
	double magY;
	double magZ;

public:
	Magnetometer(double mx,double my, double mz);
	Magnetometer();
	virtual ~Magnetometer();

	virtual int getMagneticX();
	virtual int getMagneticY();
	virtual int getMagneticZ();

};

#endif
