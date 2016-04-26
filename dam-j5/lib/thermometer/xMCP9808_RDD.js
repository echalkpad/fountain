// This module defines a johnny-five Controller object for use with the
// J-5 Thermometer Component and a DDMCP9808 temperature sensor device
// driver on an Arduino.
//
// This program is strict-mode throughout.
//
// Doug Johnson, April 2016

const log4js = require("log4js");
const five = require("johnny-five");

const RDD = require("../RemoteDeviceDriver");
const rddErr = require("../RDDStatus");
const rddCmd = require("../RDDCommand");

const path = require("path");
const thisModule = path.basename(module.filename,".js");
const logger = log4js.getLogger(thisModule);
logger.setLevel('TRACE');

/**
 * Create an MCP9808_RDD Controller object for use with a Thermometer Component.
 *
 * @param custom.unit Logical unit name and number.  default "MCP9808:0"
 * @param custom.flags flags for device open.  default ForceOpen
 * @param freq Update period in milliseconds.  default : 25
 * @param board the Board object to use.  default five.Board.mount()
 */
let MCP9808_RDD = {

  initialize: {
    value: function(opts) {

      // Can an externally defined Controller get at the state Map
      // defined in the associated Component?
      // let state = five.Servo.priv.get(this);
      // I'll use a single property 'rdd' and a closure instead ...

      const rdd = {};

      const reg = {
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

      rdd.reg = reg;
      rdd.openFlags = opts.custom.flags || 1;
      rdd.unit = opts.custom.unit || "MCP9808:0";
      rdd.board = opts.board || five.Board.mount();

      rdd.hook = [

      // 0. Open response, read version query

      function (response) {
          logger.trace(`Response hook invoked. (Step 0, open())`);
          if (response.status < 0) {
            throw new Error(`Open error during init (0): ${response.status}.`);
          } else {
            logger.debug(`Status value from open() is ${response.status}`);
            rdd.handle = response.status;
            dd.read(rdd.handle,rddCmd.CDR.DriverVersion,256,rdd.hook[1]);
            return;
          }
        },

        // 1. Read version response,
        // TODO [set interval, start auto read, ...]

      function (response) {
          logger.trace(`Response hook invoked. (Step 1, read())`);
          if (response.status < 0) {
            throw new Error(`Read error during init (1): ${response.status}.`);
          } else {
            logger.debug(`Status value from read() is ${response.status}`);
            rdd.sv = new rddCmd.SemVer(response.datablock);
            logger.info(`DeviceDriver '${rdd.sv.toString()}' is open on logical unit '${rdd.unit}' with handle ${rdd.handle}`);
            return;
          }
        }
      ];

      let dd =  new RDD.RemoteDeviceDriver({board: rdd.board, skipCapabilities: false});
      rdd.dd = dd;
      rdd.handle = 0;
      dd.open(rdd.unit,rdd.openFlags,rdd.hook[0]);
    }
  },
  toCelsius: {
    value: function(raw) {
      return raw;
    }
  }
};

module.exports = {MCP9808_RDD};
