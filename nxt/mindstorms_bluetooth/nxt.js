"use strict";

// An instance of PacketParser is provided to the SerialPort class in order to
// parse the reply packages coming back from the NXT unit over Bluetooth.  It
// emits a 'data' event for each return packet it sees.

// This parser assumes that NXT return packages are always contained within
// a single bluetooth packet and do not span packets.  It also assumes that
// each return package is transmitted as a single packet with
// its own byte count.  The input buffer may end up with more than one
// packet in it sometimes, but in all cases it is expected that there will
// be a byte count preceding each return packet.

var PacketParser = function () {

  var data = new Buffer(0);
  return function (emitter, buffer) {

    // append newest bytes

    data = Buffer.concat([data, buffer]);
    console.log("Buffer to parse: "+ JSON.stringify(data));

    // generate a 'data' event for each packet received

    while (data.length >= 2) {
      var packetLength = (data[1] << 8 ) | data[0];
      if (data.length >= (2 + packetLength)) {
        var out = data.slice(2,2+packetLength);
        data = data.slice(2+packetLength);
        emitter.emit('data', out);
      }
    }
  };
};

var NullLogger = function () {
	this.level = Infinity;
	this.trace = function () {};
	this.debug = function () {};
	this.info = function () {};
	this.warn = function () {};
	this.error = function () {};
	this.fatal = function () {};
};



//TODO:
// - Input checking
// - proper close after use, maybe something with detect if command have been
//   run, only exit when no commands need to be answered.

// DWJ 6/2015 - Use PacketParser instead of parser.raw so that each 'data' event
// 							provides one and only one return packet to the event handler.
// 						- Rewrite the event handler methods so that we don't have to push
// 						  any new properties into the SerialPort object.

var SerialPort = require("serialport").SerialPort;

var Nxt = function (port, optNxt, optSerialPort) {
	this.debug = false;

	//Output ports
	this.MOTOR_A = 0x00;
	this.MOTOR_B = 0x01;
	this.MOTOR_C = 0x02;
	this.MOTOR_ALL = 0xff;

	//Output modes
	this.MOTORON = 0x01;
	this.BRAKE = 0x02;
	this.REGULATED = 0x04;

	//Regulations mode
	this.REGULATION_MODE_IDLE = 0x00;
	this.REGULATION_MODE_MOTOR_SPEED = 0x01;
	this.REGULATION_MODE_MOTOR_SYNC = 0x02;

	//Runstate
	this.MOTOR_RUN_STATE_IDLE = 0x00;
	this.MOTOR_RUN_STATE_RAMPUP = 0x10;
	this.MOTOR_RUN_STATE_RUNNING = 0x20;
	this.MOTOR_RUN_STATE_RAMPDOWN = 0x40;

	//Input ports
	this.INPUT_PORT_1 = 0x00;
	this.INPUT_PORT_2 = 0x01;
	this.INPUT_PORT_3 = 0x02;
	this.INPUT_PORT_4 = 0x03;

	//Sensor types
	this.NO_SENSOR = 0x00;
	this.SWITCH = 0x01;
	this.TEMPERATURE = 0x02;
	this.REFLECTION = 0x03;
	this.ANGLE = 0x04;
	this.LIGHT_ACTIVE = 0x05;
	this.LIGHT_INACTIVE = 0x06;
	this.SOUND_DB = 0x07;
	this.SOUND_DBA = 0x08;
	this.CUSTOM = 0x09;
	this.LOWSPEED = 0x0a;
	this.LOWSPEED_9V = 0x0b;
	this.NO_OF_SENSOR_TYPES = 0x0c;

	//Sensor modes
	this.RAWMODE = 0x00;
	this.BOOLEANMODE = 0x20;
	this.TRANSITIONCNTMODE = 0x40;
	this.PERIODCOUNTERMODE = 0x60;
	this.PCTFULLSCALEMODE = 0x80;
	this.CELSIUSMODE = 0xa0;
	this.FAHRENHEITMODE = 0xc0;
	this.ANGLESTEPSMODE = 0xe0;
	this.SLOPEMASK = 0x1f;
	this.MODEMASK = 0xe0;

	// map all response event ids to default handler for now

	var key;
	this.nxtEventHandler = {};
	for (key in this.nxt_commands) {
		this.nxtEventHandler[key] = this.generic_response_handler;
	}

	// process options to control this Nxt instance

	var nxtOptions = {"log": new NullLogger()};
	if ((typeof optNxt !== 'undefined') && (optNxt !== null)) {
		for (key in optNxt) {
			nxtOptions[key] = optNxt[key];
		}
	}
	this.log = nxtOptions.log;

	// process options for the serial port

	var portOptions = {"parser": new PacketParser() };
	if ((typeof optSerialPort !== 'undefined') && (optNxt !== null)) {
	for (key in optSerialPort) {
			portOptions[key] = optSerialPort[key];
		}
	}

	this.sp = new SerialPort(port, portOptions);
	this.packetDataHandler	= this.packetDataHandler.bind(this);
  this.sp.on('data', this.packetDataHandler);

	this.initialized = true;
};

