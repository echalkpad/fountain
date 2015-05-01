#!/usr/bin/env node

var noble = require('noble');

var scanStartTime;
var date = new Date();

noble.on('stateChange', function(state) {
  if (state === 'poweredOn') {
    console.log("Starting scan.");
    scanStartTime = date.getTime();
    noble.startScanning();
  } else {
    noble.stopScanning();
    var scanStopTime = date.getTime();
    console.log("Stopping scan after "+(scanStopTime-scanStartTime)+ " ms.");
  }
});

noble.on('discover', function(peripheral) {
  console.log("+++++++ Peripheral discovered.")
  console.log("   Name: " + peripheral.advertisement.localName)
  console.log("   UUID: " + peripheral.uuid);
  console.log("address: " + peripheral.address);
  console.log(" advert: " + peripheral.advertisement);
  console.log("-------");
  console.log(peripheral);
  console.log("=======");
});
