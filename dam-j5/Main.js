// This is the main entry point for a simple demo of Johnny-Five controlling
// Remote Device Drivers on an Arduino host through Firmata.
//
// This program is strict-mode throughout, and it uses some ES6 features.
//
// Doug Johnson, April 2016

const five = require("johnny-five");
const log4js = require("log4js");

const RDD = require("./RemoteDeviceDriver");
const rddErr = require("./RDDStatus");
const rddCmd = require('./RDDCommand');

let logger = log4js.getLogger("Main");

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
  let dd = new RDD({'board': board, skipCapabilities: false});

// Open the remote device drivers of interest

  let unitNamesOfInterest = ["Meta:0","Hello:0","Foo:0"];

  let openQueries = [];
  for (let unit of unitNamesOfInterest) {
    openQueries.push(dd.open(unit,1));
  }

  logger.debug("Main program device OPEN requests all issued.");

  // Wait for the open() calls to complete, then read() the driver versions

  let readQueries = [];
  for (let openPromise of openQueries) {
    openPromise.then((response) => {
      logger.debug(`then: Status value from open() is ${response.status}`);
      readQueries.push(dd.read(response.handle,rddCmd.CDR.DriverVersion,256)
      .then((response) => {
        logger.info(`Settled read promise value (fulfill): ${new rddCmd.SemVer(response.datablock).toString()}`);
        return response;
      },(response) => {
        logger.error(`Settled read promise value (reject): ${response.status}`);
        return response;
      }))},(response) => {
        logger.error(`catch: Error value from open() is ${response.status}`);
      })
  }
  logger.info("Main program starter processing completed.");
});
