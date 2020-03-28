import { Point3V, Turn } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { Path } from "../geo/paths"
import { Paramotor } from "../paramotor"
import { distance } from "../util"
import { dubins } from "./planner-dubins"
import { naive } from "./planner-naive"
import { straight } from "./planner-straight"

/**
 * Search across a set of path plans
 */
export function search(loc: Point3V, lz: LandingZone): Path {
  // How much farther can we fly with available altitude?
  const alt = loc.alt - lz.destination.alt
  const distance = Paramotor.flightDistanceRemaining(alt)

  // Construct flight paths
  const dest = lz.startOfFinal()
  const r = Paramotor.turnRadius

  // Construct flight paths
  const straightPath = straight(loc).fly(distance)
  const paths = [
    dubins(loc, dest, r, Turn.Right, Turn.Right), // rsr
    dubins(loc, dest, r, Turn.Right, Turn.Left), // rsl
    dubins(loc, dest, r, Turn.Left, Turn.Right), // lsr
    dubins(loc, dest, r, Turn.Left, Turn.Left), // lsl
    naive(loc, dest, r),
    straightPath
  ]
  .filter((path): path is Path => !!path)
  .map((p) => p.fly(distance))

  return best_plan(lz, paths) || straightPath
}

/**
 * Find the path that minimizes landing error
 */
function best_plan(lz: LandingZone, paths: Path[]): Path | undefined {
  let best
  let bestScore = Infinity
  for (const path of paths) {
    const dist = distance(path.end, lz.dest)
    if (dist < bestScore) {
      best = path
      bestScore = dist
    }
  }
  return best
}
