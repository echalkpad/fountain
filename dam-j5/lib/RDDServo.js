// This module defines a Johnny-Five Controller object for use with the
// J-5 Servo Component and a DDServo device driver on an Arduino.
//
// This program is strict-mode throughout.
//
// Doug Johnson, April 2016

const log4js = require("log4js");
const five = require("johnny-five");

const RDD = require("./RemoteDeviceDriver");

let logger = log4js.getLogger();

/**
 * Create an RDDServo controller object for use with a Servo Component.
 */
const Controller = {
  initialize: {
    value: function(opts) {
      const dd = new RDD.RemoteDeviceDriver({"board": this, skipCapabilities: false});
      if (Array.isArray(opts.pwmRange)) {
        this.io.servoConfig(this.pin, opts.pwmRange[0], opts.pwmRange[1]);
      } else {
        this.io.pinMode(this.pin, this.mode);
      }
    }
  },

  servoWrite: {
    writable : true,
    value: function(pin, degrees) {
      // Servo is restricted to integers
      degrees |= 0;

      // If same degrees return immediately.
      if (this.last && this.last.degrees === degrees) {
        return this;
      }
      this.io.servoWrite(this.pin, degrees);
    }
  }
};

module.exports = {Controller};
