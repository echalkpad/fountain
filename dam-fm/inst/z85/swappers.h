#ifndef SWAPPERS_H
#define SWAPPERS_H

#include <stdint.h>

#if __BYTE_ORDER__ == __ORDER_BIG_ENDIAN__
/* The host byte order is the same as network byte order,
   so these functions are all just identity.  */
# define ntohl(x)        (x)
# define ntohs(x)        (x)
# define htonl(x)        (x)
# define htons(x)        (x)
# elif __BYTE_ORDER == __LITTLE_ENDIAN
#   define ntohl(x)        byteSwap32 (x)
#   define ntohs(x)        byteSwap16 (x)
#   define htonl(x)        byteSwap32 (x)
#   define htons(x)        byteSwap16 (x)
# else
# error "Unrecognized __BYTE_ORDER value."
# endif

uint16_t byteSwap16(uint16_t a);
uint32_t byteSwap32(uint32_t a);

#endif
