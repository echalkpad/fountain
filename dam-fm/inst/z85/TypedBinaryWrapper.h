
#ifndef TYPED_BINARY_WRAPPER_H
#define TYPED_BINARY_WRAPPER_H

#include <stdint.h>

// declare a function to handle each supported binary integer type

void encodeInt8(int8_t *src,int itemCount, char *dst, size_t dstSize);
void encodeInt16(int16_t *src,int itemCount, char *dst, size_t dstSize);
void encodeInt32(int32_t *src,int itemCount, char *dst, size_t dstSize);

void encodeUInt8(uint8_t *src,int itemCount, char *dst, size_t dstSize);
void encodeUInt16(uint16_t *src,int itemCount, char *dst, size_t dstSize);
void encodeUInt32(uint32_t *src,int itemCount, char *dst, size_t dstSize);

size_t decodeInt8(char *src, int8_t *dst, size_t maxItemCount);
size_t decodeInt16(char *src, int16_t *dst, size_t maxItemCount);
size_t decodeInt32(char *src, int32_t *dst, size_t maxItemCount);

size_t decodeUInt8(char *src, uint8_t *dst, size_t maxItemCount);
size_t decodeUInt16(char *src, uint16_t *dst, size_t maxItemCount);
size_t decodeUInt32(char *src, uint32_t *dst, size_t maxItemCount);

#endif
