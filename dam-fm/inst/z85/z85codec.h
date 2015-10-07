//  --------------------------------------------------------------------------
//  Reference implementation for rfc.zeromq.org/spec:32/Z85
//
//  This implementation provides a Z85 codec as an easy-to-reuse C class
//  designed to be easy to port into other languages.

//  --------------------------------------------------------------------------
//  Copyright (c) 2010-2013 iMatix Corporation and Contributors
//
//  Permission is hereby granted, free of charge, to any person obtaining a
//  copy of this software and associated documentation files (the "Software"),
//  to deal in the Software without restriction, including without limitation
//  the rights to use, copy, modify, merge, publish, distribute, sublicense,
//  and/or sell copies of the Software, and to permit persons to whom the
//  Software is furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
//  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
//  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
//  DEALINGS IN THE SOFTWARE.
//  --------------------------------------------------------------------------

#ifndef Z85Codec_h
#define Z85Codec_h

//#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <limits.h>

#include "Arduino.h"
#include "HardwareSerial.h" 

typedef unsigned char byte;

#if defined (__cplusplus)
extern "C" {
#endif

size_t Z85_encode_bound(size_t inputByteCount);
size_t Z85_decode_bound(size_t inputCharCount);

void Z85_encode (byte *src, size_t srcSize,  char *dst, size_t dstSize );
void Z85_decode (char *src, size_t srcSize,  byte *dst, size_t dstSize );

#if defined (__cplusplus)
}
#endif

#endif

