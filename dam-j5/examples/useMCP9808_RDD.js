// This is the main entry point for a simple demo of Johnny-Five controlling
// a Thermometer using a Remote Device Driver on an Arduino host through Firmata.
//
// This program is strict-mode throughout, and it uses some ES6 features.
//
// Doug Johnson, April 2016

const log4js = require("/users/finson/repos/log4js-node/lib/log4js");
const five = require("/users/finson/repos/johnny-five/lib/johnny-five");
const firmata = require("/users/finson/repos/firmata/lib/firmata");

const RDD = require("./RemoteDeviceDriver");
const rddErr = require("./RDDStatus");
const rddCmd = require("./RDDCommand");

let logger = log4js.getLogger();
logger.info(`----Begin useRDDThermometer.`);

const ledPin = 13;
let ledOn = true;

// Create and initialize a board object

const serialPortName = "COM42";
// const serialPortName = "/dev/cu.usbmodem621";

const board = new firmata.Board(serialPortName, function(err) {
  if (err) {
    logger.error(err);
    return;
  }
  board.pinMode(ledPin, board.MODES.OUTPUT);
});

// When the board is ready, start blinking the LED and then trigger the rest of the program to run

board.on("ready", function() {
  logger.info(`Connected to ${board.firmware.name} -${board.firmware.version.major}.${board.firmware.version.minor}`);
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
  let dd = new RDD({"board": board, skipCapabilities: false});

  let sensor = new five.Thermometer({
    controller: "RDD-MCP9808",
    unit: "Tmp:0"
  });

  sensor.on("data", function() {
    console.log("celsius: %d", this.C);
    console.log("fahrenheit: %d", this.F);
    console.log("kelvin: %d", this.K);
  });
});

// Open the remote device drivers of interest

  let unitNamesOfInterest = ["Meta:0","Hello:0","Foo:0"];

  let openQueries = [];
  for (let unit of unitNamesOfInterest) {
    openQueries.push(dd.open(unit,1));
  }

  logger.trace("Main program device OPEN requests all issued.");

  // Wait for the open() calls to complete, then read() the driver versions

  Promise.all(openQueries)
  .then((openResponses) => {
    let readQueries = [];
    for (let response of openResponses) {
      logger.debug(`Handle from open(${response.unitName}) is ${response.handle}`);
      readQueries.push(dd.read(response.handle,rddCmd.CDR.DriverVersion,256));
    }

    Promise.all(readQueries)
    .then((readResponses) => {
      for (let response of readResponses) {
        logger.info(`Remote Device Driver: ${new rddCmd.SemVer(response.datablock)}`);
      }
    })
    .catch((readResponses) => {
      logger.error(`Returned read promise values (reject):  ${readResponses}`);
    });

   logger.info("Main program starter processing completed.");
  })
  .catch((openResponses) => {
      logger.error(`Returned open promise values (reject):  ${openResponses}`);
  });
});
