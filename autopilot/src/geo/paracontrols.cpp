#include "dtypes.h"

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
