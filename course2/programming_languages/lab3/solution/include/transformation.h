#ifndef TRANSFORMATION_H
#define TRANSFORMATION_H

enum transformation {
    none,
    cw90,
    ccw90,
    fliph,
    flipv
  };

enum transformation string_to_transformation(const char* str);

#endif

