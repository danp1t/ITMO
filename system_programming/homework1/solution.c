#include <stddef.h>
#include "solution.h"
#include <string.h>

int stringStat(const char *string, size_t multiplier, int *count){
	size_t length = strlen(string);
	(*count)++; 
	return length * multiplier;
}
