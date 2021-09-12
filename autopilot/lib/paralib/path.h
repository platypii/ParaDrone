#ifndef _PATH_H
#define _PATH_H

#include "gtypes.h"

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
TogglePosition turn_controls(Turn *turn);

// Paths
Path *new_path(const char *name, uint8_t segment_count, Segment *segments[]);
Path *path_fly_free(Path *path, double distance);
double path_length(Path *path);
TogglePosition path_controls(Path *path);
void free_path(Path *path);

#endif
