
  /**
   * The Firmata Sysex command codes identify the DeviceDriver messages.
   */
  let SYSEX = function(key) {
    let obj = {
      'DEVICE_QUERY' : 0x30,
      'DEVICE_RESPONSE' :0x31
    };
    return obj[key];
  };

  /**
   * The action codes indicate the type of DeviceDriver query we are making
   */
  let ACTION = function(key) {
    let obj = {
      'OPEN' : 0,
      'READ' : 1,
      'WRITE' : 2,
      'CLOSE' : 3
    };
    return obj[key];
  };


class DeviceQueryOpen {

  constructor(unitName,flags) {
    this.action = ACTION('OPEN');
    this.flags = flags;
    this.register = 0;
    this.requestedByteCount = 0;
    this.unitName = unitName;
  }

  toByteArray() {
    let msgBody = new Buffer(256);
    let index = 0;
    index = msgBody.writeUInt8(this.action,index);
    index = msgBody.writeUInt16LE(this.flags,index);
    index = msgBody.writeInt16LE(this.register,index);
    index = msgBody.writeUInt16LE(this.requestedByteCount,index);
    index = msgBody.writeUInt16LE(0,index);
    index += msgBody.write(this.unitName,index,'utf8');
    index = msgBody.writeUInt8(0,index);

    let s = msgBody.toString("base64",0,index);
    let encodedMsgBody = Uint8Array.from(s, x => x.charCodeAt(0));
    let msgArray =  [SYSEX('DEVICE_QUERY'), ...encodedMsgBody];

    return msgArray;
  }
}

/**
 * This class represents a Firmata Device Response message created by the remote
 * device driver after a call to open(unitName,flags).  The constructor
 * takes one parameter: the body of the message.  The
 * body is defined in firmata.js as board.currentBuffer.slice(2, -1).  In other
 * words, all bytes in the message except for the beginning START_SYSEX,
 * DEVICE_RESPONSE pair and the closing END_SYSEX.  In the case of
 * DEVICE_RESPONSE messages the body is decoded from base64 by the event
 * listener in RemoteDeviceDriver before being given to us, and it can thus be
 * parsed without any further decoding.
 */
class DeviceResponseOpen {

  constructor(msgBody) {
    this.action = msgBody.readUInt8(0);
    this.flags = msgBody.readUInt16LE(1);
    this.register = msgBody.readUInt16LE(3);
    this.requestedByteCount = msgBody.readUInt16LE(5);
    this.status = msgBody.readInt16LE(7);
    if (this.status >= 0) {
        this.handle = this.status;
    }

    if (msgBody.length > 9) {
      let datablock = msgBody.slice(9,msgBody.length-1);
      this.unitName = datablock.toString("utf8");
    }
  }
}

module.exports = {SYSEX, ACTION, DeviceQueryOpen, DeviceResponseOpen};