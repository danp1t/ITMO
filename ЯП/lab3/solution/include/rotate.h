//
// Created by danp1t on 24.11.2024.
//

#ifndef ROTATE_H
#define ROTATE_H

struct image cw90_rotate(struct image const source);
struct image ccw90_rotate(struct image const source);
struct image flipv_rotate(struct image const source);
struct image fliph_rotate(struct image const source);

#endif //ROTATE_H
