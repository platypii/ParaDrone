#include <fcntl.h>
#include <stdio.h>
#include <sys/select.h>
#include <sys/stat.h>
#include <unistd.h>
#include "dtypes.h"

/**
 * Return true if BerryGPS is detected
 */
int berry_check() {
  // Check for data coming in on serial port, with timeout
  fd_set set;
  int fd = open("/dev/ttyS0", O_RDWR);
  FD_ZERO(&set); // clear the set
  FD_SET(fd, &set); // add our file descriptor to the set

  struct timeval timeout;
  timeout.tv_sec = 0;
  timeout.tv_usec = 1001000; // 1.001 seconds

  int status = select(fd + 1, &set, NULL, NULL, &timeout);
  int result = 0;
  // status -1 = error, 0 = timeout
  if (status > 0) {
    // data is available to read
    char buffer[255];
    int bytes = read(fd, buffer, 255);
    result = bytes > 0;
  }
  close(fd);
  return result;
}

/**
 * Start listening for GPS updates
 */
void berry_start() {
  // Listen to serial port
  FILE *fp = fopen("/dev/serial0", "r");

  if (fp) {
    // Read loop
    char buffer[255];
    while (fgets(buffer, 255, fp)) {
      parseNMEA(buffer);
    }
    fclose(fp);
  } else {
    printf("Failed to open serial port\n");
  }
}
