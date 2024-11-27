#include <stdio.h>

#ifndef BMP_FORMAT_H
#define BMP_FORMAT_H
enum read_status  {
    READ_OK = 0,
    READ_INVALID_SIGNATURE,
    READ_INVALID_BITS,
    READ_INVALID_HEADER,
    READ_NOT_MEMORY
    };

enum  write_status  {
    WRITE_OK = 0,
    WRITE_ERROR
  };

enum read_status from_bmp( FILE* in, struct image* img );
enum write_status to_bmp( FILE* out, struct image const* img );
void create_new_image(struct image new_image, char* transformed_image_path);

#endif
