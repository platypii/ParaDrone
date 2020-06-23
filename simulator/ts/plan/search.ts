import { Point3V, Turn } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { Path } from "../geo/paths"
import { SegmentLine } from "../geo/segment-line"
import { Paramotor } from "../paramotor"
import { plan_score } from "./planner"
import { dubins } from "./planner-dubins"
import { naive } from "./planner-naive"
import { straight } from "./planner-straight"
import { viaWaypoints } from "./planner-waypoints"
import { shortestDubins } from "./shortest-dubins"

const no_turns_below = 30 // meters

/**
 * Search across a set of path plans
 */
export function search(loc: Point3V, lz: LandingZone): Path {
  // How much farther can we fly with available altitude?
  const flight_distance_remaining = Paramotor.flightDistanceRemaining(loc.alt)

  const dest = lz.startOfFinal()
  const r = Paramotor.turnRadius
  const dist = Math.hypot(loc.x, loc.y)

  if (loc.vx === 0 && loc.vy === 0) {
    return new Path("default", new SegmentLine(loc, lz.dest))
  }

  // Construct flight paths
  const straightPath = straight(loc).fly(Math.max(1, flight_distance_remaining))

  // TODO: Loiter between SoD left and right, if we will arrive high

  if (loc.alt <= no_turns_below) {
    // No turns under 100ft
    return straightPath
  } else if (dist > 1000) {
    const naivePath = naive(loc, dest, r) || straightPath
    return apply_turn_distance(naivePath, loc.alt)
  } else {
    const paths = [
      ...viaWaypoints(loc, lz),
      // dubins(loc, dest, r, Turn.Right, Turn.Right), // rsr
      // dubins(loc, dest, r, Turn.Right, Turn.Left), // rsl
      // dubins(loc, dest, r, Turn.Left, Turn.Right), // lsr
      // dubins(loc, dest, r, Turn.Left, Turn.Left), // lsl
      // shortestDubins(loc, dest, Paramotor.turnRadius),
      // naive(loc, dest, r),
      straightPath
    ]
    .filter((path): path is Path => !!path)
    .map((p) => apply_turn_distance(p, loc.alt))
    return best_plan(lz, paths) || straightPath
  }
}

function apply_turn_distance(path: Path, alt_agl: number): Path {
  if (alt_agl <= 0) {
    console.error("Grounded")
  }
  if (alt_agl <= no_turns_below) {
    console.error("No turns", alt_agl)
  }
  const turn_distance_remaining = Paramotor.flightDistanceRemaining(alt_agl - no_turns_below)
  const flight_distance_remaining = Paramotor.flightDistanceRemaining(alt_agl)
  return path.fly(turn_distance_remaining).fly(flight_distance_remaining)
}

/**
 * Find the path that minimizes landing error
 */
function best_plan(lz: LandingZone, paths: Path[]): Path | undefined {
  let best
  let bestScore = Infinity
  for (const path of paths) {
    const score = plan_score(lz, path)
    if (score < bestScore) {
      best = path
      bestScore = score
    }
  }
  return best
}
