
#ifndef Z85Typed_h
#define Z85Typed_h

// make sure defined sizes are exactly what we think they are

typedef char Z85_int8_t_static_assert[(sizeof(int8_t) * CHAR_BIT == 8) * 2 - 1];
typedef char Z85_uint8_t_static_assert[(sizeof(uint8_t) * CHAR_BIT == 8) * 2 - 1];

typedef char Z85_int16_t_static_assert[(sizeof(int16_t) * CHAR_BIT == 16) * 2 - 1];
typedef char Z85_uint16_t_static_assert[(sizeof(uint16_t) * CHAR_BIT == 16) * 2 - 1];

typedef char Z85_int32_t_static_assert[(sizeof(int32_t) * CHAR_BIT == 32) * 2 - 1];
typedef char Z85_uint32_t_static_assert[(sizeof(uint32_t) * CHAR_BIT == 32) * 2 - 1];

// htons()
// htonl()
// ntohs()
// ntohl()

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
