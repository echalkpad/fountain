// Import libraries (BLEPeripheral depends on SPI)
#include <SPI.h>
#include <BLEPeripheral.h>

// define pins (varies per shield/board)
#define BLE_REQ   10
#define BLE_RDY   2
#define BLE_RST   9

// create peripheral instance, see pinouts above
BLEPeripheral blePeripheral = BLEPeripheral(BLE_REQ, BLE_RDY, BLE_RST);

// uuid's can be:
//   16-bit: "ffff"
//  128-bit: "19b10010e8f2537e4f6cd104768a1214" (dashed format also supported)

// create one or more services
BLEService service = BLEService("181A");

// create one or more characteristics
BLECharCharacteristic c1 = BLECharCharacteristic("2A6E", BLERead );  // temperature
BLECharCharacteristic c2 = BLECharCharacteristic("2A77", BLERead );  // irradiance

// create one or more descriptors (optional)
//BLEDescriptor descriptor = BLEDescriptor("2901", "value");

void setup() {
  Serial.begin(115200);
#if defined (__AVR_ATmega32U4__)
  delay(5000);  //5 seconds delay for enabling to see the start up comments on the serial board
#endif

  blePeripheral.setLocalName("SenseNode"); // optional
  blePeripheral.setAdvertisedServiceUuid(service.uuid()); // optional

  // add attributes (services, characteristics, descriptors) to peripheral
  blePeripheral.addAttribute(service);
  blePeripheral.addAttribute(c1);
  blePeripheral.addAttribute(c2);
 // blePeripheral.addAttribute(descriptor);

  // set initial value
  c1.setValue(20);
  c2.setValue(30);

  // begin initialization
  blePeripheral.begin();
}

void loop() {
  BLECentral central = blePeripheral.central();

  if (central) {
    // central connected to peripheral
    Serial.print(F("Connected to central: "));
    Serial.println(central.address());

    while (central.connected()) {
      // central still connected to peripheral
//      if (characteristic.written()) {
//        // central wrote new value to characteristic
//        Serial.println(characteristic.value(), DEC);
//
//        // set value on characteristic
//        characteristic.setValue(5);
//      }
    }

    // central disconnected
    Serial.print(F("Disconnected from central: "));
    Serial.println(central.address());
  }
}
