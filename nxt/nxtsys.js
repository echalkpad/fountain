"use strict";

// This module provides access to the non-direct Lego NXT commands.
// For more information, see Appendix 1 - Lego Mindstorms NXT Communication
// Protocol document, in the NXT Bluetooth Development Kit.

var NxtSys = function (nxt) {
	this.nxt = nxt;

	this.EVENTID = {
		'openread' : 0x80,
		'openwrite' : 0x81,
		'read' : 0x82,
		'write' : 0x83,
		'close' : 0x84,
		'delete' : 0x85,
		'findfirst' : 0x86,
		'findnext' : 0x87,
		'getfirmwareversion' : 0x88,
		'openwritelinear' : 0x89,
		'openreadlinear' : 0x8a,
		'openwritedata' : 0x8b,
		'openappenddata' : 0x8c,
		'boot' : 0x97,
		'setbrickname' : 0x98,
		'getdeviceinfo' : 0x9b,
		'deleteuserflash' : 0xa0,
		'pollcommandlength' : 0xa1,
		'poll' : 0xa2,
		'bluetoothfactoryreset' : 0xa4
	};
};

NxtSys.prototype.sys_commands = {
	0x80: 'openread',
	0x81: 'openwrite',
	0x82: 'read',
	0x83: 'write',
	0x84: 'close',
	0x85: 'delete',
	0x86: 'findfirst',
	0x87: 'findnext',
	0x88: 'getfirmwareversion',
	0x89: 'openwritelinear',
	0x8a: 'openreadlinear',
	0x8b: 'openwritedata',
	0x8c: 'openappenddata',
	0x97: 'boot',
	0x98: 'setbrickname',
	0x9b: 'getdeviceinfo',
	0xa0: 'deleteuserflash',
	0xa1: 'pollcommandlength',
	0xa2: 'poll',
	0xa4: 'bluetoothfactoryreset'
};


NxtSys.prototype.get_firmware_version = function () {
	var command = new Buffer([0x01, 0x88]);
	this.nxt.execute_command(command);
};

NxtSys.prototype.get_device_info = function () {
	var command = new Buffer([0x01, 0x9b]);
	this.nxt.execute_command(command);
};

NxtSys.prototype.on = function(event, handler){
	if(this.EVENTID.hasOwnProperty(event)){
		this.nxt.sp.data_handles[this.EVENTID[event]] = handler;
	}
};

module.exports = NxtSys;
