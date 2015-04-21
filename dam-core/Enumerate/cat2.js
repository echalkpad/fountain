var Cylon = require('cylon');

Cylon.robot({
	name: "cat-toy-2",
  connections: {
    loopback: { adaptor: 'loopback' }
  },

  devices: {
    ping: { driver: 'ping' },
    ding: {driver: 'ping' }
  },

  work: function() {
  	console.log("Cat2 work function called ...");
  }
}).start();
