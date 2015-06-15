'use strict';
// This module provides a do-nothing logger when needed.  It supports the
// same API as npmlog.

var NullLogger = function () {
};

NullLogger.prototype.level = Infinity;

NullLogger.prototype.trace = function () {};
NullLogger.prototype.debug = function () {};
NullLogger.prototype.info = function () {};
NullLogger.prototype.warn = function () {};
NullLogger.prototype.error = function () {};
NullLogger.prototype.fatal = function () {};

exports = NullLogger;
