#include "geo.h"

/**
 * Fly straight forever.
 */
Path *straight(PointV loc) {
  // Project velocity out
  Point dest = {
    x: loc.x + loc.vx,
    y: loc.y + loc.vy
  };
  Line *line = new Line {'L', {loc.x, loc.y}, dest};
  return new_path("str", 1, (Segment**) &line);
}
