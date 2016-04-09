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
const rddCmd = require('./RDDCommand');

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

// Open the remote device drivers of interest

  let pack = [];
  pack[0] = dd.open("Meta:0",1);
  pack[1] = dd.open("Hello:0",1);

  console.log("Main program device OPEN requests all issued.");

  for (let i=0; i<pack.length; i++) {
    console.log(`Device open promise ${i}: ${pack[i]}`);
  }

  // Wait for the open() calls to complete

  Promise.all(pack)
  .then((values) => {
    console.log(`Returned open promise values (fulfill): ${values}`);
    console.log(`Handle value 0 from open() is ${values[0].handle}`);
    console.log(`Handle value 1 from open() is ${values[1].handle}`);
    // this.handle.Hello = values[1].handle;
    // console.log(`Returned handles: Meta:${this.handle.Meta}, Hello:${this.handle.Hello}`);

    // If both opens worked okay, read() the driver versions

    let readProms = [];
    console.log(`Main program device version READ requests about to be issued for register ${rddCmd.CDR.DriverVersion}`);

    readProms[0] = dd.read(values[0].handle,rddCmd.CDR.DriverVersion,256);
    readProms[1] = dd.read(values[1].handle,rddCmd.CDR.DriverVersion,256);
    console.log(`Returned read promise values: ${readProms}`);

    console.log("Main program device version READ requests all issued.");

    Promise.all(readProms)
    .then((values) => {
      console.log(`Returned read promise values (fulfill): ${values}`);
    })
    .catch((values) => {
      console.log(`Returned read promise values (reject):  ${values}`);
    });

   console.log("Main program starter processing completed.");
  });
});