// Each time a 'data' event is emitted by the parser, run this method and
// call the appropriate NXT event handler.

Nxt.prototype.packetDataHandler = function (data) {
  this.log.debug('packet',data[0] +" " + data[1] +" " + data[2] +
		" plus "+ (data.length-3) + ' more bytes.');
	this.nxtEventHandler[data[1]](data);
};

Nxt.prototype.on = function(event, handler) {
	if (event in this.EVENTID) {
		this.nxtEventHandler[this.EVENTID[event]] = handler;
	} else {
		console.log("Unable to register handler for unknown event "+event);
	}
};
Nxt.prototype.register_callback = Nxt.prototype.on;

Nxt.prototype.generic_response_handler = function (data) {
	console.log("Response package: "+ data[0] +" " + data[1] + " " + data[2] +
		" plus "+ (data.length-3) + ' more bytes.');
};

Nxt.prototype.execute_command = function (command) {
	//The bluetooth packet need a length (2-bytes) in front of the packet
	var real_command = new Buffer(command.length + 2);
	real_command[0] = command.length & 0xff;
	real_command[1] = (command.length >> 8) & 0xff;
	command.copy(real_command, 2);
	if (this.initialized !== true) {
		this.log.fatal('nxt.js',"NXT not initialized!!!");
	}
	this.sp.write(real_command);
};

//------------------------------------------------

Nxt.prototype.get_firmware_version = function () {
	var command = new Buffer([0x01, 0x88]);
	this.execute_command(command);
};

Nxt.prototype.get_device_info = function () {
	var command = new Buffer([0x01, 0x9b]);
	this.execute_command(command);
};

//------------------------------------------------

Nxt.prototype.start_program = function (program_name) {
	var command_arr = [0x00,0x00];
	var chars = program_name.split('');
	var i;
	for (i in chars) {
		command_arr.push(chars[i]);
	}
	var command = new Buffer(command_arr);
	this.execute_command(command);
};

Nxt.prototype.stop_program = function () {
	var command = new Buffer([0x00, 0x01]);
	this.execute_command(command);
};

Nxt.prototype.play_tone = function (freq, dur) {
	var command = new Buffer([0x00, 0x03, freq & 0xff, (freq >> 8) & 0xff, dur & 0x00ff, (dur >> 8) & 0xff]);
	this.execute_command(command);
};

Nxt.prototype.set_output_state = function (port, power, mode, reg_mode, turn_ratio, run_state, tacho_limit) {
	var tacho = [];
	tacho[0] = tacho_limit & 0xff;
	tacho[1] = (tacho_limit >> 8) & 0xff;
	tacho[2] = (tacho_limit >> 16) & 0xff;
	tacho[3] = (tacho_limit >> 24) & 0xff;
	// The power is set between -100 and 100
	var command_arr = [0x00, 0x04, port, power, mode, reg_mode, ((turn_ratio + 100) % 200), run_state, tacho[0], tacho[1], tacho[2], tacho[3]];
	var command = new Buffer(command_arr);
	this.execute_command(command);
};

Nxt.prototype.set_input_state = function (port, sensor_type, sensor_mode) {
	var command = new Buffer([0x00, 0x05, port, sensor_type, sensor_mode]);
	this.execute_command(command);
};

Nxt.prototype.get_output_state = function (port) {
	var command = new Buffer([0x00, 0x06, port]);
	this.execute_command(command);
};

