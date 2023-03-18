#include <math.h>
#include <vector>
#include "path.h"
#include "plan.h"
using namespace std;

static vector<Path*> search_pattern(Point3V loc, vector<Point3V> pattern, double radius);
static Path *cat_paths(Path *first, vector<Path*> paths);

/**
 * Find the best path to fly between a set of waypoints.
 * Waypoints include a velocity component to indicate the direction to arrive.
 * In between each waypoint, follow the shortest dubins path.
 */
vector<Path*> via_waypoints(Point3V loc, LandingZone *lz, double radius) {
  // Fly straight for 10s
  Point3V strait = {
    .x = loc.x + 10 * loc.vx,
    .y = loc.y + 10 * loc.vy,
    .alt = loc.alt + 10 * loc.climb,
    .vx = loc.vx,
    .vy = loc.vy,
    .climb = loc.climb
  };
  vector<Point3V> left_pattern = {strait, lz->start_of_downwind(TURN_LEFT), lz->start_of_base(TURN_LEFT), lz->start_of_final()};
  vector<Point3V> right_pattern = {strait, lz->start_of_downwind(TURN_RIGHT), lz->start_of_base(TURN_RIGHT), lz->start_of_final()};

  vector<Path*> left_plans = search_pattern(loc, left_pattern, radius);
  vector<Path*> right_plans = search_pattern(loc, right_pattern, radius);

  vector<Path*> plans;
  plans.insert(plans.end(), left_plans.begin(), left_plans.end());
  plans.insert(plans.end(), right_plans.begin(), right_plans.end());
  return plans;
}

/**
 * Search all suffixes of a flight pattern.
 * In between each waypoint, follow the shortest dubins path.
 */
static vector<Path*> search_pattern(Point3V loc, vector<Point3V> pattern, double radius) {
  // Pre-compute shortest dubins paths from pattern[i] to pattern[i+1]
  vector<Path*> steps;
  for (unsigned int i = 0; i < pattern.size() - 1; i++) {
    steps.push_back(shortest_dubins(pattern[i], pattern[i + 1], radius));
  }
  // Construct paths for all suffixes
  vector<Path*> paths;
  for (unsigned int i = 0; i < pattern.size(); i++) {
    // Construct path for [loc, pattern[i], ..., pattern[n]]
    Path *first = shortest_dubins(loc, pattern[i], radius);
    Path *path = cat_paths(first, vector<Path*>(steps.begin() + i, steps.end()));
    free_path(first);
    if (path) paths.push_back(path);
  }
  // Free steps
  for (const auto step : steps) {
    free_path(step);
  }
  return paths;
}


/**
 * Concatenate paths.
 * If any of the paths are null, return null.
 * TODO: Check for empty segments?
 */
static Path *cat_paths(Path *first, vector<Path*> paths) {
  vector<Segment*> segments;
  // Copy first segments
  for (int i = 0; i < first->segment_count; i++) {
    segments.push_back(segment_copy(first->segments[i]));
  }
  for (const auto path : paths) {
    if (!path) return NULL; // No shortest path
    // Copy step segments
    for (int i = 0; i < path->segment_count; i++) {
      segments.push_back(segment_copy(path->segments[i]));
    }
  }
  return new_path("Waypoint", segments.size(), &segments[0]);
}
