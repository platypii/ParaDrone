#include <fcntl.h>
#include <stdio.h>
#include <termios.h>
#include <unistd.h>
#include "dtypes.h"

#define CHANNEL_LEFT 3
#define CHANNEL_RIGHT 5

// Use middle of actual range: [4000, 8000]
#define TARGET_MIN 5000
#define TARGET_MAX 7000
#define TARGET_RANGE (TARGET_MAX - TARGET_MIN)

// TODO: Save file descriptor?

int getConn();
int maestroSetTarget(int fd, unsigned char channel, unsigned short target);

/**
 * Interface to Pololu Micro Maestro USB servo controller
 */
void setControlsMaestro(struct ParaControls controls) {
  printf("Setting controls to left %f right %f\n", controls.left, controls.right);
  int fd = getConn();
  // TODO: Needs duration
  maestroSetTarget(fd, CHANNEL_LEFT, TARGET_MIN + controls.left * TARGET_RANGE);
  maestroSetTarget(fd, CHANNEL_RIGHT, TARGET_MIN + controls.right * TARGET_RANGE);
  close(fd);
}

/**
 * Gets the position of a maestro channel
 */
int maestroGetPosition(int fd, unsigned char channel) {
  unsigned char command[] = {0x90, channel};
  if (write(fd, command, sizeof(command)) == -1) {
    perror("error writing");
    return -1;
  }

  unsigned char response[2];
  if (read(fd, response, 2) != 2) {
    perror("error reading");
    return -1;
  }

  return response[0] + 256 * response[1];
}

/**
 * Sets the target of a maestro channel
 * @param target target speed in quarter-microseconds
 */
int maestroSetTarget(int fd, unsigned char channel, unsigned short target) {
  unsigned char command[] = {0x84, channel, target & 0x7F, target >> 7 & 0x7F};
  if (write(fd, command, sizeof(command)) == -1) {
    perror("error writing");
    return -1;
  }
  return 0;
}

/**
 * Return serial file descriptor with appropriate flags set
 */
int getConn() {
  // Open the Maestro's virtual COM port
  const char *device = "/dev/ttyACM0"; // Linux
  int fd = open(device, O_RDWR | O_NOCTTY);
  if (fd == -1) {
    perror(device);
    return -1;
  }

  // Linux set attributes
  struct termios options;
  tcgetattr(fd, &options);
  options.c_iflag &= ~(INLCR | IGNCR | ICRNL | IXON | IXOFF);
  options.c_oflag &= ~(ONLCR | OCRNL);
  options.c_lflag &= ~(ECHO | ECHONL | ICANON | ISIG | IEXTEN);
  tcsetattr(fd, TCSANOW, &options);

  return fd;
}

int maestro_main(int target) {
  int fd = getConn();

  printf("Getting position\n");
  int position = maestroGetPosition(fd, 5);
  printf("Current position is %d.\n", position);

  // int target = (position < 6000) ? 7000 : 5000;
  printf("Setting target to %d (%d us).\n", target, target / 4);
  maestroSetTarget(fd, 5, target);

  close(fd);
  return 0;
}
