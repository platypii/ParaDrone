#include <stdio.h>
#include <string.h>
#include "dtypes.h"

static void run();

int main(int argc, char *argv[]) {
  if (argc == 2 && strcmp(argv[1], "test") == 0) {
    printf("DubFlight self test\n");
    // test();
  } else {
    run();
  }
  return 0;
}

static void run() {
  struct DConfig conf = load_config();
  if (conf.berry_gps) {
    printf("BerryGPS enabled\n");
    // berry_start();
  } else {
    // // Check if BerryGPS is present
    // if (berry_check()) {
    //   printf("BerryGPS found\n");
    //   // Save for faster start up
    //   save_berry(1);
    //   berry_start();
    // } else {
    //   printf("BerryGPS not found\n");
    // }
  }
  free_config(conf);
}

/**
 * Called when GPS location is updated
 */
void update_location(struct GeoPointV point) {
  log_point(point);
  // TODO: Plan and update controls
}
