#!/usr/bin/env node

var Nxt = require('mindstorms_bluetooth').Nxt;

var nxt = new Nxt("/dev/rfcomm0");
//var nxt = new Nxt("/org/bluez/569/hci0/dev_00_16_53_1A_59_B9");

//console.log(nxt);
console.log("===========");

nxt.on('getinputvalue', function(data) {
  console.log("event: getinputvalue");
    if(data[3] == nxt.INPUT_PORT_1){
        var adc = (data[11] << 8) | data[10];
        console.log('Touch ADC: ' + adc);
    }
});

nxt.on('getbatterylevel', function(data) {
    console.log("event: getbatterylevel");
    console.log(data);
    if (data[1] == nxt.EVENTID.getbatterylevel) {
        var bat = (data[4] << 8) | data[3];
        console.log('Battery level: '+bat);
    }
});

nxt.sp.on("open", function () {
    // Init
    console.log("Robot connected")
    nxt.play_tone(440, 500);

    // Set up hardware
    nxt.set_input_state(nxt.INPUT_PORT_1, nxt.SWITCH, nxt.BOOLEANMODE);
    console.log("Hardware set up");

    // Start interval
    setInterval(function(){
        nxt.get_battery_level();
 //       nxt.get_input_values(nxt.INPUT_PORT_1);
    }, 500);

//    nxt.get_battery_level();
});

//
