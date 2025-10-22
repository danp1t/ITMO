#include <dirent.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/syscall.h>

#define BUF_SIZE 1024

struct linux_dirent {
    long d_ino;
    off_t d_off;
    unsigned short d_reclen;
    char d_name[];
};

int main() {
    int fd, nread;
    char buf[BUF_SIZE];
    struct linux_dirent *d;
    int bpos;
    char process_name[256];
    int count = 0;
    FILE *comm_file;

    fd = open("/proc", O_RDONLY | O_DIRECTORY);
    if (fd == -1) {
        perror("open");
        return 1;
    }

    while (1) {
        nread = syscall(SYS_getdents, fd, buf, BUF_SIZE);
        if (nread == -1) {
            perror("getdents");
            break;
        }

        if (nread == 0) {
            break;
        }

        for (bpos = 0; bpos < nread;) {
            d = (struct linux_dirent *)(buf + bpos);
            if (d->d_name[0] >= '0' && d->d_name[0] <= '9') {
                snprintf(process_name, sizeof(process_name), "/proc/%s/comm", d->d_name);
                comm_file = fopen(process_name, "r");
                if (comm_file) {
                    if (fgets(process_name, sizeof(process_name), comm_file)) {
                        process_name[strcspn(process_name, "\n")] = 0;
                        if (strcmp(process_name, "genenv") == 0) {
                            count++;
                        }
                    }
                    fclose(comm_file);
                }
            }
            bpos += d->d_reclen;
        }
    }

    close(fd);
    printf("%d\n", count);
    return 0;
}
