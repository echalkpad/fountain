'use strict';

var Tuxi = require('../../tuxijs');

var active = false;
console.log("Active LogMgr: ",active);

var log = new Tuxi.LogMgr(active);
var prefix = "test";

var names = ["all","trace","debug", "info", "warn", "error", "fatal","off"];

for (var lvl = 0; lvl<names.length; lvl++) {
  log.level = names[lvl];
  console.log("\nLevel: "+log.level);
  log.trace(prefix,"trace message");
  log.debug(prefix,"debug message");
  log.info(prefix, "info message");
  log.warn(prefix, "warn message");
  log.error(prefix,"error message");
  log.fatal(prefix,"fatal message");
}


var active = true;
console.log("Active LogMgr: ",active);

var log = new Tuxi.LogMgr(active);
var prefix = "test";

var names = ["all","trace","debug", "info", "warn", "error", "fatal","off"];

for (var lvl = 0; lvl<names.length; lvl++) {
  log.level = names[lvl];
  console.log("\nLevel: "+log.level);
  log.trace(prefix,"trace message");
  log.debug(prefix,"debug message");
  log.info(prefix, "info message");
  log.warn(prefix, "warn message");
  log.error(prefix,"error message");
  log.fatal(prefix,"fatal message");
}



