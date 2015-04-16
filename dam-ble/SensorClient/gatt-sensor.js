/*
 * gatt-sensor.js
 * This module is an interface layer that mimics an api that might be used to
 * talk over Bluetooth BLE to a device implementing specific sensor
 * characteristics defined by the Bluetooth SIG in
 * org.bluetooth.service.environmental_sensing.  This
 * module is intended for use with Cylon.js, and is based on the existing
 * module analog-sensor.js.
 *
 * Doug Johnson, 2015
 *
 *------------------------------------------
 *
 * Analog Sensor driver
 * http://cylonjs.com
 *
 * Copyright (c) 2013-2014 The Hybrid Group
 * Licensed under the Apache 2.0 license.
 *
 *------------------------------------------
 *
 * Possible Sensor types
 *
 * Descriptor Value Changed
 * Apparent Wind Direction
 * Apparent Wind Speed
 * Dew Point
 * Elevation
 * Gust Factor
 * Heat Index
 * Humidity
 * Irradiance - org.bluetooth.characteristic.irradiance
 * Pollen Concentration
 * Rainfall
 * Pressure
 * Temperature
 * True Wind Direction
 * True Wind Speed
 * UV Index
 * Wind Chill
 * Barometric Pressure Trend
 * Magnetic Declination
 * Magnetic Flux Density - 2D
 * Magnetic Flux Density - 3D
 */

"use strict";

var Cylon = require("cylon");

var events = [
  /**
   * Emitted when the Sensor has fetched a new value
   *
   * @event analogRead
   */
  "analogRead",

  /**
   * Emitted when the Sensor reads a value above the specified upper
   * limit
   *
   * @event upperLimit
   */
  "upperLimit",

  /**
   * Emitted when the Sensor reads a value below the specified lower
   * limit
   *
   * @event lowerLimit
   */
  "lowerLimit"
];

/**
 * A GATT Sensor driver
 *
 * @constructor gatt-sensor
 *
 * @param {Object} opts
 * @param {String|Number} opts.pin the pin to connect to
 * @param {Number=} opts.upperLimit
 * @param {Number=} opts.lowerLimit
 */
var GATTSensor = module.exports = function GATTSensor(opts) {
  GATTSensor.__super__.constructor.apply(this, arguments);

  this.upperLimit = opts.upperLimit || 256;
  this.lowerLimit = opts.lowerLimit || 0;
  this.analogVal = null;

  if (this.pin == null) {
    throw new Error("No pin specified for GATTSensor. Cannot proceed");
  }

  this.commands = {
    analog_read: this.analogRead
  };

  this.events = events;
};

/** Subclasses the Cylon.Driver class */
Cylon.Utils.subclass(GATTSensor, Cylon.Driver);

/**
 * Gets the current value from the GATTSensor
 *
 * @return {Number} the current `this.analogVal`
 * @publish
 */
GATTSensor.prototype.analogRead = function() {
  return this.analogVal;
};

/**
 * Starts the GATTSensor
 *
 * @param {Function} callback to be triggered when started
 * @return {null}
 */
GATTSensor.prototype.start = function(callback) {
  this.connection.analogRead(this.pin, function(err, readVal) {
    this.analogVal = readVal;
    this.emit("analogRead", readVal);

    if (readVal >= this.upperLimit) {
      this.emit("upperLimit", readVal);
    } else if (readVal <= this.lowerLimit) {
      this.emit("lowerLimit", readVal);
    }
  }.bind(this));

  callback();
};

/**
 * Stops the GATTSensor
 *
 * @param {Function} callback to be triggered when stopped
 * @return {null}
 * @api private
 */
GATTSensor.prototype.halt = function(callback) {
  callback();
};
