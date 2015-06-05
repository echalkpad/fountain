#!/usr/bin/env node
/* jshint bitwise:false */
'use strict';

var EVENTID = {
    'startprogram' : 0x00,
    'stopprogram' : 0x01,
    'playsoundfile': 0x02,
    'playtone' : 0x03,
    'setoutputstate' : 0x04,
    'setinputmode' : 0x05,
    'getoutputstate' : 0x06,
    'getinputvalue' : 0x07,
    'resetinputscaledvalue' : 0x08,
    'messagewrite' : 0x09,
    'resetmotorposition' : 0x0A,
    'getbatterylevel' : 0x0B,
    'stopsoundplayback' : 0x0C,
    'keepalive' : 0x0D,
    'lsgetstatus' : 0x0E,
    'lswrite' : 0x0F,
    'lsread' : 0x10,
    'getcurrentprogramname' : 0x11,
    'messageread' : 0x13
  };

var nxtCommPort = "/dev/rfcomm0";

var NXTParser = require('./NXTParser');
var packetParser = new NXTParser(EVENTID);

var Nxt = require('mindstorms_bluetooth').Nxt;
var nxt = new Nxt(nxtCommPort,{ parser: packetParser});

var NxtSys = require('./nxtsys.js');
var nxtSys = new NxtSys(nxt);

var Tuxi = require('tuxi');
var tj = new Tuxi();

var DistanceSensor = require('./Sensor9846Distance');
var ds = new DistanceSensor(nxt, nxt.INPUT_PORT_4);

var log = require('book');
var debugLevel = 0;

//log.info(nxt);
log.info("===========");

nxt.sp.on("open", function () {

  log.info("Robot connected on " + nxtCommPort);

  // Set up hardware

  // nxt.set_input_state(nxt.INPUT_PORT_1, nxt.NO_SENSOR, nxt.RAWMODE);
  // nxt.set_input_state(nxt.INPUT_PORT_2, nxt.NO_SENSOR, nxt.RAWMODE);
  // nxt.set_input_state(nxt.INPUT_PORT_3, nxt.NO_SENSOR, nxt.RAWMODE);
  nxt.set_input_state(nxt.INPUT_PORT_4, nxt.LOWSPEED_9V, nxt.RAWMODE);

  log.info("Hardware set up");
 //   nxt.play_tone(440, 500);

 //   nxtSys.get_device_info();

  setTimeout(ds.readVersion(), 5000);
    //setTimeout(ds.readState(), 5000)

    // Start interval timer for sampling

 //   setInterval(function(){
 //       nxt.get_battery_level();
 //       nxt.get_input_values(nxt.INPUT_PORT_1);
 //
 //   }, 1000);

});

nxt.on('getinputvalue', function (data) {
  if (data[1] === nxtSys.EVENTID.getinputvalue) {
    if (data[2] !== 0) {
      log.info("Error. Get Input Value: " + nxt.nxt_error_messages[data[2]]);
    } else {
      log.info("event: getinputvalue");
      switch (data[3]) {
      case nxt.INPUT_PORT_0:
        log.info("input port " + data[3]);
        break;
      case nxt.INPUT_PORT_1:
        log.info("input port " + data[3]);
        break;
      case nxt.INPUT_PORT_2:
        log.info("input port " + data[3]);
        break;
      case nxt.INPUT_PORT_3:
        log.info("input port " + data[3]);
        break;
      default:
        log.info("Error: Get Input Value: Unexpected input port value: " + data[3]);
      }
      // if(data[3] === nxt.INPUT_PORT_1){
      // var adc = (data[11] << 8) | data[10];
      // log.info('Touch ADC: '  +  adc);
      // }
    }
  } else {
    log.panic("Error. The invoked callback function 'getinputvalue' does not match Event ID " + data[1] + ".");
  }
});

nxt.on('playtone', function (data) {
  if (data[1] === nxt.EVENTID.playtone) {
    if (data[2] !== 0) {
      log.info("Error. Play Tone: " + nxt.nxt_error_messages[data[2]]);
    } else {
      log.info("event: Play Tone complete.");
    }
  } else {
    log.panic("Error. The invoked callback function 'playtone' does not match Event ID " + data[1] + ".");
  }
});

nxt.on('getbatterylevel', function (data) {
  if (data[1] === nxt.EVENTID.getbatterylevel) {
    if (data[2] !== 0) {
      log.info("Error. Get Battery Level: " + nxt.nxt_error_messages[data[2]]);
    } else {
      var bat = (data[4] << 8) | data[3];
      log.info('event: Battery level: ' + bat);
    }
  } else {
    log.panic("Error. The invoked callback function 'getbatterylevel' does not match Event ID " + data[1] + ".");
  }
});

nxtSys.on('getdeviceinfo', function (data) {
  var idx;
  if (data[1] === nxtSys.EVENTID.getdeviceinfo) {
    if (data[2] !== 0) {
      log.info("Error. Get Device Info: " + nxt.nxt_error_messages[data[2]]);
    } else {
      var btAddress = '';
      for (idx = 18; idx <= 23; idx++) {
        btAddress += tj.toHex(data[idx]);
        if (idx !== 23) {
          btAddress += ':';
        }
      }
      var rssi = (data[28] << 8) | data[25];
      var flash = (data[32] << 8) | data[29];
      log.info("event: Device Info " + nxt.nxt_error_messages[data[2]]);
      log.info(" Name: " + data.slice(3, 18));
      log.info(" BT address: " + btAddress);
      log.info(" RSSI: " + rssi);
      log.info(" Free User Flash: " + flash);
    }
  } else {
    log.panic("Error. The invoked callback function 'getdeviceinfo' does not match Event ID " + data[1] + ".");
  }
});


nxt.on('setinputmode', function (data) {
  if (data[1] === nxt.EVENTID.setinputmode) {
    if (data[2] !== 0) {
      log.info("Error. Set Input Mode: " + nxt.nxt_error_messages[data[2]]);
    } else {
      log.info('event: setinputmode ' + nxt.nxt_error_messages[data[2]]);
      if (data.length !== 3) {
        log.debug("  Packet length: " + data.length);
        log.debug("  Packet:        " + JSON.stringify(data));
      }
    }
  } else {
    log.panic("Error. The invoked callback function 'setinputmode' does not match Event ID " + data[1] + ".");
  }
});

