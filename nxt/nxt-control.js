#!/usr/bin/env node

var nxtCommPort = "/dev/rfcomm0";

var Nxt = require('mindstorms_bluetooth').Nxt;
var nxt = new Nxt(nxtCommPort);

var NxtSys = require('./nxtsys.js');
var nxtSys = new NxtSys(nxt);

var Tuxi = require('tuxi');
var tj = new Tuxi();

//console.log(nxt);
console.log("===========");

nxt.sp.on("open", function () {

    console.log("Robot connected on "+nxtCommPort);

    // Set up hardware

    nxt.set_input_state(nxt.INPUT_PORT_0, nxt.NO_SENSOR, nxt.RAWMODE);
    nxt.set_input_state(nxt.INPUT_PORT_1, nxt.NO_SENSOR, nxt.RAWMODE);
    nxt.set_input_state(nxt.INPUT_PORT_2, nxt.NO_SENSOR, nxt.RAWMODE);
    nxt.set_input_state(nxt.INPUT_PORT_3, nxt.LOWSPEED_9V, nxt.RAWMODE);

    console.log("Hardware set up");
    nxt.play_tone(440, 500);

    // Start interval
    setInterval(function(){
        nxt.get_battery_level();
 //       nxt.get_input_values(nxt.INPUT_PORT_1);
 //
        nxtSys.get_device_info();
    }, 1000);

});

nxt.on('getinputvalue', function(data) {
  if (data[1] == nxtSys.EVENTID.getinputvalue) {
    if (data[2] != 0) {
      console.log("Error. Get Input Value: "+nxt.nxt_error_messages[data[2]]);
    } else {
      console.log("event: getinputvalue");
      switch (data[3]) {
        case nxt.INPUT_PORT_0:
          console.log("input port "+data[3]);
          break;
        case nxt.INPUT_PORT_1:
          console.log("input port "+data[3]);
          break;
        case nxt.INPUT_PORT_2:
          console.log("input port "+data[3]);
          break;
        case nxt.INPUT_PORT_3:
          console.log("input port "+data[3]);
          break;
        default:
          console.log("Error: Get Input Value: Unexpected input port value: "+data[3]);
      }
      // if(data[3] == nxt.INPUT_PORT_1){
      // var adc = (data[11] << 8) | data[10];
      // console.log('Touch ADC: ' + adc);
  }
});

nxt.on('playtone', function(data) {
  if (data[1] == nxt.EVENTID.playtone) {
    if (data[2] != 0) {
      console.log("Error. Play Tone: "+nxt.nxt_error_messages[data[2]]);
    } else {
      console.log("event: Play Tone complete.");
    }
  } else {
    console.log("Error. The invoked callback function 'playtone' does not match Event ID "+data[1]+".");
  }
})

nxt.on('getbatterylevel', function(data) {
  if (data[1] == nxt.EVENTID.getbatterylevel) {
    if (data[2] != 0) {
      console.log("Error. Get Battery Level: "+nxt.nxt_error_messages[data[2]]);
    } else {
      var bat = (data[4] << 8) | data[3];
      console.log('event: Battery level: '+bat);
    }
  } else {
    console.log("Error. The invoked callback function 'getbatterylevel' does not match Event ID "+data[1]+".");
  }
});

nxtSys.on('getdeviceinfo',function(data) {
  if (data[1] == nxtSys.EVENTID.getdeviceinfo) {
    if (data[2] != 0) {
      console.log("Error. Get Device Info: "+nxt.nxt_error_messages[data[2]]);
    } else {
      var btAddress = '';
      for (var idx=18; idx<=23; idx++) {
        btAddress += tj.toHex(data[idx]);
        if (idx != 23) {
          btAddress += ':';
        }
      }
      var rssi = (data[28] << 8) | data[25];
      var flash = (data[32] << 8) | data[29];
      console.log("event: Device Info");
      console.log(" Name: "+data.slice(3,18));
      console.log(" BT address: "+btAddress);
      console.log(" RSSI: "+rssi);
      console.log(" Free User Flash: "+flash);
    }
  } else {
  console.log("Error. The invoked callback function 'getdeviceinfo' does not match Event ID "+data[1]+".");
  }
});


