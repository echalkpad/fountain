// This is the main entry point for a simple demo of Johnny-Five controlling
// a Servo using a Remote Device Driver controller.
//
// This program is strict-mode throughout, and it uses some ES6 features.
//
// Doug Johnson, April 2016

const log4js = require("log4js");
const five = require("johnny-five");
const fn = require("../node_modules/johnny-five/lib/fn");

const RDD = require("../lib/RemoteDeviceDriver");
const rddErr = require("../lib/RDDStatus");
const rddCmd = require("../lib/RDDCommand");

const logger = log4js.getLogger("useRDDServo");
logger.setLevel('INFO');

const servoController = require("../lib/servo/ServoRDD").ServoRDD;

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
  const led = new five.Led(13);
  led.blink(500);
  board.emit("blinking");
});

// Once the light is blinking, we're ready to really start work

board.on("blinking", function () {
  logger.trace(`Controller property keys are ${Object.keys(servoController)}.`);

  const servo = new five.Servo({
    controller: servoController,
    custom: {unit: "Servo:0",flags: 1},
    pin: 3,
    center: true
  });

  const pot = [];
  for (let aPin of ["A6","A7","A8","A9"]) {
      const dial = new five.Sensor({
        pin: aPin,
        freq: 50
      });
      pot.push(dial);
  }

  pot[0].on("data",() => {
    let newPosition = fn.fmap(pot[0].raw,0,1023,0,180);
    logger.debug(`New position is ${newPosition} degrees.`);
    servo.to(newPosition);
  });
});
