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

#include "z85codec.h"

typedef unsigned long uint32_t;
// make sure uint32_t is 32-bit
typedef char Z85_uint32_t_static_assert[(sizeof(uint32_t) * CHAR_BIT == 32) * 2 - 1];

//  Maps base 256 to base 85
static char encoder [85 + 1] = {
  "0123456789"
  "abcdefghij"
  "klmnopqrst"
  "uvwxyzABCD"
  "EFGHIJKLMN"
  "OPQRSTUVWX"
  "YZ.-:+=^!/"
  "*?&<>()[]{"
  "}@%$#"
};

//  Maps base 85 to base 256
//  We chop off lower 32 and higher 128 ranges
static byte decoder [96] = {
  0x00, 0x44, 0x00, 0x54, 0x53, 0x52, 0x48, 0x00,
  0x4B, 0x4C, 0x46, 0x41, 0x00, 0x3F, 0x3E, 0x45,
  0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
  0x08, 0x09, 0x40, 0x00, 0x49, 0x42, 0x4A, 0x47,
  0x51, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A,
  0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32,
  0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A,
  0x3B, 0x3C, 0x3D, 0x4D, 0x00, 0x4E, 0x43, 0x00,
  0x00, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
  0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
  0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20,
  0x21, 0x22, 0x23, 0x4F, 0x00, 0x50, 0x00, 0x00
};

//  --------------------------------------------------------------------------
//  Encode a byte array in src into a null-terminated string in dst.

void Z85_encode (byte *src, size_t srcSize, char *dst, size_t dstSize ) {

  if (srcSize % 4) return;

  size_t encoded_size = srcSize * 5 / 4;
  if (dstSize < (encoded_size + 1)) return;
  
  size_t char_nbr = 0;
  size_t byte_nbr = 0;
  uint32_t value = 0;
  while (byte_nbr < srcSize) {
    //  Accumulate value in base 256 (binary)
    value = (value * 256) + src[byte_nbr++];
    if (byte_nbr % 4 == 0) {
      //  Output value in base 85
      uint32_t divisor = 85UL * 85UL * 85UL * 85UL;
      while (divisor) {
        int bits = (value / divisor) % 85; 
        dst[char_nbr++] = encoder[(value / divisor) % 85];
        divisor /= 85;
      }
      value = 0;
    }
  }
  dst[char_nbr] = 0;
  return;
}

//  --------------------------------------------------------------------------
//  Decode an encoded string into a byte array; number of elements in array will be
//  strlen (src) * 4 / 5.

void Z85_decode (char *src, size_t srcSize,  byte *dst, size_t dstSize ) {
  
    if (strlen (src) % 5) return;

    size_t decoded_size = strlen (src) * 4 / 5;
  if (dstSize < (decoded_size)) return;

    size_t byte_nbr = 0;
    size_t char_nbr = 0;
    uint32_t value = 0;
    while (char_nbr < strlen (src)) {
//        Accumulate value in base 85
        value = (value * 85) + decoder [(byte) src [char_nbr++] - 32];
        if (char_nbr % 5 == 0) {
//            Output value in base 256
            uint32_t divisor = 256UL * 256UL * 256UL;
            while (divisor) {
                dst [byte_nbr++] = (value / divisor) % 256;
                divisor /= 256;
            }
            value = 0;
        }
    }
    return;
}

size_t Z85_encode_bound(size_t inputByteCount) {
  return inputByteCount * 5 / 4;
}

size_t Z85_decode_bound(size_t inputCharCount) {
  return inputCharCount * 4 / 5;
}


