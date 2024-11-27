#include <string.h>

enum transformation {
  none,
  cw90,
  ccw90,
  fliph,
  flipv,
  TRANSFORMATION_COUNT = 5 // Количество команд
};


enum transformation string_to_transformation(const char* str) {
  if (strcmp(str, "none") == 0) return none;
  if (strcmp(str, "cw90") == 0) return cw90;
  if (strcmp(str, "ccw90") == 0) return ccw90;
  if (strcmp(str, "fliph") == 0) return fliph;
  if (strcmp(str, "flipv") == 0) return flipv;
  return TRANSFORMATION_COUNT;
}
