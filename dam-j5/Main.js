// This is the main entry point for a simple demo of Johnny-Five controlling
// Remote Device Drivers on an Arduino host through Firmata.
//
// This program is strict-mode throughout, and it uses some ES6 features.
//
// Doug Johnson, April 2016

const five = require("johnny-five");

const RDD = require("./RemoteDeviceDriver");
const rddErr = require("./RDDStatus");
const rddCmd = require('./RDDCommand');

const log4js = require("log4js");
const logger = log4js.getLogger("Main");
logger.setLevel('DEBUG');

logger.info(`----Begin RemoteDeviceDriver exercise.`);

const ledPin = 13;
let ledOn = true;

// Create and initialize a J5 board object

const serialPortName = "COM42";
// const serialPortName = "/dev/cu.usbmodem621";

const board = new five.Board({port: serialPortName, repl: false});

// When the board is ready, start blinking the LED and then trigger the rest of the program to run

board.on("ready", function() {
  board.info("Board object is ready.","hi!");
  logger.info(`Connected to ${board.io.firmware.name} -${board.io.firmware.version.major}.${board.io.firmware.version.minor}`);
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

// Strings from Firmata host to client are usually error messages

board.on("string",function (remoteString) {
  logger.warn(`[STRING_DATA] ${remoteString}`);
});

// Once the light is blinking, we're ready to really start work

board.on("blinking", function () {
  let dd = new RDD.RemoteDeviceDriver({'board': board, skipCapabilities: false});

// Open the remote device drivers and read version identifiers

  let unitNamesOfInterest = ["Meta:0","Hello:0","Foo:0"];

  let handleMap = {};
  for (let unit of unitNamesOfInterest) {
    let openEvent = dd.open(unit,1);
    logger.trace(`Response event to wait for: ${openEvent}`);
    dd.once(openEvent, (response) => {
      logger.trace(`${openEvent} handler invoked.`);
      if (response.status >= 0) {
        logger.debug(`Status value from open() is ${response.status}`);
        let readEvent = dd.read(response.handle,rddCmd.CDR.DriverVersion,256);
        dd.once(readEvent, (response) => {
          logger.trace(`${readEvent} handler invoked.`);
          if (response.status >= 0) {
            logger.debug(`Status value from read() is ${response.status}`);
            let sv = new rddCmd.SemVer(response.datablock);
            logger.info(`Driver: ${sv.toString()}`);
            handleMap[response.handle] = sv;
          } else {
            logger.error(`Error value from read() is ${response.status}`);
          }});
      } else {
        logger.error(`Error value from open() is ${response.status}`);
      }
    });
  }

  logger.debug("Main program device queries all issued.");
  logger.info("Main program processing completed.");
});

