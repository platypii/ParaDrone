import { Point3V, PointV, Turn } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { Path } from "../geo/paths"
import { SegmentLine } from "../geo/segment-line"
import { Paramotor } from "../paramotor"
import { distance } from "../util"
import { dubins } from "./planner-dubins"
import { naive } from "./planner-naive"
import { straight } from "./planner-straight"
import { viaWaypoints } from "./planner-waypoints"

const no_turns_below = 30 // meters

/**
 * Search across a set of path plans
 */
export function search(loc: Point3V, lz: LandingZone): Path {
  // How much farther can we fly with available altitude?
  const alt_agl = loc.alt - lz.destination.alt
  const turn_distance_remaining = Paramotor.flightDistanceRemaining(alt_agl - no_turns_below)
  const flight_distance_remaining = Paramotor.flightDistanceRemaining(alt_agl)

  const dest = lz.startOfFinal()
  const r = Paramotor.turnRadius
  const dist = Math.hypot(loc.x, loc.y)

  if (loc.vx === 0 && loc.vy === 0) {
    return new Path("default", new SegmentLine(loc, lz.dest))
  }

  // Construct flight paths
  const straightPath = straight(loc).fly(Math.max(1, flight_distance_remaining))

  // TODO: Loiter between SoD left and right, if we will arrive high

  if (alt_agl < no_turns_below) {
    // No turns under 100ft
    return straightPath
  } else if (dist > 1000) {
    const naivePath = naive(loc, dest, r)?.fly(turn_distance_remaining).fly(flight_distance_remaining)
    return naivePath || straightPath
  } else {
    const paths = [
      ...viaWaypoints(loc, lz),
      // dubins(loc, dest, r, Turn.Right, Turn.Right), // rsr
      // dubins(loc, dest, r, Turn.Right, Turn.Left), // rsl
      // dubins(loc, dest, r, Turn.Left, Turn.Right), // lsr
      // dubins(loc, dest, r, Turn.Left, Turn.Left), // lsl
      // naivePath,
      straightPath
    ]
    .filter((path): path is Path => !!path)
    .map((p) => p.fly(turn_distance_remaining).fly(flight_distance_remaining))
    return best_plan(lz, paths) || straightPath
  }
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

function plan_score(lz: LandingZone, path: Path): number {
  return distance(path.end, lz.dest) + 20 * directionError(lz, path.end)
}

function directionError(lz: LandingZone, end: PointV): number {
  // Dot product
  const dot = lz.dest.vx * end.vx + lz.dest.vx * end.vy
  return Math.acos(dot)
}
