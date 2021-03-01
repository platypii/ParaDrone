#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <time.h>
#include <SPIFFS.h>
#include "gtypes.h"

static File log_file;
static File get_file(GeoPointV *loc);

void log_point(GeoPointV *loc) {
  // Date string
  char dateStr[40];
  const time_t seconds = loc->millis / 1000;
  struct tm date = *localtime(&seconds);
  strftime(dateStr, sizeof(dateStr), "%Y-%m-%dT%H:%M:%SZ", &date);

  // Log to file
  if (!log_file) {
    log_file = get_file(loc);
    log_file.println("time,lat,lng,hMSL,velN,velE,velD");
  }
  log_file.printf("%s,%.6f,%.6f,%.2f,%.2f,%.2f,%.2f\n", dateStr, loc->lat, loc->lng, loc->alt, loc->vN, loc->vE, -loc->climb);
  log_file.flush();
}

static File get_file(GeoPointV *loc) {
  // Start SPIFFS file system
  if (!SPIFFS.begin(true)) {
    Serial.println("Error mounting SPIFFS");
  }

  // Construct filename
  char filename[24] = "/";
  const time_t seconds = loc->millis / 1000;
  struct tm *t = localtime(&seconds);
  strftime(filename + 1, 12, "%Y-%m-%d", t);
  strcat(filename, ".csv");
  printf("Logging to %s\n", filename);
  // Open file for append writing
  return SPIFFS.open(filename, "a");
}

void stop_logging() {
  if (log_file) {
    log_file.close();
  }
}
