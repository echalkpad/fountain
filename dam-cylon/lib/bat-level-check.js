/**
 * This is a simple example of how to use cylon-ble.  It is copied
 * directly from the module's npm page, except that the uuid was
 * changed to match my BLE Nano.
 */

"use strict";

var Cylon = require('cylon');

Cylon.robot({
  connections: {
    bluetooth: { adaptor: 'ble', uuid: '7dc34621191a4e8d87e9747f561038f6' }
  },

  devices: {
    battery: { driver: 'ble-battery-service' }
  },

  work: function(my) {
    my.battery.getBatteryLevel(function(err, data) {
      if (!!err) {
        console.log("Error: ", err);
        return;
      }

      console.log("Data: ", data);
    });
  }
}).start();
