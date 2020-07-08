#include <cmath>
#include <vector>
#include "geo.h"
#include "plan.h"
using namespace std;

static Path *shortest_path(vector<Path*> paths);

/**
 * Find the shortest dubins path from A to B
 */
Path *shortest_dubins(PointV loc, PointV dest, double r) {
  // Construct flight paths
  vector<Path*> paths = {
    dubins(loc, dest, r, TURN_RIGHT, TURN_RIGHT), // rsr
    dubins(loc, dest, r, TURN_RIGHT, TURN_LEFT), // rsl
    dubins(loc, dest, r, TURN_LEFT, TURN_RIGHT), // lsr
    dubins(loc, dest, r, TURN_LEFT, TURN_LEFT) // lsl
  };
  Path *shortest = shortest_path(paths);
  // Free non-shortest paths
  for (const auto path : paths) {
    if (path != shortest) {
      free_path(path);
    }
  }
  return shortest;
}

/**
 * Find the path that minimizes path length
 */
static Path *shortest_path(vector<Path*> paths) {
  Path *best = NULL;
  double best_score = INFINITY;
  for (const auto path : paths) {
    if (path) {
      const double score = path_length(path);
      if (score < best_score) {
        best = path;
        best_score = score;
      }
    }
  }
  return best;
}
