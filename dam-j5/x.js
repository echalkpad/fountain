// This is the main entry point for a simple demo of Johnny-Five controlling
// Remote Device Drivers on an Arduino host through Firmata.
//
// This program is strict-mode throughout, and it uses some ES6 features.
//
// The program started life as a copy of blink.js, an example program that
// comes in the JavaScript Firmata client library used with Johnny-Five.
//
// Doug Johnson, April 2016

console.log(`\n----Begin RemoteDeviceDriver exercise.`);

const robo = require("johnny-five");
const firmata = require("firmata");
const RDD = require("./RemoteDeviceDriver");
const rddErr = require("./RDDStatus");

const ledPin = 13;
let ledOn = true;

// Create and initialize a board object

const serialPortName = "COM42";
// const serialPortName = "/dev/cu.usbmodem621";

const board = new firmata.Board(serialPortName, function(err) {
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

board.on("string",function (remoteString) {
  console.log("Rcvd: [STRING_DATA] "+remoteString);
});

board.on("blinking", function () {
  let dd = new RDD({'board': board, skipCapabilities: false});

  let pack = [];
  pack[0] = dd.open("Meta:0",1);
  pack[1] = dd.open("Hello:0",1);

  for (let i=0; i<pack.length; i++) {
    console.log(`Promised device open ${i}: ${pack[i]}`);
  }

  Promise.all(pack).then((values) => {
    console.log(`Returned promise values (fulfill): ${values}`);
  })
  .catch((values) => {
    console.log(`Returned promise values (reject):  ${values}`);
  });

  console.log("Main program open phase complete.");
});
