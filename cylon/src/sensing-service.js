/*
 * sensing-service.js
 * This module is an interface layer that mimics an api that might be used to
 * talk over Bluetooth BLE to a device implementing the service defined by 
 * the Bluetooth SIG as org.bluetooth.service.environmental_sensing.  This
 * module is intended for use with Cylon.js, and is based on the existing
 * module cylon-gpio.
 * 
 * Doug Johnson, 2015
 *
 * cylon-gpio
 * http://cylonjs.com
 *
 * Copyright (c) 2013-2014 The Hybrid Group
 * Licensed under the Apache 2.0 license.
*/

"use strict";

var Drivers = {
  "gatt-sensor": require("./gatt-sensor"),
    "led": require("./led"),
};



module.exports = {
  drivers: Object.keys(Drivers),

  driver: function(opts) {
    for (var d in Drivers) {
      if (opts.driver === d) {
        return new Drivers[d](opts);
      }
    }

    return null;
  }
};
