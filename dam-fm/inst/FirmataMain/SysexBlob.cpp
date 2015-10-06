/*
  This module implements the ability to return a binary blob from a Firmata device.
*/

#include "SysexBlob.h"

// Globals

// Initialize our part of the Sysex command jump table

void setSysexBlobHooks(sysexCallbackFunction *h) {
  h[BLOB_CONFIG] = processBlobConfig;
  h[BLOB_STATUS] = processBlobStatus;
  h[BLOB_WRITE] = processBlobWrite;
  h[BLOB_READ] = processBlobRead;

  h[BLOB_CONFIG_RESPONSE] = 0;
  h[BLOB_STATUS_RESPONSE] = 0;
  h[BLOB_WRITE_RESPONSE] = 0;
  h[BLOB_READ_RESPONSE] = 0;

}

// Define the functions that implement the Sysex Blob capabilities

void processBlobConfig(byte command, byte argc, byte *argv) {

}

void processBlobStatus(byte command, byte argc, byte *argv) {
}


void processBlobWrite(byte command, byte argc, byte *argv) {

}

void processBlobRead(byte command, byte argc, byte *argv) {
}

