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

Cylon.robot({
  name: "searcher",
  connections: {
    bluetooth: { adaptor: 'ble', uuid: '7dc34621191a4e8d87e9747f561038f6' }
  },

  devices: {
    ble_srv: { driver: 'ble-device-information' }
  },

  display: function(err, data) {
    if (!!err) {
      console.log("Error: ", err);
      return;
    }

    console.log("Data: ", data);
  },

  work: function(my) {
    my.ble_srv.getManufacturerName(my.display);
    }
  }
).start();

//Cylon.start();
