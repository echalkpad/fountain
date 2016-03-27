/**
 * Sample script to blink LED 13
 */

console.log("TestSuite Core starting ...");

var ledPin = 13;
var ledOn = true;

// Create and initialize a board object

var firmata = require("firmata");
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
  console.info("testReportVersion function");
};

board.on("blinking", function () {
  var suite = new TestSuite();
  suite.testReportVersion();
});

