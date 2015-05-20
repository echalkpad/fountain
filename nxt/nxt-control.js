#!/usr/bin/env node

var Nxt = require('mindstorms_bluetooth');

var nxt = new Nxt("/dev/tty.NXT-DevB");
nxt.play_tone(440, 1000);

var scanStartTime;
var date = new Date();
var servers = {};

noble.on('stateChange', function(state) {
  console.log("Adapter state change: "+state);
  if (state === 'poweredOn') {
    console.log("Power on. Initiate scanning.");
    noble.startScanning();
  } else if (state === 'poweredOff') {
    noble.stopScanning();
    var scanStopTime = date.getTime();
    console.log("\nPower off.  Terminate scanning.");
  }
});

noble.on('scanStart', function() {
    console.log("\n======= Scan started.");
    scanStartTime = date.getTime();
});

noble.on('scanStop', function() {
    var scanStopTime = date.getTime();
    console.log("\n======= Scan stopped after "+(scanStopTime-scanStartTime)+ " ms.");
});

noble.on('discover', function(peripheral) {
  console.log("\n+++++++ Peripheral discovered.");
  console.log("   Name: " + peripheral.advertisement.localName);
  console.log("   UUID: " + peripheral.uuid);
  console.log("address: " + peripheral.address);

  console.log("------- advertisement object");
  console.log(peripheral.advertisement);

  console.log("------- peripheral object");
 // console.log(peripheral);

  servers[peripheral.uuid] = peripheral;

  if (peripheral.uuid === 'eb2299410ba7') {
    console.log("------> connection requested");
    peripheral.connect();
  }

});

noble.on('connect', function () {
  console.log('------- connected');
//  var pd = servers[]
  peripheral.discoverServices(); // any service UUID
 // service.discoverIncludedServices(); // any service UUID
});

noble.on('disconnect', function () {
  console.log('disconnected');
});

noble.on('rssiUpdate', function(rssi) {
  console.log("\nrssi update: "+rssi);
});

noble.on('servicesDiscover', function(s) {
  console.log("\nDiscovered services... ");

  // var arrayLength = peripheral.advertisement.serviceUuids.length;
  // for (var i = 0; i < arrayLength; i++) {
  //   var sid = peripheral.advertisement.serviceUuids[i];
  //   var serviceLabel = services.hasOwnProperty(sid) ? sid + "  "+services[sid].name : sid;
  //   console.log("advertised service: " + serviceLabel);
  // }
//  console.log(peripheral);

//    services[sid].discoverCharacteristics() // any characteristic UUID
});

noble.on('characteristicsDiscover', function(c) {
  console.log("\ndiscovered characteristic: "+c);
});
