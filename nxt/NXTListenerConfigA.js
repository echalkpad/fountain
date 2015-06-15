/* jshint bitwise:false */
'use strict';

// This node.js program receives data from a Lego NXT brick.

var NXTListenerConfig = function (device) {

  this.nxt = device;

  var Tuxi = require('./tuxijs');
  this.tux = new Tuxi();
  this.log = new this.tux.Logger();
  this.logPrefix = "NXTListenerConfig";

  this.nxt.on('getfirmwareversion',this.rGetFirmwareVersion.bind(this));
  this.nxt.on('getdeviceinfo',this.rGetDeviceInfo.bind(this));
  this.nxt.on('getbatterylevel', this.rGetBatteryLevel.bind(this));
  this.nxt.on('setinputmode', this.rSetInputMode.bind(this));
  this.nxt.on('getinputvalue',this.rGetInputValue.bind(this));
  this.nxt.on('playtone', this.rPlaytone.bind(this));
  this.nxt.on('lswrite', this.rLSWrite.bind(this));
  this.nxt.on('lsgetstatus', this.rLSGetStatus.bind(this));

};

NXTListenerConfig.prototype.tux = null;
NXTListenerConfig.prototype.log = null;
NXTListenerConfig.prototype.logPrefix = null;
NXTListenerConfig.prototype.nxt = null;

NXTListenerConfig.prototype.rGetFirmwareVersion = function(data) {
  if (data[1] === this.nxt.EVENTID.getfirmwareversion) {
    if (data[2] !== 0) {
      this.log.warn('status', "Get Firmware Version: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      var protocolMinor = data[3];
      var protocolMajor = data[4];
      var firmwareMinor = data[5];
      var firmwareMajor = data[6];

      this.log.info("event",
        'Firmware Version: ' + firmwareMajor + '.' + firmwareMinor+
        ', Protocol Version: '+protocolMajor + '.' + protocolMinor);
    }
  } else {
    this.log.fatal('*bug*', "The invoked callback function 'rGetFirmwareVersion' does not match Event ID " + data[1] + ".");
  }
};


NXTListenerConfig.prototype.rGetDeviceInfo = function (data) {
  var idx;
  if (data[1] === this.nxt.EVENTID.getdeviceinfo) {
    if (data[2] !== 0) {
      this.log.info("Error. Get Device Info: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      var btAddress = '';
      for (idx = 18; idx <= 23; idx++) {
        btAddress += this.tux.toHex(data[idx]);
        if (idx !== 23) {
          btAddress += ':';
        }
      }
      var rssi = (data[28] << 8) | data[25];
      var flash = (data[32] << 8) | data[29];
      this.log.info("event", "Device Info " + this.nxt.nxt_error_messages[data[2]]);
      this.log.info("detail", "Name: " + data.slice(3, 18));
      this.log.info("detail", "BT address: " + btAddress);
      this.log.debug("detail", "RSSI: " + rssi);
      this.log.debug("detail", "Free User Flash: " + flash);
    }
  } else {
    this.log.fatal('*bug*', "The invoked callback function 'rGetDeviceInfo' does not match Event ID " + data[1] + ".");
  }
};

NXTListenerConfig.prototype.rGetBatteryLevel = function(data) {
  if (data[1] === this.nxt.EVENTID.getbatterylevel) {
    if (data[2] !== 0) {
      this.log.warn('status', "Get Battery Level: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      var bat = (data[4] << 8) | data[3];
      this.log.info("event",'Battery level: ' + bat);
    }
  } else {
    this.log.fatal('*bug*', "The invoked callback function 'rGetBatteryLevel' does not match Event ID " + data[1] + ".");
  }
};

NXTListenerConfig.prototype.rSetInputMode = function (data) {
  if (data[1] === this.nxt.EVENTID.setinputmode) {
    if (data[2] !== 0) {
      this.log.warn('status', "Set Input Mode: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      this.log.info("event",'setinputmode ' + this.nxt.nxt_error_messages[data[2]]);
      if (data.length !== 3) {
        this.log.debug("  Packet length: " + data.length);
        this.log.debug("  Packet:        " + JSON.stringify(data));
      }
    }
  } else {
    this.log.fatal('*bug*', "The invoked callback function 'rSetInputMode' does not match Event ID " + data[1] + ".");
  }
};

NXTListenerConfig.prototype.rGetInputValue = function (data) {
  if (data[1] === this.nxt.EVENTID.getinputvalue) {
    if (data[2] !== 0) {
      this.log.warn('status', "Get Input Value: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      switch (data[3]) {
      case this.nxt.INPUT_PORT_0:
        this.log.info("event","input port " + data[3]);
        break;
      case this.nxt.INPUT_PORT_1:
        this.log.info("event","input port " + data[3]);
        break;
      case this.nxt.INPUT_PORT_2:
        this.log.info("event","input port " + data[3]);
        break;
      case this.nxt.INPUT_PORT_3:
        this.log.info("event","input port " + data[3]);
        break;
      default:
        this.log.warn('status',"Get Input Value: Unexpected input port value: " + data[3]);
      }
    }
  } else {
    this.log.fatal('*bug*', "The invoked callback function 'rGetInputValue' does not match Event ID " + data[1] + ".");
  }
};

NXTListenerConfig.prototype.rPlaytone = function (data) {
  if (data[1] === this.nxt.EVENTID.playtone) {
    if (data[2] !== 0) {
      this.log.warn('status', "Play Tone: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      this.log.info("event","Play Tone complete.");
    }
  } else {
    this.log.fatal('*bug*', "The invoked callback function 'rPlaytone' does not match Event ID " + data[1] + ".");
  }
};

NXTListenerConfig.prototype.rLSWrite = function (data) {
  if (data[1] === this.nxt.EVENTID.lswrite) {
    if (data[2] !== 0) {
      this.log.warn('status', "LS Write: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      this.log.info("event","LS Write complete.");
    }
  } else {
    this.log.fatal('*bug*', "The invoked callback function 'rLSWrite' does not match Event ID " + data[1] + ".");
  }
};

NXTListenerConfig.prototype.rLSGetStatus = function (data) {
  if (data[1] === this.nxt.EVENTID.lsgetstatus) {
    if (data[2] !== 0) {
      this.log.warn('status', "LS Get Status: " + this.nxt.nxt_error_messages[data[2]]);
    } else {
      this.log.info("event","LS Get Status complete.");
    }
  } else {
    this.log.fatal('*bug*', "The invoked callback function 'rLSGetStatus' does not match Event ID " + data[1] + ".");
  }
};

module.exports = NXTListenerConfig;