Nxt.prototype.get_input_values = function (port) {
	var command = new Buffer([0x00, 0x07, port]);
	this.execute_command(command);
};

Nxt.prototype.reset_input_scaled_value = function (port) {
	var command = new Buffer([0x00, 0x08, port]);
	this.execute_command(command);
};

//Message is needed to be an array
Nxt.prototype.message_write = function (inbox_no, message) {
	var command_arr = [0x00, 0x09, inbox_no, message.length];
	var i;
	for (i in message) {
		command_arr.push(message[i]);
	}
	var command = new Buffer(command_arr);
	this.execute_command(command);
};

Nxt.prototype.reset_motor_position = function (port, relative) {
	var command = new Buffer([0x00, 0x0a, port, (relative ? 0x01 : 0x00)]);
	this.execute_command(command);
};

Nxt.prototype.get_battery_level = function () {
	var command = new Buffer([0x00, 0x0b]);
	this.execute_command(command);
};

Nxt.prototype.stop_sound_playback = function () {
	var command = new Buffer([0x00, 0x0c]);
	this.execute_command(command);
};

Nxt.prototype.keep_alive = function () {
	var command = new Buffer([0x00, 0x0d]);
	this.execute_command(command);
};

Nxt.prototype.ls_get_status = function (port) {
	var command = new Buffer([0x00, 0x0e, port]);
	this.execute_command(command);
};

//tx_data needs to be an array
Nxt.prototype.ls_write = function (port, rx_read_length, tx_data) {
	var command_arr = [0x00, 0x0f, port, tx_data.length, rx_read_length];
	var i;
	for (i in tx_data) {
		command_arr.push(tx_data[i]);
	}
	var command = new Buffer(command_arr);
	this.execute_command(command);
};

Nxt.prototype.ls_read = function (port) {
	var command = new Buffer([0x00, 0x10, port]);
	this.execute_command(command);
};

Nxt.prototype.get_current_program_name = function () {
	var command = new Buffer([0x00, 0x11]);
	this.execute_command(command);
};

Nxt.prototype.message_read = function (remote_inbox_no, local_inbox_no, remove_remote_msg) {
	var command = new Buffer([0x00, 0x13, remote_inbox_no, local_inbox_no, (remove_remote_msg ? 0x01 : 0x00)]);
	this.execute_command(command);
};

Nxt.prototype.close_connection = function (sp) {
	//sp.end(false,null);
	if (this.debug) {
		console.log("Closing serialport connection");
	}
	this.sp.end(false, null);
};

//------------------------------------------------

Nxt.prototype.nxt_error_messages = {
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

Nxt.prototype.nxt_commands = {

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
	0xa4: 'bluetoothfactoryreset',

	0x00: 'startprogram',
	0x01: 'stopprogram',
	0x02: 'playsoundfile',
	0x03: 'playtone',
	0x04: 'setoutputstate',
	0x05: 'setinputmode',
	0x06: 'getoutputstate',
	0x07: 'getinputvalues',
	0x08: 'resetinputscaledvalue',
	0x09: 'messagewrite',
	0x0a: 'resetmotorposition',
	0x0b: 'getbatterylevel',
	0x0c: 'stopsoundplayback',
	0x0d: 'keepalive',
	0x0e: 'lsgetstatus',
	0x0f: 'lswrite',
	0x10: 'lsread',
	0x11: 'getcurrentprogramname',
	0x13: 'messageread'
};

Nxt.prototype.EVENTID = {

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
		'bluetoothfactoryreset' : 0xa4,

		'startprogram' : 0x00,
		'stopprogram' : 0x01,
		'playsoundfile': 0x02,
		'playtone' : 0x03,
		'setoutputstate' : 0x04,
		'setinputmode' : 0x05,
		'getoutputstate' : 0x06,
		'getinputvalue' : 0x07,
		'resetinputscaledvalue' : 0x08,
		'messagewrite' : 0x09,
		'resetmotorposition' : 0x0A,
		'getbatterylevel' : 0x0B,
		'stopsoundplayback' : 0x0C,
		'keepalive' : 0x0D,
		'lsgetstatus' : 0x0E,
		'lswrite' : 0x0F,
		'lsread' : 0x10,
		'getcurrentprogramname' : 0x11,
		'messageread' : 0x13
	};

module.exports.Nxt = Nxt;
