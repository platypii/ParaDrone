#include <math.h>
#include <stdlib.h>
#include <vector>
#include "paradrone.h"
using namespace std;

Path *shortest_dubins(PointV loc, PointV dest, double radius);
static vector<Path*> search_pattern(Point3V loc, vector<Point3V> pattern);
static Path *cat_paths(Path *first, vector<Path*> paths);

/**
 * Find the best path to fly between a set of waypoints.
 * Waypoints include a velocity component to indicate the direction to arrive.
 * In between each waypoint, follow the shortest dubins path.
 */
vector<Path*> via_waypoints(Point3V loc, LandingZone *lz) {
  vector<Point3V> right_pattern = {loc, lz->start_of_downwind(TURN_RIGHT), lz->start_of_base(TURN_RIGHT), lz->start_of_final()};
  vector<Point3V> left_pattern = {loc, lz->start_of_downwind(TURN_LEFT), lz->start_of_base(TURN_LEFT), lz->start_of_final()};

  vector<Path*> right_plans = search_pattern(loc, right_pattern);
  vector<Path*> left_plans = search_pattern(loc, left_pattern);

  vector<Path*> plans;
  plans.insert(plans.end(), right_plans.begin(), right_plans.end());
  plans.insert(plans.end(), left_plans.begin(), left_plans.end());
  return plans;
}

/**
 * Search all suffixes of a flight pattern.
 * In between each waypoint, follow the shortest dubins path.
 */
static vector<Path*> search_pattern(Point3V loc, vector<Point3V> pattern) {
  // Pre-compute shortest dubins path from pattern[i] to pattern[i+1]
  vector<Path*> steps;
  for (int i = 0; i < pattern.size() - 1; i++) {
    steps.push_back(shortest_dubins(pattern[i], pattern[i + 1], PARAMOTOR_TURNRADIUS));
  }
  // Construct paths for all suffixes
  vector<Path*> paths;
  for (int i = 0; i < pattern.size(); i++) {
    // Construct path for [loc, pattern[i], ..., pattern[n]]
    Path *first = shortest_dubins(loc, pattern[i], PARAMOTOR_TURNRADIUS);
    Path *path = cat_paths(first, vector<Path*>(steps.begin() + i, steps.end()));
    if (path) paths.push_back(path);
  }
  return paths;
}


/**
 * Concatenate paths.
 * If any of the paths are null, return null.
 */
static Path *cat_paths(Path *first, vector<Path*> paths) {
  vector<Segment*> segments;
  // Copy first segments
  segments.insert(segments.end(), first->segments, first->segments + first->segment_count);
  for (const auto path : paths) {
    if (!path) return NULL; // No shortest path
    // TODO: Deep copy
    segments.insert(segments.end(), path->segments, path->segments + path->segment_count);
  }
  return new_path("waypoints", segments.size(), &segments[0]);
}
