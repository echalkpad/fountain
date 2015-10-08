 /* Functions to convert between host and network byte order.

   Please note that these functions normally take `unsigned long int' or
   `unsigned short int' values as arguments and also return them.  But
   this was a short-sighted decision since on different systems the types
   may have different representations but the values are always the same.  */

extern uint32_t ntohl (uint32_t __netlong) __THROW __attribute__ ((__const__));
extern uint16_t ntohs (uint16_t __netshort)
     __THROW __attribute__ ((__const__));
extern uint32_t htonl (uint32_t __hostlong)
     __THROW __attribute__ ((__const__));
extern uint16_t htons (uint16_t __hostshort)
     __THROW __attribute__ ((__const__));

#include <endian.h>

/* Get machine dependent optimized versions of byte swapping functions.  */
#include <bits/byteswap.h>

#ifdef __OPTIMIZE__
/* We can optimize calls to the conversion functions.  Either nothing has
   to be done or we are using directly the byte-swapping functions which
   often can be inlined.  */
# if __BYTE_ORDER == __BIG_ENDIAN
/* The host byte order is the same as network byte order,
   so these functions are all just identity.  */
# define ntohl(x)        (x)
# define ntohs(x)        (x)
# define htonl(x)        (x)
# define htons(x)        (x)
# else
#  if __BYTE_ORDER == __LITTLE_ENDIAN
#   define ntohl(x)        __bswap_32 (x)
#   define ntohs(x)        __bswap_16 (x)
#   define htonl(x)        __bswap_32 (x)
#   define htons(x)        __bswap_16 (x)
#  endif
# endif
#endif
