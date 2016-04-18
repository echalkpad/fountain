const log4js = require("log4js");
const logger = log4js.getLogger("RDDCommand");
logger.setLevel('DEBUG');

  /**
   * These Firmata Sysex command codes identify the DeviceDriver messages.
   */
  let SYSEX = {
      'DEVICE_QUERY' : 0x30,
      'DEVICE_RESPONSE' :0x31
    };

  /**
   * These action codes indicate the type of DeviceDriver query we are making
   */
  let ACTION = {
      'OPEN' : 0,
      'READ' : 1,
      'WRITE' : 2,
      'CLOSE' : 3
    };

    /**
     * These register names and codes indicate the specific item of interest
     * during DeviceDriver reads and writes.
     */
    let CDR = {
        'DriverVersion' : -2,
        'Intervals' : -6
    };

  /**
   * Define the messge offsets common to all of the DEVICE_QUERY and
   * DEVICE_RESPONSE messages.
   */
  let MO = {
    ACTION : 0,
    FLAGS : 1,
    HANDLE : 1,
    REGISTER : 3,
    REQUESTED_COUNT : 5,
    STATUS : 7,
    DATA : 9
  };

  //--------------------

/**
 * This class represents a Firmata Device Query Open message created by the
 * local client prior to sending it to a remote device driver.
 */
class DeviceQueryOpen {

  constructor(unitName,flags) {
    this.action = ACTION.OPEN;
    this.flags = flags;
    this.register = 0;
    this.requestedByteCount = 0;
    this.unitName = unitName;
  }

  toByteArray() {
    let msgBody = new Buffer(256);
    msgBody.writeUInt8(this.action,MO.ACTION);
    msgBody.writeUInt16LE(this.flags,MO.FLAGS);
    msgBody.writeInt16LE(0,MO.REGISTER);
    msgBody.writeUInt16LE(0, MO.REQUESTED_COUNT);
    msgBody.writeUInt16LE(0,MO.STATUS);
    msgBody.write(this.unitName,MO.DATA,'utf8');
    msgBody.writeUInt8(0,MO.DATA+this.unitName.length);

    let s = msgBody.toString("base64",0,MO.DATA+this.unitName.length);
    let encodedMsgBody = Uint8Array.from(s, x => x.charCodeAt(0));
    let msgArray =  [SYSEX.DEVICE_QUERY, ...encodedMsgBody];

    return msgArray;
  }
}

/**
 * This class represents a Firmata Device Response Open message received from
 * a remote device driver after a call to open(unitName,flags).
 */
class DeviceResponseOpen {

  constructor(msgBody) {
    this.action = msgBody.readUInt8(MO.ACTION);
    this.flags = msgBody.readUInt16LE(MO.FLAGS);
    this.register = msgBody.readUInt16LE(MO.REGISTER);
    this.requestedByteCount = msgBody.readUInt16LE(MO.REQUESTED_COUNT);
    this.status = msgBody.readInt16LE(MO.STATUS);
    if (this.status >= 0) {
        this.handle = this.status;
    }

    if (msgBody.length > MO.DATA) {
      let datablock = msgBody.slice(MO.DATA,msgBody.length-1);
      this.unitName = datablock.toString("utf8");
    }
  }
}

  //--------------------

/**
 * This class represents a Firmata Device Query Read message created by the
 * local client prior to sending it to a remote device driver.
 */
class DeviceQueryRead {

  constructor(handle, reg, count) {
    this.action = ACTION.READ;
    this.handle = handle;
    this.register = reg;
    this.requestedByteCount = count;
  }

  toByteArray() {
    let msgBody = new Buffer(256);
    msgBody.writeUInt8(this.action,MO.ACTION);
    msgBody.writeUInt16LE(this.handle,MO.HANDLE);
    msgBody.writeInt16LE(this.register,MO.REGISTER);
    msgBody.writeUInt16LE(this.requestedByteCount, MO.REQUESTED_COUNT);
    msgBody.writeUInt16LE(0,MO.STATUS);

    let s = msgBody.toString("base64",0,MO.DATA);
    let encodedMsgBody = Uint8Array.from(s, x => x.charCodeAt(0));
    let msgArray =  [SYSEX.DEVICE_QUERY, ...encodedMsgBody];

    return msgArray;
  }
}

/**
 * This class represents a Firmata Device Response Read message received from
 * a remote device driver after a call to read(handle, reg, count).
 */
class DeviceResponseRead {

  constructor(msgBody) {
    this.action = msgBody.readUInt8(MO.ACTION);
    this.handle = msgBody.readUInt16LE(MO.HANDLE);
    this.register = msgBody.readInt16LE(MO.REGISTER);
    this.requestedByteCount = msgBody.readUInt16LE(MO.REQUESTED_COUNT);
    this.status = msgBody.readInt16LE(MO.STATUS);
    if (this.status >= 0) {
        this.actualByteCount = this.status;
    } else {
        this.actualByteCount = 0;
    }

    if (msgBody.length > MO.DATA) {
        this.datablock = Buffer.from(msgBody.slice(MO.DATA,msgBody.length));
    } else {
        this.datablock = null;
    }
  }
}

  //--------------------

/**
 * This class represents a Firmata Device Query Write message created by the
 * local client prior to sending it to a remote device driver.
 */
class DeviceQueryWrite {

