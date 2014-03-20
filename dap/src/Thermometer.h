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

#ifndef THERMOMETER_H_
#define THERMOMETER_H_

class Thermometer {
protected:
	double temperature;

public:
	Thermometer(double t);
	Thermometer();
	virtual ~Thermometer();

	double getTemperature();

};

#endif
