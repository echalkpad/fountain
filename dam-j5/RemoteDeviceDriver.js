// This module provides a Johnny-Five accessible interface to DeviceDrivers
// running on a remote Arduino host.  The transport protocol is StandardFirmata
// with the addition of DEVICE_QUERY and DEVICE_RESPONSE.
//
// This module is strict-mode throughout, and it uses some ES6 features.
//
// Doug Johnson, April 2016

const EventEmitter = require('events');
const firmata = require('firmata');

const rddCmd = require('./RDDCommand');
const rddErr = require('./RDDStatus');

/**
 * Define the event handlers needed for the Firmata Remote Device Driver
 * implementation in Javascript.
 */
class RemoteDeviceDriver extends EventEmitter {

  constructor(opts) {
    super(opts);
    console.log("RemoteDeviceDriver constructor",Object.keys(opts));
    if ('board' in opts) {
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
      console.log("\nsysexDeviceResponseHandler invoked");

      let encodedMsgBodyBuffer = Buffer.from(encodedMsgBody);
      console.log("encoded Response Body length: ", encodedMsgBodyBuffer.length);
      console.log("encoded Response Body (b): ", encodedMsgBodyBuffer);

      let msgBody = Buffer.from(encodedMsgBodyBuffer.toString(),'base64');
      console.log("decoded Response Body length: ", msgBody.length);
      console.log("decoded Response Body (b): ", msgBody);

      let action = msgBody.readUInt8(0);
      console.log(`Rcvd DeviceResponse: action code: ${action}`);

      let response;
      let eventName = "";

      switch (action) {
        case rddCmd.ACTION.OPEN:
          response = new rddCmd.DeviceResponseOpen(msgBody);
          eventName = this.responseEvents.get(rddCmd.ACTION.OPEN)+'-'+response.unitName;
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
        console.log(`Response event to emit: ${eventName}`);
        this.emit(eventName, response);
      } else {
        let errString = `Invalid Device Response Action received from remote device: ${action}`;
        console.log(errString);
        this.board.emit('string',errString);
      }
    });
  }

  //--------------------------------------------------------

/**
 * open() uses Promise handling to simplify staying coordinated with the
 * asynchronous return of responses to DEVICE_QUERY messsages.
 */
 open(unitName, flags) {
    console.info("\nRemoteDeviceDriver open() started: ",unitName);

    // Send the OPEN message

    let message = new rddCmd.DeviceQueryOpen(unitName,flags);
    this.board.sysexCommand(message.toByteArray());

    // Create a promise callback that will be fulfilled when our OPEN RESPONSE
    // is received.

    let p = new Promise((fulfill, reject) => {
      console.log("Promise initialization method is started for OPEN.");
      this.once(`DeviceResponseOpen-${unitName}`, (response) => {
        console.log(`DeviceResponseOpen-${unitName} handler invoked`);
        console.log(`DeviceResponseOpen status: ${response.status}`);
        if (response.status >= 0) {
          console.log(`DeviceResponse Open handle: ${response.handle}, unitName: ${response.unitName}`);
          fulfill(response);
        } else {
          reject(response);
        }
      });
      console.log("Promise initialization method is complete.");
    })
  .then((response) => {
      console.log(`then: Status value in open() is ${response.status}`);
      return response;
    })
    .catch((response) => {
      console.log(`catch: Error value in open() is ${response.status}`);
      return response;
    });
    console.info("RemoteDeviceDriver open() finished.");
    return p;
  }

  //--------------------------------------------------------

/**
 * read() uses Promise handling to simplify staying coordinated with the
 * asynchronous return of responses to DEVICE_QUERY messsages.
 */
  read(handle, reg, count) {
    console.info(`\nRemoteDeviceDriver read(${handle}, ${reg}, ${count}) started`);

    // Send the READ message

    let message = new rddCmd.DeviceQueryRead(handle, reg, count);
    this.board.sysexCommand(message.toByteArray());

    // Create a promise callback that will be fulfilled when our READ RESPONSE
    // is received.

    let eventName = this.responseEvents.get(rddCmd.ACTION.READ)+`-${handle}-${reg}`;
    console.log(`Response event to wait for: ${eventName}`);
    let p = new Promise((fulfill, reject) => {
      console.log("Promise initialization method is started for READ.");
      this.once(eventName, (response) => {
        console.log(`${eventName} handler invoked.`);
        if (response.status >= 0) {
          fulfill(response);
        } else {
          reject(response);
        }
      });
      console.log("Promise initialization method is complete.");
    })
  .then((response) => {
      console.log(`then: Status value from read() is ${response.status}`);
      console.log(`datablock from read() is ${response}`);
      return response;
    })
    .catch((response) => {
      console.log(`catch: Error value from read() is ${response.status}`);
      return status;
    });
    console.info("RemoteDeviceDriver read() finished.");
    return p;
  }

  //--------------------------------------------------------

/**
 * write() uses Promise handling to simplify staying coordinated with the
 * asynchronous return of responses to DEVICE_QUERY messsages.
 */
  write(handle, reg, count,buf) {
    console.info(`\nRemoteDeviceDriver write(${handle}, ${reg}, ${count}, ${buf}) started`);

    // Send the Write message

    let message = new rddCmd.DeviceQueryWrite(handle, reg, count,buf);
    this.board.sysexCommand(message.toByteArray());

    // Create a promise callback that will be fulfilled when our WRITE RESPONSE
    // is received.

    let eventName = this.responseEvents.get(rddCmd.ACTION.WRITE)+`-${handle}-${reg}`;
    console.log(`Response event to wait for: ${eventName}`);
    let p = new Promise((fulfill, reject) => {
      console.log("Promise initialization method is started for WRITE.");
      this.once(eventName, (response) => {
        console.log(`${eventName} handler invoked.`);
        if (response.status >= 0) {
          fulfill(response);
        } else {
          reject(response);
        }
      });
      console.log("Promise initialization method is complete.");
    })
  .then((response) => {
      console.log(`then: Status value from write() is ${response.status}`);
      return response;
    })
    .catch((response) => {
      console.log(`catch: Error value from write() is ${response.status}`);
      return status;
    });
    console.info("RemoteDeviceDriver write() finished.");
    return p;
  }

  //--------------------------------------------------------

/**
 * close() uses Promise handling to simplify staying coordinated with the
 * asynchronous return of responses to DEVICE_QUERY messsages.
 */
  close(handle) {
    console.info(`\nRemoteDeviceDriver close(${handle}) started`);

    // Send the Close message

    let message = new rddCmd.DeviceQueryClose(handle);
    this.board.sysexCommand(message.toByteArray());

    // Create a promise callback that will be fulfilled when our CLOSE RESPONSE
    // is received.

    let eventName = this.responseEvents.get(rddCmd.ACTION.CLOSE)+`-${handle}`;
    console.log(`Response event to wait for: ${eventName}`);
    let p = new Promise((fulfill, reject) => {
      console.log("Promise initialization method is started for CLOSE.");
      this.once(eventName, (response) => {
        console.log(`${eventName} handler invoked.`);
        if (response.status >= 0) {
          fulfill(response);
        } else {
          reject(response);
        }
      });
      console.log("Promise initialization method is complete.");
    })
  .then((response) => {
      console.log(`then: Status value from close() is ${response.status}`);
      return response;
    })
    .catch((response) => {
      console.log(`catch: Error value close write() is ${response.status}`);
      return status;
    });
    console.info("RemoteDeviceDriver close() finished.");
    return p;
  }
}

module.exports = RemoteDeviceDriver;
