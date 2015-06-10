#!/usr/bin/env node
/* jshint bitwise:false */
'use strict';

// This node.js program receives data from a Lego NXT brick.

var NXTListenerConfig = function (container) {

//console.log("Container: ", container);

this.nxt = container.nxt;
this.log = container.log;

this.nxt.on('getinputvalue', function (data) {
  if (data[1] === this.nxt.EVENTID.getinputvalue) {
    if (data[2] !== 0) {
      this.log.info("Error. Get Input Value: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      switch (data[3]) {
      case this.nxt.INPUT_PORT_0:
        this.log.info("input port " + data[3]);
        break;
      case this.nxt.INPUT_PORT_1:
        this.log.info("input port " + data[3]);
        break;
      case this.nxt.INPUT_PORT_2:
        this.log.info("input port " + data[3]);
        break;
      case this.nxt.INPUT_PORT_3:
        this.log.info("input port " + data[3]);
        break;
      default:
        this.log.info("Error: Get Input Value: Unexpected input port value: " + data[3]);
      }
    }
  } else {
    this.log.panic("Error. The invoked callback function 'getinputvalue' does not match Event ID " + data[1] + ".");
  }
});

this.nxt.on('playtone', function (data) {
  if (data[1] === this.nxt.EVENTID.playtone) {
    if (data[2] !== 0) {
      this.log.info("Error. Play Tone: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      this.log.info("event: Play Tone complete.");
    }
  } else {
    this.log.panic("Error. The invoked callback function 'playtone' does not match Event ID " + data[1] + ".");
  }
});

this.nxt.on('getbatterylevel', function (data) {
  if (data[1] === this.nxt.EVENTID.getbatterylevel) {
    if (data[2] !== 0) {
      this.log.info("Error. Get Battery Level: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      var bat = (data[4] << 8) | data[3];
      this.log.info("event",'Battery level: ' + bat);
    }
  } else {
    this.log.panic("Error. The invoked callback function 'getbatterylevel' does not match Event ID " + data[1] + ".");
  }
}.bind(this));

// nxtSys.on('getdeviceinfo', function (data) {
//   var idx;
//   if (data[1] === nxtSys.EVENTID.getdeviceinfo) {
//     if (data[2] !== 0) {
//       this.log.info("Error. Get Device Info: " + this.nxt.nxt_error_messages[data[2]]);
//     } else {
//       var btAddress = '';
//       for (idx = 18; idx <= 23; idx++) {
//         btAddress += tj.toHex(data[idx]);
//         if (idx !== 23) {
//           btAddress += ':';
//         }
//       }
//       var rssi = (data[28] << 8) | data[25];
//       var flash = (data[32] << 8) | data[29];
//       this.log.info("event: Device Info " + this.nxt.nxt_error_messages[data[2]]);
//       this.log.info(" Name: " + data.slice(3, 18));
//       this.log.info(" BT address: " + btAddress);
//       this.log.info(" RSSI: " + rssi);
//       this.log.info(" Free User Flash: " + flash);
//     }
//   } else {
//     this.log.panic("Error. The invoked callback function 'getdeviceinfo' does not match Event ID " + data[1] + ".");
//   }
// });


this.nxt.on('setinputmode', function (data) {
  if (data[1] === this.nxt.EVENTID.setinputmode) {
    if (data[2] !== 0) {
      this.log.info("Error. Set Input Mode: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      this.log.info("event",'setinputmode ' + this.nxt.nxt_error_messages[data[2]]);
      if (data.length !== 3) {
        this.log.debug("  Packet length: " + data.length);
        this.log.debug("  Packet:        " + JSON.stringify(data));
      }
    }
  } else {
    this.log.panic("Error. The invoked callback function 'setinputmode' does not match Event ID " + data[1] + ".");
  }
}.bind(this));

};

NXTListenerConfig.prototype.nxt = null;
NXTListenerConfig.prototype.log = null;


module.exports = NXTListenerConfig;
