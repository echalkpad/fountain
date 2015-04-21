var Cylon = require('cylon');

Cylon.robot({
	name: "cat1",
  connections: {
    loopback: { adaptor: 'loopback' }
  },

  devices: {
    ping: { driver: 'ping' },
    ding: {driver: 'ping' }
  },

  work: function() {
  	console.log("cat1 work function called ...");
  }
}).start();
