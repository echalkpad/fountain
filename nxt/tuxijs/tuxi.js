'use strict';

// This module provides various utility functions for use with node.js programs.
//
// To use:
//			var tux = new  require('tuxi');
//			var strval = tux.toHex(0xf);  //strval is'0F'

var Tuxi = function () {
};

Tuxi.prototype.toHex = function (d) {
    return  ("0"+(Number(d).toString(16))).slice(-2).toUpperCase();
};

// This function is a constructor that can be used with the 'new' operator
// to create a new logging object.
//
// var Tuxi = require('tuxi');
// var log = new Tuxi.LogMgr(true);
// var prefix = "test";
//
// apache:   all, trace, debug,   info,        warn, error, fatal, off
// npmlog: silly,        verbose, info, http,  warn, error,        silent
// slf4j:         trace, debug,   info,        warn, error
// log4j:    all,        debug,   info,        warn, error, fatal, off

var LogMgr = function (enable) {
    var aLogger;

    var useNullLogger =
      (typeof enable === 'undefined') ||
      (enable === null ) ||
      (enable === 0) || (enable === '0') ||
      (enable === false) || (enable === 'false');

    if (useNullLogger) {
      aLogger = new NullLogger();
    } else {
      aLogger = new require('npmlog');
    }

    aLogger.prefixStyle = {fg: 'yellow', bg: 'black'};
    aLogger.headingStyle = {fg: 'yellow'};
    aLogger.heading = '';

    aLogger.addLevel('all', -Infinity);
    aLogger.addLevel('trace', 1000, { fg: 'green', bg: 'black' });
    aLogger.addLevel('debug', 2000, { fg: 'green', bg: 'black' });
    aLogger.addLevel('info',  3000, { fg: 'green', bg: 'black' }, 'info ');
    aLogger.addLevel('warn',  4000, { fg: 'red', bg: 'black' }, 'warn ');
    aLogger.addLevel('error', 5000, { fg: 'red', bg: 'black' });
    aLogger.addLevel('fatal', 6000, { fg: 'red', bg: 'black' });
    aLogger.addLevel('off', Infinity);
    return aLogger;
};

var NullLogger = function () {
    this.addLevel = function (lvl) {
      if (!this[lvl]) {
        this[lvl] = function () {};
      }
    };
};

exports.Tuxi = Tuxi;
exports.LogMgr = LogMgr;
exports.NullLogger = NullLogger;
