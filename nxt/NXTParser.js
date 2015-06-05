'use strict';

var log = require('book');

 // Emit a data event for each return packet

var NXTParser = function () {

  // This parser assumes that NXT return packages are always contained within
  // a single bluetooth packet and do not span packets.  It also assumes that
  // each return package is transmitted as a single packet with
  // its own byte count.  The input buffer may end up with more than one
  // packet in it sometimes, but in all cases it is expected that there will
  // be a byte count preceding each return packet that follows.

  var data = new Buffer(0);
  return function (emitter, buffer) {

    // append newest bytes

    data = Buffer.concat([data, buffer]);
    log.debug("Raw data buffer: "+ JSON.stringify(data));

    // generate a 'data' event for each packet received

    while (data.length >= 2) {
      var packetLength = (data[1] << 8 ) | data[0];
      if (data.length >= (2 + packetLength)) {
        var out = data.slice(2,2+packetLength);
        data = data.slice(2+packetLength);
        log.debug("Packet buffer: "+ JSON.stringify(out));
        emitter.emit('data', out);
      }
    }
  };
};

module.exports = NXTParser;
