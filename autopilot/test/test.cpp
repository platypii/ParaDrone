#include "geo.h"
#include "messages.h"
#include "path.h"
#include "plan.h"
#include <vector>
#include <unity.h>
using namespace std;

// Unity setup
void setUp() {}
void tearDown() {}

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
GeoPointV loc = {
  .millis = 0,
  .lat = 47.239,
  .lng = -123.143,
  .alt = 84,
  .vN = 0,
  .vE = 0,
  .climb = 0
};

void test_straight() {
  Path *path = straight(start);
  TEST_ASSERT_NOT_NULL(path);
  if (!path) return;
  TEST_ASSERT_EQUAL_STRING("Str", path->name);
  TEST_ASSERT_EQUAL(1, path->segment_count);
  TEST_ASSERT_EQUAL(10, path_length(path));
  free_path(path);
}

void test_naive() {
  Path *path = naive(start, lz.dest, r);
  TEST_ASSERT_NOT_NULL(path);
  if (!path) return;
  TEST_ASSERT_EQUAL_STRING("NaiveR", path->name);
  TEST_ASSERT_EQUAL(2, path->segment_count);
  TEST_ASSERT_EQUAL(1589, path_length(path));
  free_path(path);
}

void test_shortest_dubins() {
  Path *path = shortest_dubins(start, lz.dest, r);
  TEST_ASSERT_NOT_NULL(path);
  if (!path) return;
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
  if (!path) return;
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
  if (!path) return;
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

void test_convert() {
  char str[3];
  bearing2(str, 0);
  TEST_ASSERT_EQUAL_STRING("N", str);
  bearing2(str, 45);
  TEST_ASSERT_EQUAL_STRING("NE", str);
  bearing2(str, 90);
  TEST_ASSERT_EQUAL_STRING("E", str);
  bearing2(str, 135);
  TEST_ASSERT_EQUAL_STRING("SE", str);
  bearing2(str, 180);
  TEST_ASSERT_EQUAL_STRING("S", str);
  bearing2(str, 225);
  TEST_ASSERT_EQUAL_STRING("SW", str);
  bearing2(str, 270);
  TEST_ASSERT_EQUAL_STRING("W", str);
  bearing2(str, 315);
  TEST_ASSERT_EQUAL_STRING("NW", str);
  bearing2(str, 0);
  TEST_ASSERT_EQUAL_STRING("N", str);
  bearing2(str, NAN);
  TEST_ASSERT_EQUAL_STRING("", str);
  bearing2(str, 720);
  TEST_ASSERT_EQUAL_STRING("N", str);
  bearing2(str, 800);
  TEST_ASSERT_EQUAL_STRING("E", str);
  bearing2(str, -400);
  TEST_ASSERT_EQUAL_STRING("NW", str);
}

void test_geo() {
  LatLng seattle = {47.60, -122.33};
  LatLng la = {34.0, -118.2};
  double bear = 2.8913; // radians
  double dist = 1551093.52;
  TEST_ASSERT_EQUAL(bear, geo_bearing(seattle.lat, seattle.lng, la.lat, la.lng));
  TEST_ASSERT_EQUAL(dist, geo_distance(seattle.lat, seattle.lng, la.lat, la.lng));
  LatLng moved = geo_move_bearing(seattle.lat, seattle.lng, bear, dist);
  TEST_ASSERT_EQUAL(la.lat, moved.lat);
  TEST_ASSERT_EQUAL(la.lng, moved.lng);
}

void test_messages() {
  TEST_ASSERT_EQUAL(11, sizeof(LocationMessage));
  TEST_ASSERT_EQUAL(17, sizeof(SpeedMessage));
  TEST_ASSERT_EQUAL(13, sizeof(LandingZoneMessage));
  TEST_ASSERT_EQUAL(8, sizeof(MotorConfigMessage));

  // Landing zone
  LandingZoneMessage packed_lz = pack_lz(&lz);
  LandingZone *lz2 = unpack_lz(&packed_lz);
  TEST_ASSERT_EQUAL(13, sizeof(packed_lz));
  TEST_ASSERT_EQUAL(lz.destination.lat, lz2->destination.lat);
  TEST_ASSERT_EQUAL(lz.destination.lng, lz2->destination.lng);
  TEST_ASSERT_EQUAL(lz.destination.alt, lz2->destination.alt);
  TEST_ASSERT_EQUAL(lz.landingDirection, lz2->landingDirection);
  delete lz2;

  // No landing zone
  packed_lz = pack_lz(NULL);
  LandingZone *nolz = unpack_lz(&packed_lz);
  TEST_ASSERT_NULL(nolz);
  delete nolz;

  // Speed message
  SpeedMessage packed_loc = pack_speed(&loc);
  GeoPointV *loc2 = unpack_speed(0, &packed_loc);
  TEST_ASSERT_EQUAL(17, sizeof(packed_loc));
  TEST_ASSERT_EQUAL(loc.lat, loc2->lat);
  TEST_ASSERT_EQUAL(loc.lng, loc2->lng);
  TEST_ASSERT_EQUAL(loc.alt, loc2->alt);
  TEST_ASSERT_EQUAL(loc.vN, loc2->vN);
  TEST_ASSERT_EQUAL(loc.vE, loc2->vE);
  TEST_ASSERT_EQUAL(loc.climb, loc2->climb);
  delete loc2;
}

int main() {
  test_straight();
  test_naive();
  test_shortest_dubins();
  test_waypoints();
  test_autopilot_far();
  test_autopilot_near();
  test_path();
  test_convert();
  test_geo();
  test_messages();

  return 0;
}
