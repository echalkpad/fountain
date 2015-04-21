var Cylon = require('cylon');

Cylon.api('http');

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
});

Cylon.robot({
  name: "cat2",
  connections: {
    loopback: { adaptor: 'loopback' }
  },

  devices: {
    ping: { driver: 'ping' },
    ding: {driver: 'ping' }
  },

  work: function() {
    console.log("cat2 work function called ...");
  }
});

Cylon.start();
