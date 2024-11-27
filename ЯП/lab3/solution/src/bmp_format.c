#include <malloc.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>


#include "../include/file.h"
#include "../include/struct_image.h"



struct __attribute__((packed)) bmp_header
{
    uint16_t bfType;
    uint32_t  bfileSize;
    uint32_t bfReserved;
    uint32_t bOffBits;
    uint32_t biSize;
    uint32_t biWidth;
    uint32_t  biHeight;
    uint16_t  biPlanes;
    uint16_t biBitCount;
    uint32_t biCompression;
    uint32_t biSizeImage;
    uint32_t biXPelsPerMeter;
    uint32_t biYPelsPerMeter;
    uint32_t biClrUsed;
    uint32_t  biClrImportant;
};

enum read_status  {
    READ_OK = 0,
    READ_INVALID_SIGNATURE,
    READ_INVALID_BITS, // Ошибка, когда у нас не получается прочитать изображение из-за битых символов
    READ_INVALID_HEADER, //Если не удается прочитать header
    READ_NOT_MEMORY // Не получилось выделить память
    };

enum read_status from_bmp(FILE* input_file, struct image* img) {
    struct bmp_header header;

    if (fread(&header, sizeof(header), 1, input_file) != 1) {
        return READ_INVALID_HEADER;
    }

    if (header.bfType != 0x4D42) { // Данный параметр проверяет, что это точно bmp file
        return READ_INVALID_SIGNATURE;
    }

    img -> width = header.biWidth;
    img -> height = header.biHeight;

    img->data = malloc(img->width * img->height * sizeof(struct pixel));
    if (!img->data) { //Если не удалось выделить память
        return READ_NOT_MEMORY;
    }

    fseek(input_file, header.bOffBits, SEEK_SET);

    for (int32_t y = 0; y < (img->height); y++) { //Высота
        for (int32_t x = 0; x < (img->width); x++) { //Ширина
            struct pixel p;
            if (fread(&p, sizeof(struct pixel), 1, input_file) != 1) {
                free(img->data);
                return READ_INVALID_BITS;
            }
            img->data[y * (img->width) + x] = p; //Построчно считываем изображение
        }

        int width_in_bytes = (int)(img->width * sizeof(struct pixel));
        int remainder = width_in_bytes % 4;
        int padding = (remainder == 0) ? 0 : (4 - remainder);
        fseek(input_file, padding, SEEK_CUR);
    }

    return READ_OK;
}


/*  serializer   */
enum  write_status  {
    WRITE_OK = 0,
    WRITE_ERROR
    /* коды других ошибок  */
  };

enum write_status to_bmp(FILE* out, struct image const* img) {
    if (!img || !img->data) { //Если хотя бы один указатель NULL, то выходим с ошибкой
        return WRITE_ERROR;
    }

    struct bmp_header header;
    header.bfType = 0x4D42;
    header.bfileSize = sizeof(struct bmp_header) + ((img->width) * (img->height) * sizeof(struct pixel));
    header.bfReserved = 0; //default
    header.bOffBits = sizeof(struct bmp_header);
    header.biSize = 40;
    header.biWidth = img->width;
    header.biHeight = img->height;
    header.biPlanes = 1; //Всегда для bmp
    header.biBitCount = 24; //Всегда (По ТЗ)
    header.biCompression = 0;
    header.biSizeImage = 0;
    header.biXPelsPerMeter = 0;
    header.biYPelsPerMeter = 0;
    header.biClrUsed = 0;
    header.biClrImportant = 0;

    if (fwrite(&header, sizeof(header), 1, out) != 1) {
        return WRITE_ERROR;
    }

    for (int32_t y = 0; y < (img->height); y++) {
        for (int32_t x = 0; x < (img->width); x++) {
            struct pixel p = img->data[y * (img->width) + x];
            if (fwrite(&p, sizeof(struct pixel), 1, out) != 1) {
                return WRITE_ERROR;
            }
        }

        int width_in_bytes = (int)(img->width * sizeof(struct pixel));
        int remainder = width_in_bytes % 4;
        int padding = (remainder == 0) ? 0 : (4 - remainder);
        for (int i = 0; i < padding; i++) {
            fputc(0, out); // Заполняем паддинг нулями
        }
    }

    return WRITE_OK;
}

void create_new_image(struct image new_image, char* transformed_image_path){
    FILE* transformed_image = my_open(transformed_image_path, "w");
    enum write_status message =  to_bmp(transformed_image, &new_image);
    my_close(transformed_image);
    if (message != 0) {
      	fprintf(stderr, "ОШИБКА: При преобразовании файла возникла ошибка!");
        exit(1);
    }

}
