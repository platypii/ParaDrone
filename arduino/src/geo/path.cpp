#include <math.h>
#include <stdio.h>
#include "geo.h"

Path *new_path(uint8_t segment_count, Segment *segments[]) {
  // TODO: Switch to C++ style
  Path *path = (Path *) malloc(sizeof(Path) + segment_count * sizeof(Segment*));
  path->start = *segment_start(segments[0]);
  path->end = *segment_end(segments[segment_count - 1]);
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
      ctrl.right = 127;
    } else {
      ctrl.left = 127;
    }
  }
  return ctrl;
}

Path *path_fly(Path *path, double distance) {
  if (!path) return NULL;

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
  return new_path(i, trimmed);
}

/**
 * Return the start point with velocity
 */
PointV *path_start(Path *path) {
  return segment_start(path->segments[0]);
}

/**
 * Return the end point with velocity
 */
PointV *path_end(Path *path) {
  return segment_start(path->segments[path->segment_count]);
}

double path_length(Path *path) {
  double len = 0;
  for (int i = 0; i < path->segment_count; i++) {
    len += segment_length(path->segments[i]);
  }
  return len;
}

void free_path(Path *path) {
  // TODO: Free individual segments?
  free(path->segments);
  free(path);
}
