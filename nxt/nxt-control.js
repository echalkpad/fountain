#!/usr/bin/env node

var Nxt = require('mindstorms_bluetooth').Nxt;

var nxt = new Nxt("/dev/rfcomm0");
//var nxt = new Nxt("/org/bluez/569/hci0/dev_00_16_53_1A_59_B9");

console.log(nxt);

nxt.on('getinputvalue', function(data) {
    if(data[3] == nxt.INPUT_PORT_1){
        var adc = (data[11] << 8) | data[10];
        console.log('Touch ADC: ' + adc);
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
        nxt.get_input_values(nxt.INPUT_PORT_1);
    }, 500);
});