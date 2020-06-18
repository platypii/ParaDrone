#include <math.h>
#include <stdio.h>
#include "geo.h"

PointV segment_start(Segment *segment) {
  if (segment->segment_type == 'L') {
    return line_start((Line*) segment);
  } else if (segment->segment_type == 'T') {
    return turn_start((Turn*) segment);
  } else {
    printf("Error: invalid type in segment_start %c\n", segment->segment_type);
    return PointV {};
  }
}

PointV segment_end(Segment *segment) {
  if (segment->segment_type == 'L') {
    return line_end((Line*) segment);
  } else if (segment->segment_type == 'T') {
    return turn_end((Turn*) segment);
  } else {
    printf("Error: invalid type in segment_end %c\n", segment->segment_type);
    return PointV {};
  }
}

Path *segment_fly(Segment *segment, double distance) {
  if (segment->segment_type == 'L') {
    return line_fly((Line*) segment, distance);
  } else if (segment->segment_type == 'T') {
    return turn_fly((Turn*) segment, distance);
  } else {
    printf("Error: invalid type in segment_fly %c\n", segment->segment_type);
    return NULL;
  }
}

double segment_length(Segment *segment) {
  if (segment->segment_type == 'L') {
    return line_length((Line*) segment);
  } else if (segment->segment_type == 'T') {
    return turn_length((Turn*) segment);
  } else {
    printf("Error: invalid type in segment_length %c\n", segment->segment_type);
    return NAN;
  }
}

Segment *segment_copy(Segment *segment) {
  if (segment->segment_type == 'L') {
    Line *line = (Line*) segment;
    return (Segment*) new Line {'L', line->start, line->end};
  } else if (segment->segment_type == 'T') {
    Turn *turn = (Turn*) segment;
    return (Segment*) new Turn {'T', turn->start, turn->end, turn->circle, turn->turn};
  } else {
    printf("Error: invalid type in segment_copy %c\n", segment->segment_type);
    return NULL;
  }
}