  constructor(handle, reg, count,buf) {
    this.action = ACTION.WRITE;
    this.handle = handle;
    this.register = reg;
    this.requestedByteCount = count;
    this.datablock = Buffer.from(buf);
  }

  toByteArray() {
    let msgBody = new Buffer(256);
    msgBody.writeUInt8(this.action,MO.ACTION);
    msgBody.writeUInt16LE(this.handle,MO.HANDLE);
    msgBody.writeInt16LE(this.register,MO.REGISTER);
    msgBody.writeUInt16LE(this.requestedByteCount, MO.REQUESTED_COUNT);
    msgBody.writeUInt16LE(0,MO.STATUS);
    this.datablock.copy(msgBody,MO.DATA);

    let s = msgBody.toString("base64",0,MO.DATA+this.requestedByteCount);
    let encodedMsgBody = Uint8Array.from(s, x => x.charCodeAt(0));
    let msgArray =  [SYSEX.DEVICE_QUERY, ...encodedMsgBody];

    return msgArray;
  }
}

/**
 * This class represents a Firmata Device Response Write message received from
 * a remote device driver after a call to read(handle, reg, count).
 */
class DeviceResponseWrite {

  constructor(msgBody) {
    this.action = msgBody.readUInt8(MO.ACTION);
    this.handle = msgBody.readUInt16LE(MO.HANDLE);
    this.register = msgBody.readInt16LE(MO.REGISTER);
    this.requestedByteCount = msgBody.readUInt16LE(MO.REQUESTED_COUNT);
    this.status = msgBody.readInt16LE(MO.STATUS);
    if (this.status >= 0) {
        this.actualByteCount = this.status;
    } else {
        this.actualByteCount = 0;
    }
  }
}

  //--------------------

/**
 * This class represents a Firmata Device Query Close message created by the
 * local client prior to sending it to a remote device driver.
 */
class DeviceQueryClose {

  constructor(handle) {
    this.action = ACTION.CLOSE;
    this.handle = handle;
  }

  toByteArray() {
    let msgBody = new Buffer(MO.DATA);
    msgBody.writeUInt8(this.action,MO.ACTION);
    msgBody.writeUInt16LE(this.handle,MO.HANDLE);
    msgBody.writeInt16LE(0,MO.REGISTER);
    msgBody.writeUInt16LE(0,MO.REQUESTED_COUNT);
    msgBody.writeUInt16LE(0,MO.STATUS);

    let s = msgBody.toString("base64",0,MO.DATA);
    let encodedMsgBody = Uint8Array.from(s, x => x.charCodeAt(0));
    let msgArray =  [SYSEX.DEVICE_QUERY, ...encodedMsgBody];

    return msgArray;
  }
}

/**
 * This class represents a Firmata Device Response Close message received from
 * the remote device driver after a call to close(handle).
 */
class DeviceResponseClose {

  constructor(msgBody) {
    this.action = msgBody.readUInt8(MO.ACTION);
    this.handle = msgBody.readUInt16LE(MO.HANDLE);
    this.status = msgBody.readInt16LE(MO.STATUS);
  }
}

  //--------------------

    class SemVer {
      /**
       * Build a SemVer object based on the byte buffer presented.
       * @param  buf integer array of values containing version numbers and labels
       *
       */
      constructor(buf) {
        if (buf.length < 6) {
            this.itemName = "FormatError";
            this.version = [0,0,0];
            this.preReleaseLabel = "FormatError";
            this.buildLabel = "FormatError";
            return;
        }

        let offset = 0;
        let byteIndex = 0;

        // name

        while (buf[byteIndex++] !== 0) {}
        logger.trace(`offset: ${offset}, count: ${byteIndex-offset}`);

        let intElements = buf.slice(offset, byteIndex-1);
        this.itemName = intElements.toString("utf8");

        // version numbers

        let count = buf[byteIndex++];
        offset = byteIndex;
        byteIndex += count;
        logger.trace(`offset: ${offset}, count: ${byteIndex-offset}`);
        this.version = [];
        for (let idx = 0; idx < count; idx++) {
            this.version[idx] = buf[offset+idx] & 0xFF;
        }

        // prerelease label

        offset = byteIndex;
        while (buf[byteIndex++] !== 0) {}
        logger.trace(`offset: ${offset}, count: ${byteIndex-offset}`);

        intElements = buf.slice(offset, byteIndex-1);
        this.preReleaseLabel = intElements.toString("utf8");

        // build label

        offset = byteIndex;
        while (buf[byteIndex++] !== 0) {}
        logger.trace(`offset: ${offset}, count: ${byteIndex-offset}`);

        intElements = buf.slice(offset, byteIndex-1);
        this.buildLabel = intElements.toString("utf8");
      }

      toString() {
        let result = `${this.itemName} ${this.version[0]}.${this.version[1]}.${this.version[2]}`;
        if (this.preReleaseLabel.length > 0) {
          result += `-${this.preReleaseLabel}`;
        }
        if (this.buildLabel.length > 0) {
          result += `+${this.buildLabel}`;
        }
        return result;
    }
  }

  //--------------------

module.exports = {SYSEX, ACTION, MO, CDR,
    SemVer,
    DeviceQueryOpen, DeviceResponseOpen,
    DeviceQueryRead, DeviceResponseRead,
    DeviceQueryWrite, DeviceResponseWrite,
    DeviceQueryClose, DeviceResponseClose
};
