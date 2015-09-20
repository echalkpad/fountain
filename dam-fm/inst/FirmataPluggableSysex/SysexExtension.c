/*
	This file defines a custom sysex message processer, an extension to the one in StandardFirmata.
 */

#ifdef OPT_ENABLE_SYSEX_HOOK

#include <firmata.h>


sysexCallbackFunction sysexExtension(byte command, byte argc, byte *argv) {

}


sysexCallbackFunction currentSysexHookCallback = sysexExtension;

#endif
