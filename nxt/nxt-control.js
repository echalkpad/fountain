'use strict';

function Main () {

// This node.js program reads data from a Lego NXT brick.

var Nxt = require('./mindstorms_bluetooth/nxt').Nxt;
var NxtSys = require('./nxtsys.js');
var DistanceSensor = require('./Sensor9846Distance');
var NXTListenerConfig = require("./NXTListenerConfigA");

var Tuxi = require('./tuxijs');
var tux = new Tuxi();
var log = new tux.Logger();
var logPrefix = "Main";

log.info(logPrefix, "== Begin Static Initialization ==");

var nxtCommPort = "/dev/rfcomm0";

var nxt;
var nxtSys;
var ds;
var ear;

try {
  nxt = new Nxt(nxtCommPort);
  nxtSys = new NxtSys(this.nxt);
} catch (e) {
  console.log(e);
  process.exit(1);
}

log.info(logPrefix,"== Static Initialization Complete ==");

nxt.sp.on("open", function () {
  log.info(logPrefix, "== Begin Connected Initialization ==");
  log.info(logPrefix,"Connected to NXT on " + nxtCommPort);

  ds = new DistanceSensor(nxt, nxt.INPUT_PORT_4);
  ear = new NXTListenerConfig(nxt);

  log.info(logPrefix, "== Connected Initialization Complete ==");

  //   nxt.play_tone(440, 500);
  //   nxtSys.get_device_info();
  //   setTimeout(ds.readVersion(), 5000);
  //   setTimeout(ds.readState(), 5000)

  // Start interval timer for sampling

  setInterval(function(){
    nxt.get_battery_level();
  }.bind(this), 1000);

}.bind(this));
}

new Main();
