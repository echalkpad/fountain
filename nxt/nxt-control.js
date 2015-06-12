#!/usr/bin/env node
'use strict';


var Main = function () {

// This node.js program reads data from a Lego NXT brick.

var Nxt = require('./mindstorms_bluetooth/nxt').Nxt;
var NxtSys = require('./nxtsys.js');
var DistanceSensor = require('./Sensor9846Distance');
var NXTListenerConfig = require("./NXTListenerConfigA");

var tux = new require('./tuxijs');
this.log = new tux.Logger();
var logPrefix = "Main";

//console.log("NXTListenerConfig: ", NXTListenerConfig);

this.log.info(logPrefix, "== Begin Static Initialization ==");

var nxtCommPort = "/dev/rfcomm0";

var nxtSys;
var ds;
var ear;

try {
  this.nxt = new Nxt(nxtCommPort);
  nxtSys = new NxtSys(this.nxt);
} catch (e) {
  console.log(e);
  process.exit(1);
}

this.log.info(logPrefix,"== Static Initialization Complete ==");

this.nxt.sp.on("open", function () {

  this.log.info(logPrefix,"Connected to NXT on " + nxtCommPort);
  ds = new DistanceSensor(this.nxt, this.nxt.INPUT_PORT_4);

//  console.log("this 2: ", this);

  ear = new NXTListenerConfig(this.nxt);

  this.log.info(logPrefix, "== Connected Initialization Complete ==");
  //   nxt.play_tone(440, 500);

  //   nxtSys.get_device_info();

  //setTimeout(ds.readVersion(), 5000);
  //setTimeout(ds.readState(), 5000)

  // Start interval timer for sampling

  setInterval(function(){
    this.nxt.get_battery_level();
  }.bind(this), 1000);

}.bind(this));
  // console.log("this 1: ",this);
  // console.log("me 1: ",me);
  exports.nxt = this.nxt;
  exports.Main = this.Main;
};

Main.prototype.nxt = null;
Main.prototype.log = null;

var me = new Main();
//console.log("me 2: ",me);
