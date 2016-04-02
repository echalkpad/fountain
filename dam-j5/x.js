(function () {
   'use strict';
//-----

let robo = require("johnny-five");
let firmata = require("firmata");
let RDD = require("./RemoteDeviceDriver");

let ledPin = 13;
let ledOn = true;

// Create and initialize a board object

let serialPortName;
serialPortName = "COM42";
// serialPortName = "/dev/cu.usbmodem621";

let board = new firmata.Board(serialPortName, function(err) {
  if (err) {
    console.log(err);
    return;
  }
  board.pinMode(ledPin, board.MODES.OUTPUT);
});

// When the board is ready, start blinking the LED and then trigger the rest of the program to run

board.on("ready", function() {
  console.log("connected");
  console.log("Firmware: " + board.firmware.name + "-" + board.firmware.version.major + "." + board.firmware.version.minor);
  setInterval(function() {
    if (ledOn) {
      board.digitalWrite(ledPin, board.HIGH);
    } else {
      board.digitalWrite(ledPin, board.LOW);
    }
    ledOn = !ledOn;
  }, 500);
  board.emit("blinking");
});

board.on("blinking", function () {
  let dd = new RDD({'board': board});
  let handle = dd.open("META:0",0);
  console.info("Returned handle: ",handle);
  console.log("Blinking sysex code DEVICE_RESPONSE: ",RDD.SYSEX('DEVICE_RESPONSE'));
  console.log("Blinking action code OPEN: ",RDD.ACTION('OPEN'));
  console.log("Blinking status code ESUCCESS: ",RDD.STATUS('ESUCCESS'));
});


//-----
}());