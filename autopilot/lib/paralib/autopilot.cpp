#include <math.h>
#include <vector>
#include "path.h"
#include "plan.h"
using namespace std;

static Path* best_plan(LandingZone *lz, vector<Path*> plans);
static double plan_score(LandingZone *lz, Path *plan);
static double direction_error(PointV a, PointV b);

/**
 * Search across a set of path plans
 */
Path *search(Point3V loc3, LandingZone *lz, const double r) {
  PointV loc = {
    .x = loc3.x,
    .y = loc3.y,
    .vx = loc3.vx,
    .vy = loc3.vy
  };
  const double turn_distance_remaining = flight_distance_remaining(loc3.alt - ALT_NO_TURNS_BELOW);
  const double fdr = flight_distance_remaining(loc3.alt);

  PointV dest = lz->start_of_final();
  const double distance = sqrt(loc.x * loc.x + loc.y * loc.y);

  if (loc.vx == 0 && loc.vy == 0) {
    Line *default_line = new Line {'L', {loc.x, loc.y}, lz->dest};
    Path *default_path = new_path("default", 1, (Segment**) &default_line);
    return default_path;
  }

  Path *straight_path = straight(loc);
  straight_path = path_fly_free(straight_path, fdr);

  if (loc3.alt <= ALT_NO_TURNS_BELOW) {
    // No turns under 100ft
    return straight_path;
  } else if (distance > 1000) {
    Path *naive_path = naive(loc, dest, r);
    if (naive_path) {
      naive_path = path_fly_free(naive_path, turn_distance_remaining);
      naive_path = path_fly_free(naive_path, fdr);
      return naive_path;
    } else {
      return straight_path;
    }
  } else {
    vector<Path*> plans = {
      // dubins(loc, dest, r, TURN_RIGHT, TURN_RIGHT),
      // dubins(loc, dest, r, TURN_RIGHT, TURN_LEFT),
      // dubins(loc, dest, r, TURN_LEFT, TURN_RIGHT),
      // dubins(loc, dest, r, TURN_LEFT, TURN_LEFT),
      // naive_path,
      straight_path
    };
    vector<Path*> ways = via_waypoints(loc3, lz, PARAMOTOR_TURNRADIUS);
    plans.insert(plans.end(), ways.begin(), ways.end());
    // Fly path to ground
    for (unsigned i = 0; i < plans.size(); i++) {
      if (plans[i]) {
        plans[i] = path_fly_free(plans[i], turn_distance_remaining);
        plans[i] = path_fly_free(plans[i], fdr);
      }
    }
    Path *best = best_plan(lz, plans);
    // Free non-best
    for (unsigned i = 0; i < plans.size(); i++) {
      if (plans[i] != best && plans[i]) {
        free_path(plans[i]);
      }
    }
    return best;
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

/**
 * Plan score. Lower is better.
 */
static double plan_score(LandingZone *lz, Path *plan) {
  if (plan) {
    // LZ is at origin
    const double distance_error = hypot(plan->end.x, plan->end.y);
    const double angle_error = 15 * direction_error(lz->dest, plan->end);
    return distance_error + angle_error;
  } else {
    return 100000;
  }
}

/**
 * Return the angle between two velocity vectors (radians)
 */
static double direction_error(PointV a, PointV b) {
  // Dot product
  const double magA = sqrt(a.vx * a.vx + a.vy * a.vy);
  const double magB = sqrt(b.vx * b.vx + b.vy * b.vy);
  const double dot = (a.vx * b.vx + a.vy * b.vy) / (magA * magB);
  if (dot >= 1) {
    return 0;
  } else if (dot <= -1) {
    return M_PI;
  } else {
    return acos(dot);
  }
}
