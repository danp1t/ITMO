#include "../include/struct_image.h"
#include "../include/bmp_format.h"
#include "../include/file.h"
#include "../include/rotate.h"
#include "../include/transformation.h"



#include <stdio.h>
#include <stdlib.h>




int main(const int argc, char** argv ) {
    //Проверяем количество введенных параметров
    if (argc != 4) {
        fprintf(stderr, "Параметров должно быть ровно 4, а сейчас их %d\n", argc);
        exit(1);
    }


    //Парсим параметры в конкретные объекты
    char* source_image_path = argv[1]; //Путь к исходному файлу
    char* transformed_image_path = argv[2]; //Путь к файлу, который мы получим в результате преобразований
    char* tranformation_str = argv[3]; //Команда

    //Преобразуем строку в команду из enum
    enum transformation command = string_to_transformation(tranformation_str);

    //Читаем входной файл
    FILE* source_image = my_open(source_image_path, "r");
    if (source_image == NULL) { //Если не смогли прочитать файл, печатаем ошибку и выходим из программы
        fprintf(stderr, "ОШИБКА: Не смогли открыть файл: %s\n", source_image_path);
        exit(2);
    }


    struct image* img = malloc(sizeof(struct image));

    const enum read_status input_bmp =  from_bmp(source_image, img);
    if (input_bmp != 0) { //Если не смогли прочитать bmp файл.
        
        fprintf(stderr, "ОШИБКА: При чтении файла возникла ошибка: %s\n", source_image_path);
        exit(12);
    }
    //Закрываем исходный файл
    const int close_flag = my_close(source_image);
    if (close_flag != 0) {
        fprintf(stderr, "ОШИБКА: При закрытии файла возникла ошибка");
    }



    switch (command) {
        case none: {
            create_new_image(*img, transformed_image_path);
            break;
        }
        case cw90: {
            const struct image new_image = cw90_rotate(*img);
            create_new_image(new_image, transformed_image_path);
            free(new_image.data);
            break;
        }
        case ccw90: {
            const struct image new_image = ccw90_rotate(*img);
            create_new_image(new_image, transformed_image_path);
            free(new_image.data);
            break;
        }
        case fliph: {
            const struct image new_image = fliph_rotate(*img);
            create_new_image(new_image, transformed_image_path);
            free(new_image.data);
            break;
        }
        case flipv: {
            const struct image new_image = flipv_rotate(*img);
            create_new_image(new_image, transformed_image_path);
            free(new_image.data);
            break;
        }
        default: {
            fprintf(stderr, "Команда не найдена!");
            exit(1);
        }
    }

    free(img->data);
    free(img);

    return 0;
}
