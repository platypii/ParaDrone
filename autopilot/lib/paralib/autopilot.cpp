#include <math.h>
#include <vector>
#include "path.h"
#include "plan.h"
using namespace std;

#define NAIVE_DISTANCE 600 // Fly straight to the lz
#define LOOKAHEAD 3 // seconds

static Path* best_plan(LandingZone *lz, vector<Path*> plans);
static double plan_score(LandingZone *lz, Path *plan);
static double direction_error(PointV a, PointV b);

/**
 * Search for a 3D plan
 */
Path *search3(GeoPointV *ll, LandingZone *lz, float toggle_speed, float toggle_balance) {
  // Run to where the ball is going
  GeoPointV *next = para_predict(ll, LOOKAHEAD, toggle_speed, toggle_balance);
  Point3V loc = lz->to_point3V(next);
  return search(loc, lz, PARAMOTOR_TURNRADIUS);
}

/**
 * Apply autopilot rules, and then search over waypoint paths
 */
Path *search(Point3V loc3, LandingZone *lz, const double turn_radius) {
  PointV loc = {
    .x = loc3.x,
    .y = loc3.y,
    .vx = loc3.vx,
    .vy = loc3.vy
  };
  const double effective_radius = turn_radius * 1.25;
  const double distance2 = loc.x * loc.x + loc.y * loc.y; // squared

  // How much farther can we fly with available altitude?
  const double turn_distance_remaining = flight_distance_remaining(loc3.alt - ALT_NO_TURNS_BELOW);
  const double fdr = flight_distance_remaining(loc3.alt);

  if (loc.vx == 0 && loc.vy == 0) {
    Line *default_line = new Line {'L', {loc.x, loc.y}, lz->dest};
    Path *default_path = new_path("Default", 1, (Segment**) &default_line);
    return default_path;
  }

  Path *straight_path = straight(loc);
  straight_path = path_fly_free(straight_path, fdr);

  if (loc3.alt <= ALT_NO_TURNS_BELOW) {
    // No turns under 100ft
    return straight_path;
  } else if (distance2 > NAIVE_DISTANCE * NAIVE_DISTANCE) {
    // Naive when far away
    Path *naive_path = naive(loc, lz->start_of_final(), effective_radius);
    if (naive_path) {
      naive_path = path_fly_free(naive_path, turn_distance_remaining);
      naive_path = path_fly_free(naive_path, fdr);
      free_path(straight_path);
      return naive_path;
    } else {
      return straight_path;
    }
  } else {
    vector<Path*> plans = {
      dubins(loc, lz->dest, effective_radius, TURN_RIGHT, TURN_RIGHT),
      dubins(loc, lz->dest, effective_radius, TURN_RIGHT, TURN_LEFT),
      dubins(loc, lz->dest, effective_radius, TURN_LEFT, TURN_RIGHT),
      dubins(loc, lz->dest, effective_radius, TURN_LEFT, TURN_LEFT),
      straight_path
    };
    vector<Path*> ways = via_waypoints(loc3, lz, effective_radius);
    plans.insert(plans.end(), ways.begin(), ways.end());
    // Fly path to ground
    for (unsigned i = 0; i < plans.size(); i++) {
      if (plans[i]) {
        plans[i] = path_fly_free(plans[i], turn_distance_remaining);
        plans[i] = path_fly_free(plans[i], fdr);
      }
    }
    // Find the best and free the rest
    return best_plan(lz, plans);
  }
}

/**
 * Find the path plan with the smallest landing error.
 * Free non-best plans as we go.
 *
 * @return the best plan, or null if there were no valid plans
 */
static Path* best_plan(LandingZone *lz, vector<Path*> plans) {
  if (plans.size() == 1) return plans[0];
  Path *best = NULL;
  double best_score = INFINITY;
  for (auto plan : plans) {
    if (plan) {
      const double score = plan_score(lz, plan);
      if (!isnan(score) && score < best_score) {
        if (best) {
          free_path(best);
        }
        best = plan;
        best_score = score;
      } else {
        // Free non-best
        free_path(plan);
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
    const double distance_error = sqrt(plan->end.x * plan->end.x + plan->end.y * plan->end.y);
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
