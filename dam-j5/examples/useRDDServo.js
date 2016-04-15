// This is the main entry point for a simple demo of Johnny-Five controlling
// a Servo using a Remote Device Driver controller.
//
// This program is strict-mode throughout, and it uses some ES6 features.
//
// Doug Johnson, April 2016

const log4js = require("log4js");
const five = require("johnny-five");

const RDD = require("../lib/RemoteDeviceDriver");
const rddErr = require("../lib/RDDStatus");
const rddCmd = require("../lib/RDDCommand");

const logger = log4js.getLogger("Main");
const RDDServo = require("../lib/RDDServo");

const ledPin = 13;
let ledOn = true;

// Create and initialize a board object

const serialPortName = "COM44";
// const serialPortName = "/dev/cu.usbmodem621";

const board = new five.Board({port: serialPortName, repl: false});

// Strings from Firmata host to client are usually error messages

board.on("string",function (remoteString) {
  logger.warn(`[STRING_DATA] ${remoteString}`);
});

// When the board is ready, start blinking the LED and then trigger the rest of the program to run

board.on("ready", function() {
  logger.info(`Connected to ${board.io.firmware.name}-${board.io.firmware.version.major}.${board.io.firmware.version.minor}`);
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

// Once the light is blinking, we're ready to really start work

board.on("blinking", function () {
  const sC = RDDServo.Controller;
  logger.trace(`Controller type is ${typeof sC}.`);
  logger.trace(`Controller property keys are ${Object.keys(sC)}.`);

  const servo = new five.Servo({
    controller: sC,
    unit: "Servo:0"
  });

  servo.sweep();
});
