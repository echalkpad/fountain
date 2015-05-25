#!/usr/bin/env node

var Nxt = require('mindstorms_bluetooth').Nxt;
var nxt = new Nxt("/dev/rfcomm0");

var NxtSys = require('./nxtsys.js');
var nxtSys = new NxtSys(nxt);

var Tuxi = require('tuxi');
var tj = new Tuxi();

//console.log(nxt);
console.log("===========");

nxt.sp.on("open", function () {
    // Init
    console.log("Robot connected")
    nxt.play_tone(440, 500);

    // Set up hardware
    nxt.set_input_state(nxt.INPUT_PORT_0, nxt.NO_SENSOR, nxt.RAWMODE);
    nxt.set_input_state(nxt.INPUT_PORT_1, nxt.NO_SENSOR, nxt.RAWMODE);
    nxt.set_input_state(nxt.INPUT_PORT_2, nxt.NO_SENSOR, nxt.RAWMODE);
    nxt.set_input_state(nxt.INPUT_PORT_3, nxt.LOWSPEED_9V, nxt.RAWMODE);
    console.log("Hardware set up");

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
        console.log("Get Input Value: Error. "+nxt.nxt_error_messages[data[2]]);
      } else {
        console.log("event: getinputvalue");
        switch (data[3]) {
          case nxt.INPUT_PORT_0:
            break;
          case nxt.INPUT_PORT_1:
            break;
          case nxt.INPUT_PORT_2:
            break;
          case nxt.INPUT_PORT_3:
            break;
          default:
            console.log("Error: Unexpected input port value: "+data[3]);
        }
        // if(data[3] == nxt.INPUT_PORT_1){
        // var adc = (data[11] << 8) | data[10];
        // console.log('Touch ADC: ' + adc);
    }
});

nxt.on('getbatterylevel', function(data) {
    if (data[1] == nxt.EVENTID.getbatterylevel) {
        var bat = (data[4] << 8) | data[3];
        console.log('Battery level: '+bat);
    } else {
      console.log("event: getbatterylevel");
      console.log(data);
    }
});

nxtSys.on('getdeviceinfo',function(data) {
    if (data[1] == nxtSys.EVENTID.getdeviceinfo) {
      if (data[2] == 0) {
        console.log("Device Info: OK");
        var btAddress = '';
        for (var idx=18; idx<=23; idx++) {
          btAddress += tj.toHex(data[idx]);
          if (idx != 23) {
            btAddress += ':';
          }
        }
        var rssi = (data[28] << 8) | data[25];
        var flash = (data[32] << 8) | data[29];
        console.log(" Name: "+data.slice(3,18));
        console.log(" BT address: "+btAddress);
        console.log(" RSSI: "+rssi);
        console.log(" Free User Flash: "+flash);
 //       console.log(data);
      } else {
        console.log("Device Info: Error. "+nxt.nxt_error_messages[data[2]]);
      }
    } else {
      console.log("event: getdeviceinfo");
      console.log(data);
    }

});


