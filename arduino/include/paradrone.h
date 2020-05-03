#include "dtypes.h"

// Bluetooth
void bt_init();
void bt_notify(GeoPointV point);

// GPS
void read_gps();
void parse_nmea(char *line);
void update_location(GeoPointV point);

// Logger
void log_point(GeoPointV point);

// Math
double mod360(double degrees);
double to_degrees(double radians);
double to_radians(double degrees);

// Landing zone
void set_landing_zone(const char *packed);
