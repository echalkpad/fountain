// This module defines a Johnny-Five Controller object for use with the
// J-5 Servo Component and a DDServo device driver on an Arduino.
//
// This program is strict-mode throughout.
//
// Doug Johnson, April 2016

const log4js = require("log4js");
const five = require("johnny-five");

const RDD = require("../lib/RemoteDeviceDriver");
const rddErr = require("../lib/RDDStatus");
const rddCmd = require("../lib/RDDCommand");

let logger = log4js.getLogger("RDDServo");

/**
 * Create an RDDServo Controller object for use with a Servo Component.
 */
let Controller = {
  initialize: {
    value: function(opts) {
      this.openFlags = opts.flags || 1;
      this.board = opts.board || five.Board.mount();
      this.unit = opts.unit || "Servo:0";
      this.dd = new RDD.RemoteDeviceDriver({board: this.board, skipCapabilities: false});
      let openEvent = this.dd.open(this.unit,this.openFlags);
      logger.trace(`Open response event to wait for: ${openEvent}`);
      this.dd.once(openEvent, (response) => {
        logger.trace(`${openEvent} handler invoked.`);
        if (response.status >= 0) {
          logger.debug(`Status value from open() is ${response.status}`);
          this.handle = response.status;
          let readEvent = this.dd.read(this.handle,rddCmd.CDR.DriverVersion,256);
          logger.trace(`Read response event to wait for: ${readEvent}`);
          this.dd.once(readEvent, (response) => {
            logger.trace(`${readEvent} handler invoked.`);
            if (response.status >= 0) {
              logger.debug(`Status value from read() is ${response.status}`);
              let sv = new rddCmd.SemVer(response.datablock);
              logger.info(`DeviceDriver '${sv.toString()}' is open on logical unit '${this.unit}' with handle ${this.handle}`);
            } else {
              logger.error(`Error value from read() is ${response.status}`);
            }
          });
        } else {
          logger.error(`Error value from open() is ${response.status}`);
        }
      });
    }
  },

  servoWrite: {
    value: function(pin, degrees) {
      // // Servo is restricted to integers
      // degrees |= 0;

      // // If same degrees return immediately.
      // if (this.last && this.last.degrees === degrees) {
      //   return this;
      // }
      // this.io.servoWrite(this.pin, degrees);
    }
  }
};

module.exports = {Controller};
