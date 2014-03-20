/*
 * Thermometer.h
 *
 * Define the methods implemented by any class that wants to
 * provide temperature data.
 * (This is a Java programmer imitating a Java interface.  May change!)
 *
 *  Created on: Mar 20, 2014
 *      Author: finson
 */

#include "Thermometer.h"

Thermometer::Thermometer(double t) {
	temperature = t;
}

Thermometer::Thermometer() {
	temperature = 0.0;
}

Thermometer::~Thermometer() {
}


double Thermometer::getTemperature() {
	return temperature;
}
