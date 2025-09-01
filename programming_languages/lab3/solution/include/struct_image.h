#ifndef STRUCT_IMAGE_H
#define STRUCT_IMAGE_H
#include <stdint.h>

struct image{
    uint64_t width;
    uint64_t height;
    struct pixel* data;
};

struct pixel { uint8_t b, g, r; };

#endif
