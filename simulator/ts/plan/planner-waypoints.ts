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
  // Fly straight for 10s
  const straight: Point3V = {
    ...loc,
    x: loc.x + 10 * loc.vx,
    y: loc.y + 10 * loc.vy,
    alt: loc.alt + 10 * loc.climb
  }
  const rightPattern = [straight, lz.startOfDownwind(Turn.Right), lz.startOfBase(Turn.Right), lz.startOfFinal(), lz.dest]
  const leftPattern = [straight, lz.startOfDownwind(Turn.Left), lz.startOfBase(Turn.Left), lz.startOfFinal(), lz.dest]

  return [...searchPattern(loc, leftPattern), ...searchPattern(loc, rightPattern)]
}

/**
 * Search all suffixes of a flight pattern.
 * In between each waypoint, follow the shortest dubins path.
 */
function searchPattern(loc: Point3V, pattern: Point3V[]): Path[] {
  // Pre-compute shortest dubins path from pattern[i] to pattern[i+1]
  const steps = []
  for (let i = 0; i < pattern.length - 1; i++) {
    steps.push(shortestDubins(pattern[i], pattern[i + 1], Paramotor.turnRadius))
  }
  // Construct paths for all suffixes
  const paths = []
  for (let i = 0; i < pattern.length; i++) {
    // Construct path for [loc, pattern[i], ..., pattern[n]]
    const first = shortestDubins(loc, pattern[i], Paramotor.turnRadius)
    const path = catPaths(first, ...steps.slice(i))
    if (path) paths.push(path)
  }
  return paths
}


/**
 * Concatenate paths.
 * If any of the paths are null, return null.
 */
function catPaths(...paths: Array<Path | undefined>): Path | undefined {
  const segments = []
  for (const path of paths) {
    if (!path) return undefined // No shortest path
    segments.push(...path.segments)
  }
  return new Path("waypoints", ...segments)
}
