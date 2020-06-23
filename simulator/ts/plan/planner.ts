import { PointV } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { Path } from "../geo/paths"
import { distance } from "../util"

export interface LandingScore {
  score: number
  distance: number
  angle: number
}

/**
 * Return the angle between two velocity vectors (radians)
 */
function direction_error(a: PointV, b: PointV): number {
  // Dot product
  const magA = Math.hypot(a.vx, a.vy)
  const magB = Math.hypot(b.vx, b.vy)
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
 * Plan score. Lower is better.
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
export function plan_score(lz: LandingZone, path: Path): number {
  return landing_score(lz, path.end).score
}
