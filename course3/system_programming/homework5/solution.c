#include <stdio.h>
#include <stdlib.h>
#include <sys/ipc.h>
#include <sys/shm.h>

#define ARRAY_SIZE 100
#define SHM_SIZE 1000

int main(int argc, char *argv[]) {
	int i;
	key_t key1, key2, result_key;
	int shmid1, shmid2, result_shmid;
	int *data1, *data2, *result_data;

	if (argc != 3) {
		fprintf(stderr, "Usage: %s <key1> <key2>\n", argv[0]);
		exit(1);
	}

	key1 = atoi(argv[1]);
	key2 = atoi(argv[2]);

	result_key = ftok(".", 'R');
	if (result_key == -1) {
		perror("ftok");
		exit(1);
	}
	shmid1 = shmget(key1, SHM_SIZE, 0666);
	shmid2 = shmget(key2, SHM_SIZE, 0666);

	if (shmid1 == -1 || shmid2 == -1) {
		perror("shmget");
		exit(1);
	}

	data1 = (int*)shmat(shmid1, NULL, 0);
	data2 = (int*)shmat(shmid2, NULL, 0);

	if (data1 == (int*)-1 || data2 == (int*)-1) {
		perror("shmat");
		exit(1);
	}

	result_shmid = shmget(result_key, SHM_SIZE, IPC_CREAT | 0666);
	if (result_shmid == -1) {
		perror("shmget result");
		exit(1);
	}

	result_data = (int*)shmat(result_shmid, NULL, 0);
	if (result_data == (int*)-1) {
		perror("shmat result");
		exit(1);
	}

	for (i = 0; i < ARRAY_SIZE; i++) {
		result_data[i] = data1[i] + data2[i];
	}

	printf("%d\n", result_key);

	shmdt(data1);
	shmdt(data2);
	shmdt(result_data);

	return 0;
}
