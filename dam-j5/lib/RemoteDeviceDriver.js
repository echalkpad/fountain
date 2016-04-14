// This module provides a Johnny-Five accessible interface to DeviceDrivers
// running on a remote Arduino host.  The transport protocol is StandardFirmata
// with the addition of DEVICE_QUERY and DEVICE_RESPONSE.
//
// This module is strict-mode throughout, and it uses some ES6 features.
//
// Doug Johnson, April 2016

const log4js = require("log4js");
const EventEmitter = require("events");

const rddCmd = require("./RDDCommand");
const rddErr = require("./RDDStatus");

const logger = log4js.getLogger("RDD ");
logger.setLevel('DEBUG');

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
 * This open() method sends a DEVICE_QUERY message to the Arduino to request
 * access to the named logical unit.  It returns the name of the event to listen
 * for to its caller, which is then responsible for registering with the RDD
 * object for notification when the DEVICE_RESPONSE message arrives.
 */
 open(unitName, flags) {

    // Send the OPEN message

    let message = new rddCmd.DeviceQueryOpen(unitName,flags);
    this.board.sysexCommand(message.toByteArray());

    // Tell the caller which event will mark the arrival of a response.

    let eventName = this.responseEvents.get(rddCmd.ACTION.OPEN)+`-${unitName}`;
    logger.trace(`Response event to wait for: ${eventName}`);
    return eventName;
  }

  //--------------------------------------------------------

/**
 * This read() method sends a DEVICE_QUERY message to the Arduino to request
 * a read from the device.  It returns the name of the event to listen
 * for to its caller, which is then responsible for registering with the RDD
 * object for notification when the DEVICE_RESPONSE message arrives.
 */
  read(handle, reg, count) {

    let message = new rddCmd.DeviceQueryRead(handle, reg, count);
    this.board.sysexCommand(message.toByteArray());

    let eventName = this.responseEvents.get(rddCmd.ACTION.READ)+`-${handle}-${reg}`;
    logger.trace(`Response event to wait for: ${eventName}`);
    return eventName;
  }

  //--------------------------------------------------------

/**
 * This write() method sends a DEVICE_QUERY message to the Arduino to request
 * a write to the device.  It returns the name of the event to listen
 * for to its caller, which is then responsible for registering with the RDD
 * object for notification when the DEVICE_RESPONSE message arrives.
 */
  write(handle, reg, count,buf) {

    let message = new rddCmd.DeviceQueryWrite(handle, reg, count,buf);
    this.board.sysexCommand(message.toByteArray());

    let eventName = this.responseEvents.get(rddCmd.ACTION.WRITE)+`-${handle}-${reg}`;
    logger.trace(`Response event to wait for: ${eventName}`);
    return eventName;
  }

  //--------------------------------------------------------

/**
 * This close() method sends a DEVICE_QUERY message to the Arduino to release
 * its claim to the open logical unit.  It returns the name of the event to listen
 * for to its caller, which is then responsible for registering with the RDD
 * object for notification when the DEVICE_RESPONSE message arrives.
 */
  close(handle) {

    let message = new rddCmd.DeviceQueryClose(handle);
    this.board.sysexCommand(message.toByteArray());

    let eventName = this.responseEvents.get(rddCmd.ACTION.CLOSE)+`-${handle}`;
    logger.trace(`Response event to wait for: ${eventName}`);
    return eventName;
  }
}

module.exports = {RemoteDeviceDriver};
