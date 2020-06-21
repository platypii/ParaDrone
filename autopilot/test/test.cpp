#include "landingzone.h"
#include "geo.h"
#include "plan.h"

int main() {
  PointV loc = {
    .x = 370,
    .y = 264,
    .vx = 3,
    .vy = 4
  };

  Path *straight_path = straight(loc);
  straight_path = path_fly_free(straight_path, 10);
  free_path(straight_path);

  Path *naive_path = straight(loc);
  naive_path = path_fly_free(naive_path, 10);
  naive_path = path_fly_free(naive_path, 20);
  naive_path = path_fly_free(naive_path, 0);
  free_path(naive_path);

  return 0;
}
