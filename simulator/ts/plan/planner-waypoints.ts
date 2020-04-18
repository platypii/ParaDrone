import { Point3V, Turn } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { Path } from "../geo/paths"
import { Paramotor } from "../paramotor"
import { shortestDubins } from "./shortest-dubins"

/**
 * Find the best path to fly between a set of waypoints.
 * Waypoints include a velocity component to indicate the direction to arrive.
 */
export function viaWaypoints(loc: Point3V, lz: LandingZone): Path[] {
  const rightDownwind = [loc, lz.startOfDownwind(Turn.Right), lz.startOfBase(Turn.Right), lz.startOfFinal()]
  const leftDownwind = [loc, lz.startOfDownwind(Turn.Left), lz.startOfBase(Turn.Left), lz.startOfFinal()]
  const rightBase = [loc, lz.startOfBase(Turn.Right), lz.startOfFinal()]
  const leftBase = [loc, lz.startOfBase(Turn.Left), lz.startOfFinal()]
  const finalDirect = [loc, lz.startOfFinal()]
  const lzDirect = [loc, lz.dest]

  const patterns = [rightDownwind, leftDownwind, rightBase, leftBase, finalDirect, lzDirect]
  return patterns
    .map((p) => waypointPath(p))
    .filter((path): path is Path => !!path)
}

/**
 * Follow a series of waypoints.
 * In between each waypoint, follow the shortest dubins path.
 */
function waypointPath(waypoints: Point3V[]): Path | undefined {
  const segments = []
  for (let i = 0; i < waypoints.length - 1; i++) {
    const shortest = shortestDubins(waypoints[i], waypoints[i + 1], Paramotor.turnRadius)
    if (!shortest) {
      // No shortest path
      return undefined
    }
    segments.push(...shortest.segments)
  }
  return new Path("waypoints", ...segments)
}
