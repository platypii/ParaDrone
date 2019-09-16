#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "dtypes.h"

#define CONFIG_FILE "config.txt"

static void save_config(struct DConfig conf);
static struct LandingZone parse_landing_zone(const char *line);
static void render_landing_zone(char *str, struct LandingZone lz);

/**
 * Load config from disk
 */
struct DConfig load_config() {
  struct DConfig conf = {0};
  conf.lzs = malloc(128 * sizeof(struct LandingZone));

  FILE *fp = fopen(CONFIG_FILE, "r");
  if (fp) {
    char line[128];

    while (fgets(line, 128, fp)) {
      if (strncmp(line, "berry_gps = ", 12) == 0) {
        conf.berry_gps = line[12] == '1';
      } else if (strncmp(line, "landing = ", 10) == 0) {
        conf.lzs[conf.lz_count++] = parse_landing_zone(line + 10);
      }
    }

    fclose(fp);
  }
  return conf;
}

void free_config(struct DConfig conf) {
  free(conf.lzs);
}

/**
 * Save berry gps detected flag to config file
 * @param berry_gps flag indicating that we should use berry gps module
 */
void save_berry(int berry_gps) {
  struct DConfig conf = load_config();
  conf.berry_gps = berry_gps;
  save_config(conf);
  free_config(conf);
}

/**
 * Save config file to disk
 */
static void save_config(struct DConfig conf) {
  FILE *fp = fopen(CONFIG_FILE, "w+");
  if (fp) {
    fprintf(fp, "berry_gps = %d\n", conf.berry_gps);

    char line[128] = "landing = ";
    for (int i = 0; i < conf.lz_count; i++) {
      render_landing_zone(line + 10, conf.lzs[i]);
    }

    fclose(fp);
  }
}

/**
 * Parse landing zone
 * lat, lng, alt, bearing
 */
static struct LandingZone parse_landing_zone(const char *line) {
  struct LandingZone lz = {0};
  sscanf(line, "%lf, %lf, %lf, %lf", &lz.destination.lat, &lz.destination.lng, &lz.destination.alt, &lz.landingDirection);
  return lz;
}

/**
 * Render landing zone
 * lat, lng, alt, bearing
 */
static void render_landing_zone(char *str, struct LandingZone lz) {
  sprintf(str, "%lf, %lf, %lf, %lf", lz.destination.lat, lz.destination.lng, lz.destination.alt, lz.landingDirection);
}
