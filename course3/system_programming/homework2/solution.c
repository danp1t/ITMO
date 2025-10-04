#include <stdio.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdlib.h>
#include <dlfcn.h>

int (*function)(int);

bool init_library(const char * name_library, const char * name_function){
	void *hdl = dlopen(name_library, RTLD_LAZY);
	if (NULL == hdl)
		return false;
	
	function = (int (*)(int)) dlsym(hdl, name_function);

	if (NULL == function)
		return false;

	return true;
}

int main(int argc, char *argv[]) {
	if (init_library(argv[1], argv[2])){
		int number = atoi(argv[3]);
		int answer = function(number);
		printf("%d\n", answer);
	}
	return 0;
  
}
