#include "landingzone.h"
#include "path.h"
#include "plan.h"
#include <vector>
#include <unity.h>
using namespace std;

LandingZone lz(47.239, -123.143, 84, 0.5585 /* 32 degrees */);
double r = 100;
Point3V start = {
  .x = 1000,
  .y = 1000,
  .alt = 800,
  .vx = 10,
  .vy = 0,
  .climb = -3
};

void test_straight() {
  Path *path = straight(start);
  TEST_ASSERT_NOT_NULL(path);
  TEST_ASSERT_EQUAL_STRING("Str", path->name);
  TEST_ASSERT_EQUAL(1, path->segment_count);
  TEST_ASSERT_EQUAL(10, path_length(path));
  free_path(path);
}

void test_naive() {
  Path *path = naive(start, lz.dest, r);
  TEST_ASSERT_NOT_NULL(path);
  TEST_ASSERT_EQUAL_STRING("NaiveR", path->name);
  TEST_ASSERT_EQUAL(2, path->segment_count);
  TEST_ASSERT_EQUAL(1589, path_length(path));
  free_path(path);
}

void test_shortest_dubins() {
  Path *path = shortest_dubins(start, lz.dest, r);
  TEST_ASSERT_NOT_NULL(path);
  TEST_ASSERT_EQUAL_STRING("DubinR", path->name);
  TEST_ASSERT_EQUAL(3, path->segment_count);
  TEST_ASSERT_EQUAL(1848, path_length(path));
  free_path(path);
}

void test_waypoints() {
  vector<Path*> paths = via_waypoints(start, &lz, r);
  TEST_ASSERT_EQUAL(8, paths.size());
  TEST_ASSERT_EQUAL(12, paths[0]->segment_count);
  TEST_ASSERT_EQUAL(9, paths[1]->segment_count);
  TEST_ASSERT_EQUAL(6, paths[2]->segment_count);
  TEST_ASSERT_EQUAL(3, paths[3]->segment_count);
  TEST_ASSERT_EQUAL_STRING("Waypoint", paths[0]->name);
  TEST_ASSERT_FLOAT_WITHIN(0.1, 2979.3, path_length(paths[0]));
  TEST_ASSERT_FLOAT_WITHIN(0.1, 2793.1, path_length(paths[1]));
  TEST_ASSERT_FLOAT_WITHIN(0.1, 2778.8, path_length(paths[2]));
  TEST_ASSERT_FLOAT_WITHIN(0.1, 1995.5, path_length(paths[3]));
  for (unsigned i = 0; i < paths.size(); i++) {
    free_path(paths[i]);
  }
}

void test_autopilot_far() {
  Path *path = search(start, &lz, PARAMOTOR_TURNRADIUS);
  TEST_ASSERT_NOT_NULL(path);
  const double score = hypot(path->end.x, path->end.y);
  TEST_ASSERT_EQUAL_STRING("NaiveR", path->name);
  TEST_ASSERT_EQUAL(2, path->segment_count);
  TEST_ASSERT_FLOAT_WITHIN(0.1, 3200, path_length(path));
  TEST_ASSERT_FLOAT_WITHIN(0.1, 1701.7, score);
  free_path(path);
}

void test_autopilot_near() {
  GeoPointV loc = {1000, 47.24, -123.14, 884, 0, -10, -3};
  Point3V point = lz.to_point3V(&loc);
  Path *path = search(point, &lz, PARAMOTOR_TURNRADIUS);
  TEST_ASSERT_NOT_NULL(path);
  const double score = hypot(path->end.x, path->end.y);
  TEST_ASSERT_EQUAL_STRING("Waypoint", path->name);
  TEST_ASSERT_EQUAL(13, path->segment_count);
  TEST_ASSERT_FLOAT_WITHIN(0.1, 3200, path_length(path));
  TEST_ASSERT_FLOAT_WITHIN(0.1, 2330.9, score);
  free_path(path);
}

void test_path() {
  // Exercise path_fly_free so we can find leaks
  Path *path = naive(start, lz.dest, r);
  path = path_fly_free(path, 10);
  path = path_fly_free(path, 20);
  path = path_fly_free(path, 0);
  free_path(path);
}

int main() {
  test_straight();
  test_naive();
  test_shortest_dubins();
  test_waypoints();
  test_autopilot_far();
  test_autopilot_near();
  test_path();

  return 0;
}
