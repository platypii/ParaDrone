#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include "path.h"

#ifdef UNIT_TEST
#define PRINTF printf
#else
#include <Arduino.h>
#define PRINTF Serial.printf
#endif

/**
 * Construct a new path object.
 */
Path *new_path(const char *name, uint8_t segment_count, Segment *segments[]) {
  if (segment_count <= 0) {
    PRINTF("Error: new_path segment_count %d\n", segment_count);
    return NULL;
  }
  // C style malloc for variable length struct
  Path *path = (Path*) malloc(sizeof(Path) + segment_count * sizeof(Segment*));
  if (!path) {
    PRINTF("Error: new_path malloc failed %p\n", path);
    return NULL;
  }
  path->name = name;
  path->start = segment_start(segments[0]);
  path->end = segment_end(segments[segment_count - 1]);
  // Copy segments to struct
  path->segment_count = segment_count;
  for (int i = 0; i < segment_count; i++) {
    path->segments[i] = segments[i];
  }
  return path;
}

/**
 * Fly the path a given distance, and then free the original.
 * This is helpful for flying a path in place.
 * Consumes the passed path.
 */
Path *path_fly_free(Path *path, double distance) {
  if (!path) return NULL;
  if (!(distance > 0)) {
    PRINTF("path fly distance must be positive %f\n", distance);
    return path;
  }

  double flown = 0;
  int i = 0;
  Segment *trimmed[path->segment_count + 1];
  for (; i < path->segment_count - 1; i++) {
    const double len = segment_length(path->segments[i]);
    if (distance < flown + len) {
      // End is within segment
      break;
    } else {
      // Copy whole segment
      trimmed[i] = path->segments[i];
      flown += len;
    }
  }

  const double remaining = distance - flown;
  if (remaining > 0) {
    // Fly last segment
    Path *last = segment_fly(path->segments[i], distance - flown);
    // Free last segment
    delete path->segments[i];
    // Concat last segment(s)
    trimmed[i++] = last->segments[0];
    // A turn can become a turn plus a line
    if (last->segment_count == 2) {
      trimmed[i++] = last->segments[1];
    }
    // Free last path, segments have been consumed
    free(last);
  } else if (remaining < 0) {
    PRINTF("segment_fly distance must be positive %f - %f < 0\n", distance, flown);
  }

  // Construct new path
  Path *flight = new_path(path->name, i, trimmed);

  // Free remaining segments of previous path
  for (; i < path->segment_count; i++) {
    delete path->segments[i];
  }
  free(path);

  return flight;
}

double path_length(Path *path) {
  double len = 0;
  for (int i = 0; i < path->segment_count; i++) {
    len += segment_length(path->segments[i]);
  }
  return len;
}

ParaControls path_controls(Path *path) {
  Segment *segment = path->segments[0];
  if (segment->segment_type == 'T') {
    Turn *turn = (Turn *) segment;
    return turn_controls(turn);
  } else {
    // Straight
    ParaControls ctrl = {};
    return ctrl;
  }
}

void free_path(Path *path) {
  if (path) {
    // Free segments
    for (int i = 0; i < path->segment_count; i++) {
      delete path->segments[i];
    }
    free(path);
  }
}
