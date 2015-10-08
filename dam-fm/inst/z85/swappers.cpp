#include "swappers.h"

uint16_t byteSwap16(uint16_t a) {
  return ((a & 0xFF) << 8) | ((a & 0xFF00) >> 8);
}

uint32_t byteSwap32(uint32_t a) {
  return
    ((a & 0xFF) << 24) |
    ((a & 0xFF00) << 8) |
    ((a & 0xFF0000) >> 8) |
    ((a & 0xFF000000) >> 24);
}
