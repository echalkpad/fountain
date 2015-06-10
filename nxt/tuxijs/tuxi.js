'use strict';

// This module provides various utility functions for use with node.js programs.
//
// To use:  var Tuxi = require('tuxi');
//			var tux = new Tuxi();
//			var strval = tux.toHex(0xf);  //strval is'0F'

var Tuxi = function () {
};

Tuxi.prototype.toHex = function (d) {
    return  ("0"+(Number(d).toString(16))).slice(-2).toUpperCase();
};

// var log = new tux.Logger();

Tuxi.prototype.Logger = function () {
  return require('npmlog');
};

module.exports = Tuxi;
