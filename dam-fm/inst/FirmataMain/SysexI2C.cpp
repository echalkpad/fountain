/*
  This module implements Firmata's I2C capabilities using Sysex commands.
*/

#include "SysexI2C.h"

// Globals

/* i2c data */
struct i2c_device_info {
  byte addr;
  byte reg;
  byte bytes;
};

/* for i2c read continuous more */
i2c_device_info query[MAX_QUERIES];

byte i2cRxData[32];
signed char queryIndex = -1;
unsigned int i2cReadDelayTime = 0;  // default delay time between i2c read request and Wire.requestFrom()

boolean isI2CEnabled = false;

// Initialize our part of the Sysex command jump table

void setSysexI2CHooks(sysexCallbackFunction *h) {
  h[I2C_CONFIG] = processI2CConfig;
  h[I2C_REQUEST] = processI2CRequest;
}

// Define the functions that implement the I2C Sysex capabilities

void processI2CConfig(byte command, byte argc, byte *argv) {
  unsigned int delayTime;
  delayTime = (argv[0] + (argv[1] << 7));

  if (delayTime > 0) {
    i2cReadDelayTime = delayTime;
  }

  if (!isI2CEnabled) {
    enableI2CPins();
  }
}

void processI2CRequest(byte command, byte argc, byte *argv) {
  byte mode;
  byte slaveAddress;
  byte slaveRegister;
  byte data;
  unsigned int delayTime;

  mode = argv[1] & I2C_READ_WRITE_MODE_MASK;
  if (argv[1] & I2C_10BIT_ADDRESS_MODE_MASK) {
    Firmata.sendString("10-bit addressing mode is not yet supported");
    return;
  }
  else {
    slaveAddress = argv[0];
  }

  switch (mode) {
    case I2C_WRITE:
      Wire.beginTransmission(slaveAddress);
      for (byte i = 2; i < argc; i += 2) {
        data = argv[i] + (argv[i + 1] << 7);
#if ARDUINO >= 100
        Wire.write(data);
#else
        Wire.send(data);
#endif
      }
      Wire.endTransmission();
      delayMicroseconds(70);
      break;
    case I2C_READ:
      if (argc == 6) {
        // a slave register is specified
        slaveRegister = argv[2] + (argv[3] << 7);
        data = argv[4] + (argv[5] << 7);  // bytes to read
        readAndReportI2CData(slaveAddress, (int)slaveRegister, data);
      }
      else {
        // a slave register is NOT specified
        data = argv[2] + (argv[3] << 7);  // bytes to read
        readAndReportI2CData(slaveAddress, (int)REGISTER_NOT_SPECIFIED, data);
      }
      break;
    case I2C_READ_CONTINUOUSLY:
      if ((queryIndex + 1) >= MAX_QUERIES) {
        // too many queries, just ignore
        Firmata.sendString("too many queries");
        break;
      }
      queryIndex++;
      query[queryIndex].addr = slaveAddress;
      query[queryIndex].reg = argv[2] + (argv[3] << 7);
      query[queryIndex].bytes = argv[4] + (argv[5] << 7);
      break;
    case I2C_STOP_READING:
      byte queryIndexToSkip;
      // if read continuous mode is enabled for only 1 i2c device, disable
      // read continuous reporting for that device
      if (queryIndex <= 0) {
        queryIndex = -1;
      } else {
        // if read continuous mode is enabled for multiple devices,
        // determine which device to stop reading and remove its data from
        // the array, shifiting other array data to fill the space
        for (byte i = 0; i < queryIndex + 1; i++) {
          if (query[i].addr = slaveAddress) {
            queryIndexToSkip = i;
            break;
          }
        }

        for (byte i = queryIndexToSkip; i < queryIndex + 1; i++) {
          if (i < MAX_QUERIES) {
            query[i].addr = query[i + 1].addr;
            query[i].reg = query[i + 1].addr;
            query[i].bytes = query[i + 1].bytes;
          }
        }
        queryIndex--;
      }
      break;
    default:
      break;
  }
}

void readAndReportContinuousI2CData() {
  if (queryIndex > -1) {
    for (byte i = 0; i < queryIndex + 1; i++) {
      readAndReportI2CData(query[i].addr, query[i].reg, query[i].bytes);
    }
  }
}

void readAndReportI2CData(byte address, int theRegister, byte numBytes) {

  //
  // allow I2C requests that don't require a register read
  // for example, some devices using an interrupt pin to signify new data available
  // do not always require the register read so upon interrupt you call Wire.requestFrom()

  if (theRegister != REGISTER_NOT_SPECIFIED) {
    Wire.beginTransmission(address);
#if ARDUINO >= 100
    Wire.write((byte)theRegister);
#else
    Wire.send((byte)theRegister);
#endif
    Wire.endTransmission();
    // do not set a value of 0
    if (i2cReadDelayTime > 0) {
      // delay is necessary for some devices such as WiiNunchuck
      delayMicroseconds(i2cReadDelayTime);
    }
  } else {
    theRegister = 0;  // fill the register with a dummy value
  }

  Wire.requestFrom(address, numBytes);  // all bytes are returned in requestFrom

  // check to be sure correct number of bytes were returned by slave
  if (numBytes == Wire.available()) {
    i2cRxData[0] = address;
    i2cRxData[1] = theRegister;
    for (int i = 0; i < numBytes; i++) {
#if ARDUINO >= 100
      i2cRxData[2 + i] = Wire.read();
#else
      i2cRxData[2 + i] = Wire.receive();
#endif
    }
  }
  else {
    if (numBytes > Wire.available()) {
      Firmata.sendString("I2C Read Error: Too many bytes received");
    } else {
      Firmata.sendString("I2C Read Error: Too few bytes received");
    }
  }

  // send slave address, register and received bytes
  Firmata.sendSysex(SYSEX_I2C_REPLY, numBytes + 2, i2cRxData);
}


void enableI2CPins()
{
  byte i;
  // is there a faster way to do this? would probaby require importing
  // Arduino.h to get SCL and SDA pins
  for (i = 0; i < TOTAL_PINS; i++) {
    if (IS_PIN_I2C(i)) {
      // mark pins as i2c so they are ignore in non i2c data requests
      setPinModeCallback(i, I2C);
    }
  }

  isI2CEnabled = true;

  // is there enough time before the first I2C request to call this here?
  Wire.begin();
}

/* disable the i2c pins so they can be used for other functions */
void disableI2CPins() {
  isI2CEnabled = false;
  // disable read continuous mode for all devices
  queryIndex = -1;
  // uncomment the following if or when the end() method is added to Wire library
  // Wire.end();
}


