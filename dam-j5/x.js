(function () {
   'use strict';
//-----
//

var robo = require("johnny-five");
var firmata = require("firmata");
var RDD = require("./RemoteDeviceDriver");

var ledPin = 13;
var ledOn = true;

// Create and initialize a board object

var serialPortName;
serialPortName = "COM42";
// serialPortName = "/dev/cu.usbmodem621";

var board = new firmata.Board(serialPortName, function(err) {
  if (err) {
    console.log(err);
    return;
  }
  board.pinMode(ledPin, board.MODES.OUTPUT);
});

// When the board is ready, start blinking the LED and trigger the rest of the program to run

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
  // console.log("Ready status code ESUCCESS: ",RDD.STATUS('ESUCCESS'));
  board.emit("blinking");
});

board.on("blinking", function () {
  var dd = new RDD({'board': board});
  var handle = dd.open("META:0",0);
  console.info("Returned handle: ",handle);
  console.log("Blinking sysex code DEVICE_QUERY: ",RDD.SYSEX('DEVICE_QUERY'));
  console.log("Blinking action code OPEN: ",RDD.ACTION('OPEN'));
  console.log("Blinking status code ESUCCESS: ",RDD.STATUS('ESUCCESS'));
});


//-----
}());