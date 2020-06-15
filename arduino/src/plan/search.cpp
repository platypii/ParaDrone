#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <vector>
#include "paradrone.h"
#include "plan.h"
using namespace std;

static Path* best_plan(LandingZone *lz, vector<Path*> plans);

/**
 * Search across a set of path plans
 */
Path *search(Point3V loc3, LandingZone *lz) {
  PointV loc = {
    .x = loc3.x,
    .y = loc3.y,
    .vx = loc3.vx,
    .vy = loc3.vy
  };
  const double alt_agl = loc3.alt - lz->destination.alt;
  const double turn_distance_remaining = flight_distance_remaining(alt_agl - ALT_NO_TURNS_BELOW);
  const double fdr = flight_distance_remaining(alt_agl);

  PointV dest = lz->start_of_final();
  const double r = PARAMOTOR_TURNRADIUS;
  const double distance = hypot(loc.x, loc.y);

  Path *straight_path = straight(loc);
  straight_path = path_fly(straight_path, fdr);
  Path *naive_path = naive(loc, dest, r);
  naive_path = path_fly(naive_path, turn_distance_remaining);
  naive_path = path_fly(naive_path, fdr);

  if (alt_agl < ALT_NO_TURNS_BELOW) {
    // No turns under 100ft
    return straight_path;
  } else if (distance > 1000 && naive_path) {
    return naive_path;
  } else {
    vector<Path*> plans = {
      naive_path,
      straight_path
    };
    // Fly path to ground
    for (int i = 0; i < 5; i ++) {
      if (plans[i]) {
        plans[i] = path_fly(plans[i], turn_distance_remaining);
        plans[i] = path_fly(plans[i], fdr);
      }
    }
    return best_plan(lz, plans);
  }
}

/**
 * Find the path plan with the smallest landing error
 * @return the best plan, or null if there were no valid plans
 */
static Path* best_plan(LandingZone *lz, vector<Path*> plans) {
  Path *best = NULL;
  double best_score = INFINITY;
  for (uint8_t i = 0; i < plans.size(); i++) {
    if (plans[i]) {
      const double score = plan_score(lz, plans[i]);
      if (!isnan(score) && score < best_score) {
        best = plans[i];
        best_score = score;
      }
    }
  }
  // printf("Best plan error = %f\n", best_score);
  return best;
}
