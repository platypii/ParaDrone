#include <math.h>
#include <stdio.h>
#include "geo.h"

Path *new_path(Point start, Point end, uint8_t segment_count, Segment *segments[]) {
  // TODO: Switch to C++ style
  Path *path = (Path *) malloc(sizeof(Path) + segment_count * sizeof(Segment*));
  path->start = start;
  path->end = end;
  path->segment_count = segment_count;
  for (int i = 0; i < segment_count; i++) {
    path->segments[i] = segments[i];
  }
  return path;
}

ParaControls path_controls(Path *path) {
  ParaControls ctrl = {};
  Segment *segment = path->segments[0];
  if (segment->segment_type == 'T') {
    Turn *turn = (Turn *) segment;
    if (turn->turn == TURN_RIGHT) {
      ctrl.right = 1;
    } else {
      ctrl.left = 1;
    }
  }
  return ctrl;
}

Path *path_fly(Path *path, double distance) {
  double flown = 0;
  int i = 0;
  // Find last segment
  for (; i < path->segment_count - 1; i++) {
    const double len = segment_length(path->segments[i]);
    if (distance < flown + len) {
      // End point is within segment
      break;
    } else {
      flown += len;
    }
  }

  // Copy complete segments
  Segment *trimmed[path->segment_count + 1];
  for (int j = 0; j < i; j++) {
    trimmed[j] = path->segments[j];
  }

  // Fly last segment
  Path *last = segment_fly(path->segments[i], distance - flown);
  trimmed[i] = last->segments[0];
  if (last->segment_count == 2) {
    trimmed[++i] = last->segments[1];
  }
  return new_path(path->start, last->end, i, trimmed);
}

/**
 * Return the end point with velocity
 */
PointV *path_end(Path *path) {
  Segment *segment = path->segments[path->segment_count];
  if (segment->segment_type == 'L') {
    return line_end((Line*) segment);
  } else if (segment->segment_type == 'T') {
    return turn_end((Turn*) segment);
  } else {
    return NULL;
  }
}

Path *segment_fly(Segment *segment, double distance) {
  if (segment->segment_type == 'L') {
    return line_fly((Line*) segment, distance);
  } else if (segment->segment_type == 'T') {
    return turn_fly((Turn*) segment, distance);
  } else {
    return NULL;
  }
}

double segment_length(Segment *segment) {
  if (segment->segment_type == 'L') {
    return line_length((Line*) segment);
  } else if (segment->segment_type == 'T') {
    return turn_length((Turn*) segment);
  } else {
    return NAN;
  }
}

void free_path(Path *path) {
  // TODO: Free individual segments?
  free(path->segments);
  free(path);
}
