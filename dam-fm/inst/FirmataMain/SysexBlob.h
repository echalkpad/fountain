/*
  SysexBlob.h
  This module implements the ability to return a list of values from a Firmata device.
*/

#ifndef SysexBlob_h
#define SysexBlob_h

#include <Firmata.h>
#include "SysexCore.h"

// BLOB_CONFIG <blob-id> <... config info ...>
// BLOB_STATUS <blob-id>
// BLOB_WRITE  <blob-id> <... data to write ...>
// BLOB_READ   <blob-id>

#define BLOB_CONFIG 			0x10
#define BLOB_CONFIG_RESPONSE	0x11
#define BLOB_STATUS             0x12
#define BLOB_STATUS_RESPONSE    0x13
#define BLOB_WRITE              0x14
#define BLOB_WRITE_RESPONSE     0x15
#define BLOB_READ               0x16
#define BLOB_READ_RESPONSE      0x17

void setSysexBlobHooks(sysexCallbackFunction *h);

void processBlobConfig(byte command, byte argc, byte *argv);
void processBlobStatus(byte command, byte argc, byte *argv);
void processBlobWrite(byte command, byte argc, byte *argv);
void processBlobRead(byte command, byte argc, byte *argv);

extern sysexCallbackFunction hooks[127];	// command jump table, 1 pointer per possible command

#endif  /* SysexBlob_h */
