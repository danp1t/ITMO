#include <stdio.h>

#ifndef FILE_H
#define FILE_H

FILE* my_open(const char* filename, const char* mode);
int my_close(FILE *fp);

#endif
