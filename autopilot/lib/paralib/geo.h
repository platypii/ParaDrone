#ifndef _GEO_H
#define _GEO_H

#include <stdint.h>
#include <stdlib.h>
#include "gtypes.h"

// Math
double mod360(double degrees);
double to_degrees(double radians);
double to_radians(double degrees);

// Convert
void bearing2(char *str, double degrees);

double geo_bearing(double lat1, double lng1, double lat2, double lng2);
double geo_distance(double lat1, double lng1, double lat2, double lng2);

// Segments
PointV segment_start(Segment *segment);
PointV segment_end(Segment *segment);
Path *segment_fly(Segment *segment, const double distance);
double segment_length(Segment *segment);
Segment *segment_copy(Segment *segment);

PointV line_start(Line *line);
PointV line_end(Line *line);
Path *line_fly(Line *line, const double distance);
double line_length(Line *line);

PointV turn_start(Turn *turn);
PointV turn_end(Turn *turn);
Path *turn_fly(Turn *turn, const double distance);
double turn_length(Turn *turn);

// Paths
Path *new_path(const char *name, uint8_t segment_count, Segment *segments[]);
Path *path_fly(Path *path, double distance);
Path *path_fly_free(Path *path, double distance);
double path_length(Path *path);
void free_path(Path *path);

// Planning
Path *straight(PointV loc);
Path *naive(PointV loc, PointV dest, const double r);

double flight_distance_remaining(const double alt);

#endif
