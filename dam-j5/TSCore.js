/**
 * This test script started life as blink.js, a sample script to blink LED 13, but
 * has since grown into a more substantial program.
 */

console.log("TestSuite Core starting ...");

/**
 * Module dependencies
 */

var firmata = require("firmata");
var dd = require("DeviceDriver");

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

// When the board is ready, start blinking the LED

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

// Check core ability to respond

// direct

// expectedResponse.put(SystemReset.class, null); // (C, not in doc) reset from MIDI (FirmataCore)
// expectedResponse.put(ReportVersion.class, ReportVersion.class); // (Q/R) report protocol version (FirmataCore)

// expectedResponse.put(SetPinMode.class, null); // (C) set a pin to INPUT/OUTPUT/PWM/etc (FirmataCore, then FirmataExt)),
// expectedResponse.put(SetPinValue.class, null); // (C) set a digital pin to LOW or HIGH

// expectedResponse.put(DigitalMsg.class, null); // (C) send data for a digital port (DigitalOutputFeature)
// expectedResponse.put(AnalogMsg.class, null); // (C) send data for an analog pin (or PWM) (AnalogOutputFeature)
// expectedResponse.put(ReportAnalogMsg.class, AnalogMsg.class); // (C/R) enable analog input by pin # (AnalogInputFeature)
// expectedResponse.put(ReportDigitalMsg.class, DigitalMsg.class); // (C/R) enable digital input by port pair (DigitalInputFirmata)


function TestSuite(opts) {

  opts = opts || {};

  }

TestSuite.prototype.testReportVersion = function() {
  console.info("test: reportVersion");
  board.reportVersion(function() {
    console.info("version reported");
  });
};

TestSuite.prototype.testQueryFirmware = function() {
  console.info("test: queryFirmware");
  board.queryFirmware(function() {
    console.info("firmware version reported");
  });
};

// TestSuite.prototype.testAnalogRead = function(pin) {
//   console.info("test: analogRead");
//   board.analogRead(pin, function() {
//     console.info("analogRead complete");
//   });
// };

TestSuite.prototype.testAnalogWrite = function(pin,value) {
  console.info("test: analogWrite");
  board.analogWrite(pin,value);
};

TestSuite.prototype.testQueryAnalogMapping = function() {
  console.info("test: queryAnalogMapping");
  board.queryAnalogMapping(function() {
    console.info("analog mapping reported");
  });
};

TestSuite.prototype.testCustomSysex = function() {
  console.info("test: custom Sysex");
  var msg = [ START_SYSEX,DD.DEVICE_QUERY];

  board.queryAnalogMapping(function() {
    console.info("analog mapping reported");
  });
};

board.on("blinking", function () {
  var suite = new TestSuite();
  suite.testReportVersion();
  suite.testQueryFirmware();
  // suite.testAnalogRead(12);
  suite.testAnalogWrite(7,1);
  suite.testQueryAnalogMapping();
});
