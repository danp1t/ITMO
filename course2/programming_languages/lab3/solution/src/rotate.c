#include "../include/struct_image.h"

#include <malloc.h>

struct image ccw90_rotate(struct image const input_image) {
    struct image new_image;
    new_image.width = input_image.height;
    new_image.height = input_image.width;

    new_image.data = malloc(new_image.width * new_image.height * sizeof(struct pixel));

    for (uint32_t y = 0; y < input_image.height; y++) {
        for (uint32_t x = 0; x < input_image.width; x++) {
            uint32_t new_x = input_image.height - y - 1;
            uint32_t new_y = x;
            new_image.data[(new_y * new_image.width) + new_x] =  input_image.data[(y * input_image.width) + x];
        }
    }

    return new_image;
}

struct image cw90_rotate(struct image const input_image) {
    struct image new_image;
    new_image.width = input_image.height;
    new_image.height = input_image.width;

    new_image.data = malloc(new_image.width * new_image.height * sizeof(struct pixel));

    for (uint32_t y = 0; y < input_image.height; y++) {
        for (uint32_t x = 0; x < input_image.width; x++) {
            uint32_t new_x = y;
            uint32_t new_y = input_image.width - x - 1;
            new_image.data[(new_y * new_image.width) + new_x] = input_image.data[(y * input_image.width) + x];
        }
    }

    return new_image;
}

struct image fliph_rotate(struct image const input_image) {
    struct image new_image;
    new_image.width = input_image.width;
    new_image.height = input_image.height;

    new_image.data = malloc(new_image.width * new_image.height * sizeof(struct pixel));

    for (uint32_t y = 0; y < input_image.height; y++) {
        for (uint32_t x = 0; x < input_image.width; x++) {
            uint32_t new_x = input_image.width - x - 1;
            new_image.data[(y * new_image.width) + new_x] = input_image.data[(y * input_image.width) + x];
        }
    }

    return new_image;
}


struct image flipv_rotate(struct image const input_image) {
    struct image new_image;
    new_image.width = input_image.width;
    new_image.height = input_image.height;

    new_image.data = malloc(new_image.width * new_image.height * sizeof(struct pixel));

    for (uint32_t y = 0; y < input_image.height; y++) {
        for (uint32_t x = 0; x < input_image.width; x++) {
            uint32_t new_y = input_image.height - y - 1;
            new_image.data[(new_y * new_image.width) + x] = input_image.data[(y * input_image.width) + x];
        }
    }

    return new_image;
}
