/**
 * This module looks for a device information service available over BLE and
 * then reads data from it.
 */

"use strict";

var Cylon = require('cylon');

Cylon.robot({
  connections: {
    bluetooth: { adaptor: 'ble', uuid: 'eb2299410ba7' }
  },

  devices: {
    deviceInfo: { driver: 'ble-device-information' }
  },

  display: function(err, data) {
    if (!!err) {
      console.log("Error: ", err);
      return;
    }

    console.log("callback for getManufacturerName")
    console.log("Data: ", data);
  },

  work: function(my) {
    my.deviceInfo.getManufacturerName(my.display);
  }}).start();
