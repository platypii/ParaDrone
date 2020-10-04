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
  // TODO: Switch to C++ style
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
 */
Path *path_fly_free(Path *path, double distance) {
  Path *flown = path_fly(path, distance);
  free_path(path);
  return flown;
}

Path *path_fly(Path *path, double distance) {
  if (!path) return NULL;
  if (isnan(distance)) {
    PRINTF("Error: path_fly nan %f\n", distance);
    return path;
  }

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
  Segment **trimmed = (Segment**) malloc((path->segment_count + 1) * sizeof(Segment*));
  for (int j = 0; j < i; j++) {
    // TODO: RAII for segments? Until then, copy
    trimmed[j] = segment_copy(path->segments[j]);
  }

  // Fly last segment
  Path *last = segment_fly(path->segments[i], distance - flown);
  trimmed[i++] = last->segments[0];
  if (last->segment_count == 2) {
    trimmed[i++] = last->segments[1];
  }

  Path *flight = new_path(path->name, i, trimmed);
  free(last); // Free path not segments
  free(trimmed);
  return flight;
}

double path_length(Path *path) {
  double len = 0;
  for (int i = 0; i < path->segment_count; i++) {
    len += segment_length(path->segments[i]);
  }
  return len;
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
