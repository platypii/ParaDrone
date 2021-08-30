import { PointV } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { Path } from "../geo/path"
import { Paraglider } from "../paraglider"
import { distance } from "../util"

// Planning params
export const no_turns_below = 30 // meters
export const flare_height = 6 // meters

export interface LandingScore {
  score: number
  distance: number
  angle: number
}

/**
 * Fly until we are at no_turns_below altitude, and then fly straight
 */
export function path_no_turns_below(para: Paraglider, path: Path, alt_agl: number): Path {
  if (alt_agl <= 0) {
    console.error("Grounded")
  }
  if (alt_agl <= no_turns_below) {
    console.error("No turns", alt_agl)
  }
  const turn_distance_remaining = para.flightDistanceRemaining(alt_agl - no_turns_below)
  const flight_distance_remaining = para.flightDistanceRemaining(alt_agl)
  return path.fly(turn_distance_remaining).fly(flight_distance_remaining)
}

/**
 * Find the path that minimizes landing error
 */
export function best_plan(lz: LandingZone, paths: Path[]): Path | undefined {
  if (paths.length <= 1) return paths[0]
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

/**
 * Return the angle between two velocity vectors (radians)
 */
function direction_error(a: PointV, b: PointV): number {
  // Dot product
  const magA = Math.sqrt(a.vx * a.vx + a.vy * a.vy)
  const magB = Math.sqrt(b.vx * b.vx + b.vy * b.vy)
  const dot = (a.vx * b.vx + a.vy * b.vy) / (magA * magB)
  if (dot >= 1) {
    return 0
  } else if (dot <= -1) {
    return Math.PI
  } else {
    const angle = Math.acos(dot)
    if (isNaN(angle)) {
      console.error("direction_error NaN", a, b, dot)
    }
    return angle
  }
}

/**
 * Landing score. Lower is better.
 */
export function landing_score(lz: LandingZone, end: PointV): LandingScore {
  const distanceError = distance(end, lz.dest)
  const angleError = direction_error(lz.dest, end)
  return {
    score: distanceError + 15 * angleError, // Experimentally 15..20 is best
    distance: distanceError,
    angle: angleError
  }
}

/**
 * Plan score. Lower is better.
 */
function plan_score(lz: LandingZone, path: Path): number {
  return landing_score(lz, path.end).score
}
