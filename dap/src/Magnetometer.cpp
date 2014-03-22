/*
 * Magnetometer.cpp
 *
 * Define the methods implemented by any class that wants to
 * provide 3-axis magnetic field data.
 * (This is a Java programmer imitating a Java interface.  May change!)
 *
 *  Created on: Mar 20, 2014
 *      Author: finson
 */

#include "Magnetometer.h"

Magnetometer::Magnetometer(double mx, double my, double mz) {
	magX = mx;
	magY = my;
	magZ = mz;
}

Magnetometer::Magnetometer() {
	magX = 0.0;
	magY = 0.0;
	magZ = 0.0;
}

Magnetometer::~Magnetometer() {
}


int Magnetometer::getMagneticX() {
	return magX;
}

int Magnetometer::getMagneticY() {
	return magY;
}

int Magnetometer::getMagneticZ() {
	return magZ;
}
