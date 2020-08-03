import { Point3V, Turn } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { Path } from "../geo/paths"
import { Paramotor } from "../paramotor"
import { shortestDubins } from "./shortest-dubins"

/**
 * Find the best path to fly between a set of waypoints.
 * Waypoints include a velocity component to indicate the direction to arrive.
 */
export function viaWaypoints(loc: Point3V, lz: LandingZone, para: Paramotor): Path[] {
  const r = para.turnRadius
  // Fly straight for 10s
  const straight: Point3V = {
    ...loc,
    x: loc.x + 10 * loc.vx,
    y: loc.y + 10 * loc.vy,
    alt: loc.alt + 10 * loc.climb
  }
  const rightPattern = [straight, lz.startOfDownwind(para, Turn.Right), lz.startOfBase(para, Turn.Right), lz.startOfFinal(para), lz.dest]
  const leftPattern = [straight, lz.startOfDownwind(para, Turn.Left), lz.startOfBase(para, Turn.Left), lz.startOfFinal(para), lz.dest]

  return [...searchPattern(loc, leftPattern, r), ...searchPattern(loc, rightPattern, r)]
}

/**
 * Search all suffixes of a flight pattern.
 * In between each waypoint, follow the shortest dubins path.
 */
function searchPattern(loc: Point3V, pattern: Point3V[], r: number): Path[] {
  // Pre-compute shortest dubins path from pattern[i] to pattern[i+1]
  const steps = []
  for (let i = 0; i < pattern.length - 1; i++) {
    steps.push(shortestDubins(pattern[i], pattern[i + 1], r))
  }
  // Construct paths for all suffixes
  const paths = []
  for (let i = 0; i < pattern.length; i++) {
    // Construct path for [loc, pattern[i], ..., pattern[n]]
    const first = shortestDubins(loc, pattern[i], r)
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
