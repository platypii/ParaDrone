#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <time.h>
#include <SPIFFS.h>
#include "gtypes.h"

static File log_file;
static File get_file(struct tm *date);

void log_point(GeoPointV *loc) {
  // Date string
  char dateStr[40];
  const time_t seconds = loc->millis / 1000;
  struct tm *date = localtime(&seconds);
  strftime(dateStr, sizeof(dateStr), "%Y-%m-%dT%H:%M:%SZ", date);

  // Log to file
  if (!log_file) {
    log_file = get_file(date);
    log_file.println("time,lat,lng,hMSL,velN,velE,velD");
  }
  log_file.printf("%s,%.6f,%.6f,%.2f,%.2f,%.2f,%.2f\n", dateStr, loc->lat, loc->lng, loc->alt, loc->vN, loc->vE, -loc->climb);
  log_file.flush();
}

static File get_file(struct tm *date) {
  // Start SPIFFS file system
  if (!SPIFFS.begin(true)) {
    Serial.printf("%.1fs error mounting spiffs\n", millis() * 1e-3);
  }

  // Construct filename
  char filename[24] = "/";
  strftime(filename + 1, 12, "%Y-%m-%d", date);
  strcat(filename, ".csv");
  Serial.printf("%.1fs logging to %s\n", millis() * 1e-3, filename);
  // Open file for append writing
  return SPIFFS.open(filename, FILE_APPEND);

  // TODO: Check available space and delete oldest file if needed.
}

void stop_logging() {
  if (log_file) {
    log_file.close();
  }
}
