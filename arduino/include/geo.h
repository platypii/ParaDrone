#ifndef _GEO_H
#define _GEO_H

#include "dtypes.h"

#include <stdint.h>
#include <stdlib.h>

// Math
double mod360(double degrees);
double to_degrees(double radians);
double to_radians(double degrees);

// Convert
void bearing2(char *str, double degrees);

double geo_bearing(double lat1, double lng1, double lat2, double lng2);
double geo_distance(double lat1, double lng1, double lat2, double lng2);

// Segments
PointV *segment_start(Segment *segment);
PointV *segment_end(Segment *segment);
Path *segment_fly(Segment *segment, double distance);
double segment_length(Segment *segment);

PointV *line_start(Line *line);
PointV *line_end(Line *line);
Path *line_fly(Line *line, double distance);
double line_length(Line *line);

PointV *turn_start(Turn *turn);
PointV *turn_end(Turn *turn);
Path *turn_fly(Turn *turn, double distance);
double turn_length(Turn *turn);

// Paths
Path *new_path(uint8_t segment_count, Segment *segments[]);
ParaControls path_controls(Path *path);
PointV *path_start(Path *path);
PointV *path_end(Path *path);
Path *path_fly(Path *path, double distance);
double path_length(Path *path);
void free_path(Path *path);

double flight_distance_remaining(double alt);

#endif
