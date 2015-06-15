'use strict';

function Main () {

// This node.js program reads data from a Lego NXT brick.

var Nxt = require('./mindstorms_bluetooth/nxt').Nxt;
var DistanceSensor = require('./Sensor9846Distance');
var NXTListenerConfig = require("./NXTListenerConfigA");

var Tuxi = require('./tuxijs');
var tux = new Tuxi();
var log = new tux.Logger();
var logPrefix = "Main";

log.info(logPrefix, "== Static Initialization Started ==");

var nxtCommPort = "/dev/rfcomm0";

var nxt;
var bat;
var ear;
var nxtOptions = {'log': log};

try {
  nxt = new Nxt(nxtCommPort,nxtOptions);
} catch (e) {
  console.log(e);
  process.exit(1);
}

log.info(logPrefix,"== Static Initialization Completed ==");

nxt.sp.on("open", function () {
  log.info(logPrefix, "== Connected Initialization Started ==");
  log.info(logPrefix, "-- Connected to NXT on " + nxtCommPort);

  bat = new DistanceSensor(nxt, nxt.INPUT_PORT_4);
  ear = new NXTListenerConfig(nxt);

  nxt.get_device_info();
  nxt.get_firmware_version();

  log.info(logPrefix, "== Connected Initialization Completed ==");

  bat.readVersion();

  //   setTimeout(bat.readVersion(), 5000);
  //   setTimeout(bat.readState(), 5000)
  //   nxt.play_tone(440, 500);

  // Start interval timer for sampling

  setInterval(function(){
    nxt.ls_get_status(nxt.INPUT_PORT_4);
  }.bind(this), 1000);

}.bind(this));
}

new Main();
