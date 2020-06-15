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
  Line line = {'L', {loc.x, loc.y}, dest};
  Segment *segment = (Segment *) &line;
  return new_path(1, &segment);
}
