// This module defines a johnny-five Controller object for use with the
// J-5 Thermometer Component and a DDMCP9808 temperature sensor device
// driver on an Arduino.
//
// This program is strict-mode throughout.
//
// Doug Johnson, April 2016

const log4js = require("/users/finson/repos/log4js-node/lib/log4js");
const five = require("/users/finson/repos/johnny-five/lib/johnny-five");

const RDD = require("../RemoteDeviceDriver");
const rddErr = require("../RDDStatus");
const rddCmd = require("../RDDCommand");

let logger = log4js.getLogger("MCP9808_RDD");

/**
 * Create an MCP9808_RDD Controller object for use with a Thermometer Component.
 *
 * @param custom.unit Logical unit name and number.  default "MCP9808:0"
 * @param custom.flags flags for device open.  default ForceOpen
 * @param freq Update period in milliseconds.  default : 25
 * @param board the Board object to use.  default five.Board.mount()
 * @param address I2E device address.  default 0x18
 * @param
 */
let MCP9808_RDD = {

  initialize: {
    value: function(opts, dataHandler) {
      var address = opts.address || 0x18;
      opts.address = address;

      // Can an externally defined Controller get at the state Map
      // defined in the associated Component?
      // let state = five.Servo.priv.get(this);
      // I'll use a single property 'rdd' instead ...
      this.rdd = {};

      let reg = {
        RESERVED: 0,
        CONFIG: 1,
        UPPER_TEMP: 2,
        LOWER_TEMP: 3,
        CRIT_TEMP: 4,
        AMBIENT_TEMP: 5,
        MANUF_ID: 6,
        DEVICE_ID: 7,
        RESOLUTION: 8
      };

      this.rdd.reg = reg;

      this.rdd.openFlags = opts.custom.flags || 1;
      this.rdd.unit = opts.custom.unit || "TempSensor:0";
      this.rdd.board = opts.board || five.Board.mount();

      let dd =  new RDD.RemoteDeviceDriver({board: this.rdd.board, skipCapabilities: false});
      this.rdd.dd = dd;
      this.rdd.handle = 0;
      dd.open(this.rdd.unit,this.rdd.openFlags,(response) => {
        logger.trace(`Callback openCB invoked.`);
        logger.trace(`Property keys of 'this' are ${Object.keys(this)}.`);
        logger.trace(`Property keys of 'this.rdd' are ${Object.keys(this.rdd)}.`);
        if (response.status >= 0) {
          logger.debug(`Status value from open() is ${response.status}`);
          this.rdd.handle = response.status;
          dd.read(this.rdd.handle,rddCmd.CDR.DriverVersion,256,(response) => {
            logger.trace(`readCB callback invoked.`);
            if (response.status >= 0) {
              logger.debug(`Status value from read() is ${response.status}`);
              this.rdd.sv = new rddCmd.SemVer(response.datablock);
              logger.info(`DeviceDriver '${this.rdd.sv.toString()}' is open on logical unit '${this.rdd.unit}' with handle ${this.rdd.handle}`);
------------------
              dd.write(this.rdd.handle,reg.PIN,2,[this.pin,0],(response) => {
                logger.trace(`writeCB callback invoked after setting pin = ${this.pin}.`);
                if (response.status >= 0) {
                  logger.debug(`Status value from write() is ${response.status}`);
                  logger.info(`Logical unit '${this.rdd.unit}' (handle ${this.rdd.handle}) is attached to pin ${this.pin}.`);
                } else {
                  logger.error(`Error value from write() is ${response.status}`);
                }
              });
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
  toCelsius: {
    value: function(raw) {
      return raw;
    }
  }
};

module.exports = {MCP9808_RDD};
