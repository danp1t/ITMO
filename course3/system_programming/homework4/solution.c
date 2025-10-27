#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <ctype.h>

#define MAX_PID 100000

struct child_node {
    int pid;
    struct child_node *next;
};

int main(int argc, char *argv[]) {

    int target_pid = atoi(argv[1]);
    char filename[256];
    sprintf(filename, "/proc/%d/stat", target_pid);
    FILE *f = fopen(filename, "r");
    if (f == NULL) {
        printf("0\n");
        return 0;
    }
    fclose(f);

    // Инициализируем массив детей
    struct child_node *children[MAX_PID] = {NULL};

    // Сканируем /proc для построения дерева процессов
    DIR *dir = opendir("/proc");
    if (dir == NULL) {
        perror("opendir");
        return 1;
    }

    struct dirent *entry;
    while ((entry = readdir(dir)) != NULL) {
        // Проверяем, что это директория с числовым именем (PID)
        if (entry->d_type == DT_DIR) {
            char *name = entry->d_name;
            int is_digit = 1;
            for (int i = 0; name[i] != '\0'; i++) {
                if (!isdigit(name[i])) {
                    is_digit = 0;
                    break;
                }
            }

            if (is_digit) {
                int pid = atoi(name);
                sprintf(filename, "/proc/%d/stat", pid);
                FILE *f = fopen(filename, "r");
                if (f != NULL) {
                    int p, ppid;
                    char comm[100];
                    char state;

                    if (fscanf(f, "%d (%[^)]) %c %d", &p, comm, &state, &ppid) == 4) {
                        if (ppid < MAX_PID && ppid >= 0) {
                            struct child_node *new_node = malloc(sizeof(struct child_node));
                            new_node->pid = p;
                            new_node->next = children[ppid];
                            children[ppid] = new_node;
                        }
                    }
                    fclose(f);
                }
            }
        }
    }
    closedir(dir);

    int count = 0;
    int queue[MAX_PID];
    int front = 0
    int rear = 0;

    queue[rear++] = target_pid;

    while (front < rear) {
        int current = queue[front++];
        count++;

        if (current < MAX_PID) {
            struct child_node *child = children[current];
            while (child != NULL) {
                queue[rear++] = child->pid;
                child = child->next;
            }
        }
    }

    printf("%d\n", count);

    for (int i = 0; i < MAX_PID; i++) {
        struct child_node *current = children[i];
        while (current != NULL) {
            struct child_node *next = current->next;
            free(current);
            current = next;
        }
    }

    return 0;
}
