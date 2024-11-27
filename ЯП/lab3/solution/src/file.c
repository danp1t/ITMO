#include <stdio.h>

FILE* my_open(const char* filename, const char* mode) {
  FILE* file = fopen(filename, mode);
  return file;
}

int my_close(FILE *fp) {
  return fclose(fp);
}


