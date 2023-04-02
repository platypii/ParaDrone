#include <FS.h>
#include <SD.h>
#include <SPI.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <time.h>
#include "gtypes.h"

static File log_file;
static File get_file(struct tm *date);

int SD_CS = 0;
int SD_MOSI = 23;
int SD_MISO = 38;
int SD_SCLK = 5;

SPIClass sd_spi(HSPI);

void log_init() {
    sd_spi.begin(SD_SCLK, SD_MISO, SD_MOSI, SD_CS);
    if (!SD.begin(SD_CS, sd_spi)) {
        Serial.printf("sd mount failed\n");
        return;
    }
    uint8_t cardType = SD.cardType();
    if (cardType == CARD_NONE) {
        Serial.printf("sd card not found\n");
        return;
    }
}

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
  // Construct filename
  char filename[24] = "/";
  strftime(filename + 1, 12, "%Y-%m-%d", date);
  strcat(filename, ".csv");
  Serial.printf("%.1fs logging to %s\n", millis() * 1e-3, filename);
  // Open file for append writing
  return SD.open(filename, FILE_APPEND);
}

void stop_logging() {
  if (log_file) {
    log_file.close();
  }
}
