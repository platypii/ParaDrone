#include "landingzone.h"
#include "geo.h"
#include "plan.h"
#include <vector>
using namespace std;

int main() {
  Point3V loc = {
    .x = 370,
    .y = 264,
    .alt = 800,
    .vx = 3,
    .vy = 4,
    .climb = -1
  };

  LandingZone lz(47.239, -123.143, 84, 0.5585 /* 32 degrees */);
  double r = 100;

  Path *straight_path = straight(loc);
  straight_path = path_fly_free(straight_path, 10);
  free_path(straight_path);

  // Test naive planner
  Path *naive_path = naive(loc, lz.dest, r);
  naive_path = path_fly_free(naive_path, 10);
  naive_path = path_fly_free(naive_path, 20);
  naive_path = path_fly_free(naive_path, 0);
  free_path(naive_path);

  // Test shortest dubins
  Path *dub_path = shortest_dubins(loc, lz.dest, r);
  dub_path = path_fly_free(dub_path, 10);
  free_path(dub_path);

  // Test waypoint planner
  vector<Path*> waypoint_paths = via_waypoints(loc, &lz, r);
  for (unsigned i = 0; i < waypoint_paths.size(); i++) {
    free_path(waypoint_paths[i]);
  }

  return 0;
}
