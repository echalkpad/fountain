/**
 * The purpose of the functions defined here is to manage differences
 * in byte order (big-endian, little-endian) between two hosts.
 */

#include "SevenBitCodec.h"
#include "TypedBinaryWrapper.h"

// single byte values are not affected by addressing issues

void encodeInt8(int8_t *src,int itemCount, char *dst, size_t dstSize) {
  encodeUInt8((uint8_t *)src,itemCount,dst,dstSize);
}

void encodeUInt8(uint8_t *src,int itemCount, char *dst, size_t dstSize) {

  if ((itemCount % 4) || (dstSize % 5)) return;
  if (dstSize < ((itemCount*5)/4)+1) return;

  byte *from = (byte *)src;
  char *to = dst;
  for (size_t idx=0; idx<itemCount; idx+=4) {
      encodeBytes(from, 4, to, 6);
      from += 4;
      to += 5;
  }
}

// 2-byte values may need to have their bytes swapped

void encodeInt16(int16_t *src,int itemCount, char *dst, size_t dstSize) {}
void encodeUInt16(uint16_t *src,int itemCount, char *dst, size_t dstSize){}

// 4-byte values may need to have their bytes swapped

void encodeInt32(int32_t *src,int itemCount, char *dst, size_t dstSize){}
void encodeUInt32(uint32_t *src,int itemCount, char *dst, size_t dstSize){}

size_t decodeInt8(char *src, int8_t *dst, size_t maxItemCount){}
size_t decodeInt16(char *src, int16_t *dst, size_t maxItemCount){}
size_t decodeInt32(char *src, int32_t *dst, size_t maxItemCount){}

size_t decodeUInt8(char *src, uint8_t *dst, size_t maxItemCount){}
size_t decodeUInt16(char *src, uint16_t *dst, size_t maxItemCount){}
size_t decodeUInt32(char *src, uint32_t *dst, size_t maxItemCount){}

