#include <math.h>
#include <stdio.h>
#include "path.h"

static double interpolate(double start, double end, double alpha);

PointV line_start(Line *line) {
  const double dx = line->end.x - line->start.x;
  const double dy = line->end.y - line->start.y;
  const double len = sqrt(dx * dx + dy * dy);
  return PointV {line->start.x, line->start.y, dx / len, dy / len};
}

PointV line_end(Line *line) {
  const double dx = line->end.x - line->start.x;
  const double dy = line->end.y - line->start.y;
  const double len = sqrt(dx * dx + dy * dy);
  return PointV {line->end.x, line->end.y, dx / len, dy / len};
}

double line_length(Line *line) {
  const double dx = line->start.x - line->end.x;
  const double dy = line->start.y - line->end.y;
  return sqrt(dx * dx + dy * dy);
}

/**
 * Fly a given distance along the path
 */
Path *line_fly(Line *line, const double distance) {
  if (distance < 0) {
    printf("segment_line distance must be positive %f\n", distance);
  }
  // Linear interpolate
  const double alpha = distance / line_length(line);
  Point proj = {
    interpolate(line->start.x, line->end.x, alpha),
    interpolate(line->start.y, line->end.y, alpha)
  };
  Line *extension = new Line {'L', line->start, proj};
  return new_path("line-fly", 1, (Segment**) &extension);
}

Point *line_render(Line *line) {
  return &line->start;
}

/**
 * Linear interpolation
 * @param alpha interpolation parameter from 0 (start) to 1 (end)
 */
static double interpolate(double start, double end, double alpha) {
  return start + alpha * (end - start);
}
