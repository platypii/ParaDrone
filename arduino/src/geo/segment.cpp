#include <math.h>
#include <stdio.h>
#include "geo.h"

PointV *segment_start(Segment *segment) {
  if (segment->segment_type == 'L') {
    return line_start((Line*) segment);
  } else if (segment->segment_type == 'T') {
    return turn_start((Turn*) segment);
  } else {
    return NULL;
  }
}

PointV *segment_end(Segment *segment) {
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
