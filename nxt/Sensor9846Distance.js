'use strict';

// This class provides a driver implementation for the Lego Nxt 9846 Ultrasonic Distance Sensor

// @param aBrick the 'mindstorms_bluetooth' object that manages NXT communications
// @param aChannel the NXT sensor channel to which the sensor is connected (0-3)

var Sensor9846Distance = function (aBrick, aChannel) {
  this.nxt = aBrick;
  this.theChannel = aChannel;
  this.nxt.set_input_state(this.theChannel, this.nxt.LOWSPEED_9V, this.nxt.RAWMODE);
};

//--- the above action takes a while to complete.  Need to queue until ready or something

Sensor9846Distance.prototype.i2c_commands = {

  // read constant values

  "version" : [ 0x02, 0x00, 0x03 ],
  "product_id" : [ 0x02, 0x08, 0x03 ],
  "sensor_type" : [ 0x02, 0x10, 0x03 ],
  "factory_zero" : [ 0x02, 0x11, 0x03 ],
  "factory_scale_factor" : [ 0x02, 0x12, 0x03 ],
  "factory_scale_divisor" : [ 0x02, 0x13, 0x03 ],
  "units" : [ 0x02, 0x14, 0x03 ],

  // read variable values

  "interval" : [ 0x02, 0x40, 0x03 ],
  "state" : [ 0x02, 0x41, 0x03 ],
  "sample_0" : [ 0x02, 0x42, 0x03 ],
  "sample_1" : [ 0x02, 0x43, 0x03 ],
  "sample_2" : [ 0x02, 0x44, 0x03 ],
  "sample_3" : [ 0x02, 0x45, 0x03 ],
  "sample_4" : [ 0x02, 0x46, 0x03 ],
  "sample_5" : [ 0x02, 0x47, 0x03 ],
  "sample_6" : [ 0x02, 0x48, 0x03 ],
  "sample_7" : [ 0x02, 0x49, 0x03 ],
  "actual_zero" : [ 0x02, 0x50, 0x03 ],
  "actual_scale_factor" : [ 0x02, 0x51, 0x03 ],
  "actual_scale_divisor" : [ 0x02, 0x52, 0x03 ],

  // perform other actions

  "off" : [ 0x02, 0x41, 0x00 ],
  "single" : [ 0x02, 0x41, 0x01 ],
  "continuous" : [ 0x02, 0x41, 0x02 ],
  "event_capture" : [ 0x02, 0x41, 0x03 ],
  "warm_reset" : [ 0x02, 0x41, 0x04 ],
  "set_interval" : [ 0x02, 0x40, 0x00 ],
  "set_actual_zero" : [ 0x02, 0x50, 0x00 ],
  "set_actual_scale_factor" : [ 0x02, 0x51, 0x00 ],
  "set_actual_scale_divisor" : [ 0x02, 0x52, 0x00 ]
};

Sensor9846Distance.prototype.i2c_errors = {
  0x20 : "STAT_comm_pending",
  0xdd : "ERR_comm_bus_err",
  0xdf : "ERR_comm_chan_invalid",
  0xe0 : "ERR_comm_chan_not_ready"
};

Sensor9846Distance.prototype.readVersion = function () {
  console.log("readversion");
  console.log("channel: " + this.theChannel + ", cmd string: " + this.i2c_commands.version);
  this.nxt.ls_write(this.theChannel, 8, this.i2c_commands.version);
};

Sensor9846Distance.prototype.readState = function () {
  console.log("readstate");
  console.log("ls_write with channel: " + this.theChannel + ", cmd string: " + this.i2c_commands.state);
  this.nxt.ls_write(this.theChannel, 1, this.i2c_commands.state);
};

module.exports = Sensor9846Distance;
