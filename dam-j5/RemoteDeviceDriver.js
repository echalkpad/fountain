// This module provides a Johnny-Five accessible interface to DeviceDrivers
// running on a remote Arduino host.  The transport protocol is StandardFirmata
// with the addition of DEVICE_QUERY and DEVICE_RESPONSE.
//
// This module is strict-mode throughout, and it uses some ES6 features.
//
// Doug Johnson, April 2016

const log4js = require("log4js");
const firmata = require("firmata");
const EventEmitter = require("events");

const rddCmd = require("./RDDCommand");
const rddErr = require("./RDDStatus");

let logger = log4js.getLogger("RDD ");

/**
 * Define the event handlers needed for the Firmata Remote Device Driver
 * implementation in Javascript.
 */
class RemoteDeviceDriver extends EventEmitter {

  constructor(opts) {
    super(opts);
    logger.trace("RemoteDeviceDriver constructor",Object.keys(opts));
    if ("board" in opts) {
      this.board = opts.board;
    } else {
      throw new Error("A 'board' property must be specified in the RemoteDeviceDriver constructor options.");
    }

    this.responseEvents = new Map();
    this.responseEvents.set(rddCmd.ACTION.OPEN, "DeviceResponseOpen");
    this.responseEvents.set(rddCmd.ACTION.READ, "DeviceResponseRead");
    this.responseEvents.set(rddCmd.ACTION.WRITE, "DeviceResponseWrite");
    this.responseEvents.set(rddCmd.ACTION.CLOSE, "DeviceResponseClose");

    // Tell Firmata that we will handle all DEVICE_RESPONSE messages that arrive.
    // The message is decoded as much as needed to derive a signature event which
    // is then emitted for further processing.

    this.board.sysexResponse(rddCmd.SYSEX.DEVICE_RESPONSE, (encodedMsgBody) => {
      logger.debug("sysexDeviceResponseHandler invoked");

      let encodedMsgBodyBuffer = Buffer.from(encodedMsgBody);
      logger.trace("encoded Response Body length: ", encodedMsgBodyBuffer.length);
      logger.trace("encoded Response Body (b): ", encodedMsgBodyBuffer);

      let msgBody = Buffer.from(encodedMsgBodyBuffer.toString(),"base64");
      logger.trace("decoded Response Body length: ", msgBody.length);
      logger.trace("decoded Response Body (b): ", msgBody);

      let action = msgBody.readUInt8(0);
      logger.trace(`Rcvd DeviceResponse: action code: ${action}`);

      let response;
      let eventName = "";

      switch (action) {
        case rddCmd.ACTION.OPEN:
          response = new rddCmd.DeviceResponseOpen(msgBody);
          eventName = this.responseEvents.get(rddCmd.ACTION.OPEN)+`-${response.unitName}`;
          break;

        case rddCmd.ACTION.READ:
          response = new rddCmd.DeviceResponseRead(msgBody);
          eventName = this.responseEvents.get(rddCmd.ACTION.READ)+`-${response.handle}-${response.register}`;
          break;

        case rddCmd.ACTION.WRITE:
          response = new rddCmd.DeviceResponseWrite(msgBody);
          eventName = this.responseEvents.get(rddCmd.ACTION.WRITE)+`-${response.handle}-${response.register}`;
          break;

        case rddCmd.ACTION.CLOSE:
          response = new rddCmd.DeviceResponseClose(msgBody);
          eventName = this.responseEvents.get(rddCmd.ACTION.CLOSE)+`-${response.handle}`;
          break;
      }

      if (eventName.length !== 0) {
        logger.debug(`Response event to emit: ${eventName}`);
        this.emit(eventName, response);
      } else {
        let errString = `Invalid Device Response Action received from remote device: ${action}`;
        logger.error(errString);
        this.board.emit("string",errString);
      }
    });
  }

  //--------------------------------------------------------

/**
 * open() uses Promise handling to simplify staying coordinated with the
 * asynchronous return of responses to DEVICE_QUERY messsages.
 */
 open(unitName, flags) {
    logger.trace("RemoteDeviceDriver open() started: ",unitName);

    // Send the OPEN message

    let message = new rddCmd.DeviceQueryOpen(unitName,flags);
    this.board.sysexCommand(message.toByteArray());

    // Create a promise callback that will be fulfilled when our OPEN RESPONSE
    // is received.

    let eventName = this.responseEvents.get(rddCmd.ACTION.OPEN)+`-${unitName}`;
    logger.trace(`Response event to wait for: ${eventName}`);
    let p = new Promise((fulfill, reject) => {
      logger.trace("Promise initialization method is started for OPEN.");
      this.once(eventName, (response) => {
        logger.debug(`${eventName} handler invoked. status: ${response.status}, unitName: ${response.unitName}`);
        if (response.status >= 0) {
          fulfill(response);
        } else {
          reject(response);
        }
      });
      logger.trace("Promise initialization method is complete.");
    });

    logger.trace("RemoteDeviceDriver open() finished.");
    return p;
  }

