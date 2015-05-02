#!/usr/bin/env node

var noble = require('noble');
var services = require('./services.json');

var scanStartTime;
var date = new Date();
var servers = {};

noble.on('stateChange', function(state) {
  if (state === 'poweredOn') {
    console.log("\nStarting scan.");
    scanStartTime = date.getTime();
    noble.startScanning();
  } else {
    noble.stopScanning();
    var scanStopTime = date.getTime();
    console.log("\nStopping scan after "+(scanStopTime-scanStartTime)+ " ms.");
  }
});

noble.on('discover', function(peripheral) {
  console.log("\n+++++++ Peripheral discovered.")
  console.log("   Name: " + peripheral.advertisement.localName)
  console.log("   UUID: " + peripheral.uuid);
  console.log("address: " + peripheral.address);

  servers[peripheral.uuid] = peripheral;
  var arrayLength = peripheral.advertisement.serviceUuids.length;
  for (var i = 0; i < arrayLength; i++) {
    var sid = peripheral.advertisement.serviceUuids[i];
    var serviceLabel = services.hasOwnProperty(sid) ? sid + "  "+services[sid].name : sid;
    console.log("service: " + serviceLabel);
  }
});
