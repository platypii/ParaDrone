#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <time.h>
#include "dtypes.h"

static FILE *fp = NULL;

static FILE *get_file();

void log_point(struct GeoPointV loc) {
  // Log to screen
  char dateStr[40];
  time_t seconds = loc.millis / 1000;
  struct tm date = *localtime(&seconds);
  strftime(dateStr, sizeof(dateStr), "%Y-%m-%d %H:%M:%S", &date);
  printf("%s, %.6f, %.6f, %.2fm\n", dateStr, loc.lat, loc.lng, loc.alt);

  // Log to file
  if (!fp) {
    fp = get_file();
    fputs("millis,lat,lng,alt,vN,vE\n", fp);
  }
  fprintf(fp, "%lld,%.6f,%.6f,%.2f,%.2f,%.2f\n", loc.millis, loc.lat, loc.lng, loc.alt, loc.vN, loc.vE);
  fflush(fp);
}

static FILE *get_file() {
  // Construct filename
  char filename[20];
  strcpy(filename, "log/");
  time_t now = time(NULL);
  struct tm *t = localtime(&now);
  strftime(filename + 4, 12, "%Y-%m-%d", t);
  strcat(filename, ".csv");
  printf("Logging to %s\n", filename);
  // Make dir if needed
  struct stat st = {0};
  if (stat("log", &st) == -1) {
    mkdir("log", 0700);
  }
  // Open file for append writing
  return fopen(filename, "a");
}

void stop_logging() {
  if (fp) {
    fclose(fp);
  }
}