  //--------------------------------------------------------

/**
 * read() uses Promise handling to simplify staying coordinated with the
 * asynchronous return of responses to DEVICE_QUERY messsages.
 */
  read(handle, reg, count) {
    logger.trace(`RemoteDeviceDriver read(${handle}, ${reg}, ${count}) started`);

    // Send the READ message

    let message = new rddCmd.DeviceQueryRead(handle, reg, count);
    this.board.sysexCommand(message.toByteArray());

    // Create a promise callback that will be fulfilled when our READ RESPONSE
    // is received.

    let eventName = this.responseEvents.get(rddCmd.ACTION.READ)+`-${handle}-${reg}`;
    logger.trace(`Response event to wait for: ${eventName}`);
    let p = new Promise((fulfill, reject) => {
      logger.trace("Promise initialization method is started for READ.");
      this.once(eventName, (response) => {
        logger.trace(`${eventName} handler invoked.`);
        if (response.status >= 0) {
          fulfill(response);
        } else {
          reject(response);
        }
      });
      logger.trace("Promise initialization method is complete.");
    })
    logger.trace("RemoteDeviceDriver read() finished.");
    return p;
  }

  //--------------------------------------------------------

/**
 * write() uses Promise handling to simplify staying coordinated with the
 * asynchronous return of responses to DEVICE_QUERY messsages.
 */
  write(handle, reg, count,buf) {
    logger.info(`RemoteDeviceDriver write(${handle}, ${reg}, ${count}, ${buf}) started`);

    // Send the Write message

    let message = new rddCmd.DeviceQueryWrite(handle, reg, count,buf);
    this.board.sysexCommand(message.toByteArray());

    // Create a promise callback that will be fulfilled when our WRITE RESPONSE
    // is received.

    let eventName = this.responseEvents.get(rddCmd.ACTION.WRITE)+`-${handle}-${reg}`;
    logger.trace(`Response event to wait for: ${eventName}`);
    let p = new Promise((fulfill, reject) => {
      logger.trace("Promise initialization method is started for WRITE.");
      this.once(eventName, (response) => {
        logger.trace(`${eventName} handler invoked.`);
        if (response.status >= 0) {
          fulfill(response);
        } else {
          reject(response);
        }
      });
      logger.trace("Promise initialization method is complete.");
    })
  .then((response) => {
      logger.trace(`then: Status value from write() is ${response.status}`);
      return response;
    })
    .catch((response) => {
      logger.trace(`catch: Error value from write() is ${response.status}`);
      return status;
    });
    logger.trace("RemoteDeviceDriver write() finished.");
    return p;
  }

  //--------------------------------------------------------

/**
 * close() uses Promise handling to simplify staying coordinated with the
 * asynchronous return of responses to DEVICE_QUERY messsages.
 */
  close(handle) {
    logger.trace(`RemoteDeviceDriver close(${handle}) started`);

    // Send the Close message

    let message = new rddCmd.DeviceQueryClose(handle);
    this.board.sysexCommand(message.toByteArray());

    // Create a promise callback that will be fulfilled when our CLOSE RESPONSE
    // is received.

    let eventName = this.responseEvents.get(rddCmd.ACTION.CLOSE)+`-${handle}`;
    logger.trace(`Response event to wait for: ${eventName}`);
    let p = new Promise((fulfill, reject) => {
      logger.trace("Promise initialization method is started for CLOSE.");
      this.once(eventName, (response) => {
        logger.trace(`${eventName} handler invoked.`);
        if (response.status >= 0) {
          fulfill(response);
        } else {
          reject(response);
        }
      });
      logger.trace("Promise initialization method is complete.");
    })
  .then((response) => {
      logger.trace(`then: Status value from close() is ${response.status}`);
      return response;
    })
    .catch((response) => {
      logger.trace(`catch: Error value close write() is ${response.status}`);
      return status;
    });
    logger.trace("RemoteDeviceDriver close() finished.");
    return p;
  }
}

module.exports = RemoteDeviceDriver;
