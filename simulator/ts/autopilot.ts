import { Point3V } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Path } from "./geo/path"
import { Path3 } from "./geo/path3"
import { SegmentLine } from "./geo/segment-line"
import { Paraglider } from "./paraglider"
import { LandingPattern } from "./plan/pattern"
import { FlarePath } from "./plan/plan-flare"
import { best_plan, flare_height, no_turns_below, path_no_turns_below } from "./plan/planner"
import { naive } from "./plan/planner-naive"
import { straight } from "./plan/planner-straight"
import { viaWaypoints } from "./plan/planner-waypoints"
import { allDubins } from "./plan/shortest-dubins"

const naive_distance = 600 // Fly straight to the lz
const lookahead = 3000 // milliseconds

/**
 * Controls a paraglider toward a landing zone
 */
export class Autopilot {
  public plan?: Path3

  private readonly lz: LandingZone
  private readonly para: Paraglider

  constructor(para: Paraglider, lz: LandingZone) {
    this.lz = lz
    this.para = para
    // Subscribe to gps updates
    para.onLocationUpdate(() => this.replan())
    this.replan()
  }

  private replan() {
    if (this.para.loc) {
      // Replan with new location
      this.plan = search3(this.para, this.lz)
      // Apply to paraglider toggles
      const controls = this.plan.path.controls()
      this.para.toggles.setToggles(controls.left, controls.right)
    }
  }
}

/**
 * Search for a 3D plan
 */
function search3(para: Paraglider, lz: LandingZone): Path3 {
  // Run to where the ball is going
  const next = para.predict(lookahead)!
  const loc = lz.toPoint3V(next)
  return new Path3(search(loc, para, lz), loc.alt)
}

/**
 * Apply autopilot rules, and then search over waypoint paths
 */
export function search(loc: Point3V, para: Paraglider, lz: LandingZone): Path {
  if (isNaN(loc.x)) {
    console.error("invalid location", para.loc)
    throw new Error("invalid location")
  }

  const pattern = new LandingPattern(para, lz)
  const effectiveRadius = para.turnRadius * 1.25
  const distance2 = loc.x * loc.x + loc.y * loc.y // squared

  // How much farther can we fly with available altitude?
  const flight_distance_remaining = para.flightDistanceRemaining(loc.alt)

  // No velocity, hard to plan, holding into the wind seems good
  if (loc.vx === 0 && loc.vy === 0) {
    return new Path("default", new SegmentLine(loc, lz.dest))
  }

  // Construct flight paths
  const straightPath = straight(loc).fly(Math.max(1, flight_distance_remaining))

  if (loc.alt < flare_height) {
    // Flare!!
    return new FlarePath(straightPath)
  } else if (loc.alt <= no_turns_below) {
    // No turns under 100ft
    return straightPath
  } else if (distance2 > naive_distance * naive_distance) {
    // Naive when far away
    const naivePath = naive(loc, pattern.startOfFinal(), effectiveRadius) || straightPath
    return path_no_turns_below(para, naivePath, loc.alt)
  } else {
    const paths = [
      ...viaWaypoints(loc, pattern, effectiveRadius),
      ...allDubins(loc, lz.dest, effectiveRadius),
      // shortestDubins(loc, lz.dest, effectiveRadius), // go straight home
      // naive(loc, lz.dest, effectiveRadius),
      straightPath
    ]
    .filter((path): path is Path => !!path)
    .map((p) => path_no_turns_below(para, p, loc.alt))
    return best_plan(lz, paths) || straightPath
  }
}
