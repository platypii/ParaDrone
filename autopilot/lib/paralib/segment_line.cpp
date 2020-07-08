#include <math.h>
#include <stdio.h>
#include "geo.h"

static double interpolate(double start, double end, double alpha);

PointV line_start(Line *line) {
  const double dx = line->end.x - line->start.x;
  const double dy = line->end.y - line->start.y;
  const double len = hypot(dx, dy);
  return PointV {line->start.x, line->start.y, dx / len, dy / len};
}

PointV line_end(Line *line) {
  const double dx = line->end.x - line->start.x;
  const double dy = line->end.y - line->start.y;
  const double len = hypot(dx, dy);
  return PointV {line->end.x, line->end.y, dx / len, dy / len};
}

double line_length(Line *line) {
  const double dx = line->start.x - line->end.x;
  const double dy = line->start.y - line->end.y;
  return hypot(dx, dy);
}

/**
 * Fly a given distance along the path
 */
Path *line_fly(Line *line, const double distance) {
  if (distance < 0) {
    printf("Flight distance cannot be negative %f", distance);
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

static double interpolate(double start, double end, double alpha) {
  return start + alpha * (end - start);
}