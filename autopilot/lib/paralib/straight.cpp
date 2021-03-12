#include "path.h"

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
  return new_path("Str", 1, (Segment**) &line);
}
