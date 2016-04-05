// This module provides a Johnny-Five accessible interface to DeviceDriver
// running on a remote Arduino host.  The transport protocol is StandardFirmata
// with the addition of DEVICE_QUERY and DEVICE_RESPONSE.
//
// This module is strict-mode throughout, and it uses some ES6 features.
//
// Doug Johnson, April 2016

const EventEmitter = require('events');
const firmata = require('firmata');

const rddSys = require('./RDDSysex');
const rddErr = require('./RDDStatus');

const responseEvents = new Map();
responseEvents.set(rddSys.ACTION('OPEN'), "DeviceResponseOpen");
responseEvents.set(rddSys.ACTION('READ'), "DeviceResponseRead");
responseEvents.set(rddSys.ACTION('WRITE'), "DeviceResponseWrite");
responseEvents.set(rddSys.ACTION('CLOSE'), "DeviceResponseClose");

/**
 * Define the types and methods needed for the Firmata Remote Device Driver
 * implementation in Javascript.
 */
class RemoteDeviceDriver extends EventEmitter {

  constructor(opts) {
    super(opts);
    console.log("RemoteDeviceDriver constructor",Object.keys(opts));
    if ('board' in opts) {
      this.board = opts.board;
    } else {
      throw new Error("A 'board' property must be specified to the RemoteDeviceDriver constructor.");
    }

    this.on('DeviceResponseOpen', (msg) => {
      console.log("openResponseHandler invoked");
      let response = new rddSys.DeviceResponseOpen(msg);
      console.log(`DeviceResponse Open Status: ${response.status}`);
      if (response.status > 0) {
        console.log(`DeviceResponse Open parameters: handle: ${response.handle}, unitName: ${response.unitName}`);
      }
    });

    this.on('DeviceResponseRead',this.readResponseHandler);
    this.on('DeviceResponseWrite',this.writeResponseHandler);
    this.on('DeviceResponseClose',this.closeResponseHandler);

    this.board.sysexResponse(rddSys.SYSEX('DEVICE_RESPONSE'), (encodedMsgBody) => {
      console.log("sysexDeviceResponseHandler invoked");

      let encodedMsgBodyBuffer = Buffer.from(encodedMsgBody);
      console.log("encoded Response Body length: ", encodedMsgBodyBuffer.length);
      console.log("encoded Response Body (b): ", encodedMsgBodyBuffer);

      let msgBody = Buffer.from(encodedMsgBodyBuffer.toString(),'base64');
      console.log("decoded Response Body length: ", msgBody.length);
      console.log("decoded Response Body (b): ", msgBody);

      let action = msgBody.readUInt8(0);
      console.log(`DeviceResponse action: ${action}`);

      if (responseEvents.has(action)) {
        console.log(`Event class to emit: ${responseEvents.get(action)}`);
        this.emit(responseEvents.get(action),msgBody);
      } else {
        console.error(`Unknown Remote Device Driver action code received: ${action}`);
      }
    });
  }


  //--------------------------------------------------------

  open(unitName, flags) {
    console.info("RemoteDeviceDriver open: ",unitName);
    let message = new rddSys.DeviceQueryOpen(unitName,flags);
    this.board.sysexCommand(message.toByteArray());
    return 1;
  }

  //--------------------------------------------------------

  read(handle, reg, count,buf) {
    console.info("RemoteDeviceDriver read: ",reg,count);
    return count;
  }

  readResponseHandler(msg) {
    // let response = new DeviceResponseRead(msg);
    // console.log(`DeviceResponse Read Status: ${response.status}`);
    // if (response.status > 0) {
    //   console.log(`DeviceResponse Read parameters: handle: ${response.handle}, register: ${response.register}`);
    // }
  }

  //--------------------------------------------------------

  write(handle, reg, count, buf) {
    console.info("RemoteDeviceDriver write: ",reg,count);
    return count;
  }

  writeResponseHandler(msg) {
    // let response = new DeviceResponseWrite(msg);
    // console.log(`DeviceResponse Write Status: ${response.status}`);
    // if (response.status > 0) {
    //   console.log(`DeviceResponse Write parameters: handle: ${response.handle}, register: ${response.register}`);
    // }
  }

  //--------------------------------------------------------

  close(handle) {
    console.info("RemoteDeviceDriver close",handle);
    return rddErr(rddErr.STATUS('ESUCCESS'));
  }

  closeResponseHandler(msg) {
    // let response = new DeviceResponseClose(msg);
    // console.log(`DeviceResponse Close Status: ${response.status}`);
    // if (response.status > 0) {
    //   console.log(`DeviceResponse Close parameters: handle: ${response.handle}`);
    // }
  }

  //--------------------------------------------------------


}

  //--------------------------------------------------------

module.exports = RemoteDeviceDriver;
