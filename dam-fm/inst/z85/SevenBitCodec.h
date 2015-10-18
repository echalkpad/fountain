/**
 * The signatures defined here are the interface to methods that
 * provide encode / decode from 8-bit binary bytes to 7-bit transmittable
 * chars.  The initial implementation of this interface is Z85codec, but
 * any other codec can be substituted if needed to match a particular
 * existing implementation or other constraint.
 */

#ifndef SEVEN_BIT_CODEC_H
#define SEVEN_BIT_CODEC_H

#include <stdio.h>
#include <stdint.h>
#include <limits.h>

typedef unsigned char byte;

#if defined (__cplusplus)
extern "C" {
#endif

size_t encodedSize(size_t inputByteCount);
size_t decodedSize(size_t inputCharCount);

void encodeBytes(byte *src, size_t srcSize,  char *dst, size_t dstSize );
void decodeChars(char *src, size_t srcSize,  byte *dst, size_t dstSize );

#if defined (__cplusplus)
}
#endif

#endif

