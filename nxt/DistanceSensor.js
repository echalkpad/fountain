"use strict";

// This class provides a baseline implementation of a simple distance implementation.


// @param aPort the NXT port to which the sensor is connected (0-3)

var DistanceSensor = function (aPort) {
	var thePort = aPort+1;
};

DistanceSensor.prototype.readVersion = function() {
	var command = new Buffer([0x01, 0x88]);
ls_write  (thePort, rx_read_length, tx_data)
}
