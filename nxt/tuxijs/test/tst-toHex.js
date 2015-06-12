#!/usr/bin/env node
'use strict';

var Tuxi = require('../tuxi');
var tjs = new Tuxi();
var strval = [];
for (var i=0; i<256; i++) {
	strval[i] = tjs.toHex(i);
}
console.log(strval);
