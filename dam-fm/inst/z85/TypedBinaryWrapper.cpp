/**
 * The purpose of the functions defined here is to manage differences
 * in byte order (big-endian, little-endian) between two hosts.  Note
 * that the actual code represented by htons, ntohs, etc., is determined
 * at compile time.
 */

#include <string.h>
#include "SevenBitCodec.h"
#include "TypedBinaryWrapper.h"
#include "swappers.h"

// ---------------------------------------------------------------------------

// make sure defined sizes are exactly what we think they are

typedef char type_int8_t_static_assert[(sizeof(int8_t) * CHAR_BIT == 8) * 2 - 1];
typedef char type_uint8_t_static_assert[(sizeof(uint8_t) * CHAR_BIT == 8) * 2 - 1];

typedef char type_int16_t_static_assert[(sizeof(int16_t) * CHAR_BIT == 16) * 2 - 1];
typedef char type_uint16_t_static_assert[(sizeof(uint16_t) * CHAR_BIT == 16) * 2 - 1];

typedef char type_int32_t_static_assert[(sizeof(int32_t) * CHAR_BIT == 32) * 2 - 1];
typedef char type_uint32_t_static_assert[(sizeof(uint32_t) * CHAR_BIT == 32) * 2 - 1];

// ---------------------------------------------------------------------------

// single byte values are not affected by addressing issues

void encodeInt8(int8_t *src,int itemCount, char *dst, size_t dstSize) {
  encodeUInt8((uint8_t *)src,itemCount,dst,dstSize);
}

void encodeUInt8(uint8_t *src,int itemCount, char *dst, size_t dstSize) {

  if (itemCount % 4) return;
  if ((encodedSize(itemCount)+1) > dstSize ) return;

  byte *from = (byte *)src;
  char *to = dst;
  for (size_t idx=0; idx<itemCount; idx+=4) {
      encodeBytes(from, 4, to, 6);
      from += 4;
      to += 5;
  }
}

// 2-byte values may need to have their bytes swapped depending on host byte
// order.

void encodeInt16(int16_t *src,int itemCount, char *dst, size_t dstSize) {
  encodeUInt16((uint16_t *)src,itemCount,dst,dstSize);
}

void encodeUInt16(uint16_t *src,int itemCount, char *dst, size_t dstSize){
  uint16_t buf[2];

  size_t byteCount = 2 * itemCount;

  if (byteCount % 4) return;
  if ((encodedSize(byteCount)+1) > dstSize) return;

  uint16_t *from = src;
  char *to = dst;
  for (size_t idx=0; idx<itemCount; idx+=2) {
      buf[0] = htons(from[0]);
      buf[1] = htons(from[1]);
      encodeBytes((byte *)buf, 4, to, 6);
      from += 2;
      to += 5;
  }
}

// 4-byte values may need to have their bytes swapped depending on host byte
// order.

void encodeInt32(int32_t *src,int itemCount, char *dst, size_t dstSize){
    encodeUInt32((uint32_t *)src,itemCount,dst,dstSize);
}

void encodeUInt32(uint32_t *src,int itemCount, char *dst, size_t dstSize){
  uint32_t buf;

  size_t byteCount = 4 * itemCount;

  if (byteCount % 4) return;
  if ((encodedSize(byteCount)+1) > dstSize) return;

  uint32_t *from = src;
  char *to = dst;
  for (size_t idx=0; idx<itemCount; idx+=1) {
      buf = htonl(*from);
      encodeBytes((byte *)buf, 4, to, 6);
      from += 1;
      to += 5;
  }
}

// ---------------------------------------------------------------------------

size_t decodeInt8(char *src, int8_t *dst, size_t maxItemCount){
  return decodeUInt8(src,(uint8_t *)dst,maxItemCount);
}

size_t decodeUInt8(char *src, uint8_t *dst, size_t maxItemCount){

  if (strlen(src) % 5) return 0;
  if ((decodedSize(strlen(src))/(sizeof (uint8_t))) > maxItemCount) return 0;

  char *from = src;
  byte *to = (byte *)dst;
  for (size_t idx=0; idx<strlen(src); idx+=5) {
    decodeChars(from,5,to,4);
    from += 5;
    to += 4;
  }
  return to - dst;
}

size_t decodeInt16(char *src, int16_t *dst, size_t maxItemCount) {
    return decodeUInt16(src,(uint16_t *)dst,maxItemCount);
}

size_t decodeUInt16(char *src, uint16_t *dst, size_t maxItemCount) {
  uint16_t buf[2];

  if (strlen(src) % 5) return 0;
  if ((decodedSize(strlen(src))/(sizeof (uint16_t))) > maxItemCount) return 0;

  char *from = src;
  uint16_t *to = dst;
  for (size_t idx=0; idx<strlen(src); idx+=5) {
    decodeChars(from,5,(byte *)buf,4);
    to[0] = ntohs(buf[0]);
    to[1] = ntohs(buf[1]);
    from += 5;
    to += 2;
  }
  return to - dst;  // return item count, NOT byte count
}

size_t decodeInt32(char *src, int32_t *dst, size_t maxItemCount){
      return decodeUInt32(src,(uint32_t *)dst,maxItemCount);
}

size_t decodeUInt32(char *src, uint32_t *dst, size_t maxItemCount){
  uint32_t buf;

  if (strlen(src) % 5) return 0;
  if ((decodedSize(strlen(src))/(sizeof (uint32_t))) > maxItemCount) return 0;

  char *from = src;
  uint32_t *to = dst;
  for (size_t idx=0; idx<strlen(src); idx+=5) {
    decodeChars(from,5,(byte *)buf,4);
    to[0] = ntohl(buf);
    from += 5;
    to += 1;
  }
  return to - dst;  // return item count, NOT byte count
}
