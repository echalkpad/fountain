/**
 * This module looks for a device information service available over BLE and
 * then reads data from it.
 */

 "use strict";

 var Cylon = require('cylon');
 var noble = require('noble');
 var services = require('./services.json');

noble.on('discover', function(peripheral) {
  console.log("Peripheral discovered!")
  console.log("  Name: " + peripheral.advertisement.localName)
  console.log("  UUID: " + peripheral.uuid);
  console.log("  rssi: " + peripheral.rssi);
});

 Cylon.robot({
  connections: {
    bluetooth: { adaptor: 'ble', uuid: 'ca226ae6eb4b' }
  },

  devices: {
    deviceInfo: { driver: 'ble-device-information' }
  },

  work: function(my) {
    console.log("############################ Connection 'bluetooth'");
    console.log(my.bluetooth);
    console.log('~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Connected Peripherals');
    console.log(my.bluetooth.connectedPeripherals);
    for (var i in my.bluetooth.connectedPeripherals) {
      console.log('++++++++++++++ '); 
      console.log("Peripheral Key (uuid): "+i);
      console.log("Advertised Local Name: "+my.bluetooth.connectedPeripherals[i].peripheral.advertisement.localName);
      console.log('--');
      console.log(my.bluetooth.connectedPeripherals[i]);
      console.log('-- Services');
      for (var idx in my.bluetooth.connectedPeripherals[i].peripheral.advertisement.serviceUuids) {
        var su = my.bluetooth.connectedPeripherals[i].peripheral.advertisement.serviceUuids[idx];
        console.log("idx: "+idx+", service uuid: "+su);
  //     console.log(my.bluetooth.connectedPeripherals[i].peripheral.advertisement.serviceUuids[su]);
  //       console.log(su+": "+services[su].name+", "+services[su].type);
      }
      console.log('--------------');
    };   

  }}).start();
