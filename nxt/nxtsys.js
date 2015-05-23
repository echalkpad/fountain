"use strict";

// This module provides access to the non-direct Lego NXT commands.
// For more information, see Appendix 1 - Lego Mindstorms NXT Communication 
// Protocol document, in the NXT Bluetooth Development Kit.

var NxtSys = function (nxt) {
	this.nxt = nxt;
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

NxtSys.prototype.nxt_error_messages = {
	0x00: "OK",
	0x20: "Pending communication transaction in progress",
	0x40: "Specified mailbox queue is empty",
	0xbd: "Request failed (i.e. specified file not found)",
	0xbe: "Unknown command opcode",
	0xbf: "Insane packet",
	0xc0: "Data contains out-of-range values",
	0xdd: "Communication bus error",
	0xde: "No free memory in communication buffer",
	0xdf: "Specified channel/connection is not valid",
	0xe0: "Specified channel/connection not configured or busy",
	0xec: "No active program",
	0xed: "Illegal size specified",
	0xee: "Illegal mailbox queue ID specified",
	0xef: "Attempted to access invalid field of a structure",
	0xf0: "Bad input or output specified",
	0xfb: "Insufficient memory available",
	0xff: "Bad arguments"
};

NxtSys.prototype.get_firmware_version = function () {
	var command = new Buffer([0x01, 0x88]);
	this.nxt.execute_command(command);
};

NxtSys.prototype.get_device_info = function () {
	var command = new Buffer([0x01, 0x9b]);
	this.nxt.execute_command(command);
};


module.exports = NxtSys;