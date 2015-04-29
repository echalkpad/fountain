/**
 * This module looks for a device information service available over BLE and
 * then reads data from it.
 */

"use strict";

var Cylon = require('cylon');
var noble = require('noble');

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
    console.log(my.bluetooth);
    console.log(my.bluetooth.peripherals);
    my.deviceInfo.getManufacturerName(my.display);

  }}).start();

// noble.on('stateChange', function(state) {
//   if (state === 'poweredOn') {
//     console.log("Starting scan.");
//     noble.startScanning();
//   } else {
//     console.log("Stopping scan.");
//     noble.stopScanning();
//   }
// });

// noble.on('discover', function(peripheral) {
//   console.log("Peripheral discovered!")
//   console.log("  Name: " + peripheral.advertisement.localName)
//   console.log("  UUID: " + peripheral.uuid);
//   console.log("  rssi: " + peripheral.rssi);
// });
