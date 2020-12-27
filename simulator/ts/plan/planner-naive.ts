import { Point, PointV, Turn } from "../dtypes"
import { Path } from "../geo/path"
import { SegmentLine } from "../geo/segment-line"
import { SegmentTurn } from "../geo/segment-turn"
import { toRadians } from "../geo/trig"
import { Paraglider } from "../paraglider"
import { distance } from "../util"

/**
 * Fly naively to a waypoint.
 * This path will consist of a turn, plus a straight line to the target.
 * You will probably not arrive at your destination in the DIRECTION you want though.
 */
export function naive(loc: PointV, dest: Point, r: number): Path | undefined {
  const velocity = Math.sqrt(loc.vx * loc.vx + loc.vy * loc.vy)
  if (velocity === 0) {
    // console.log("Zero velocity no tangent")
    return undefined
  }
  const delta_x = dest.x - loc.x
  const delta_y = dest.y - loc.y
  const delta = Math.sqrt(delta_x * delta_x + delta_y * delta_y)
  if (delta < 2 * r) {
    // On top of LZ, let planner fallback to straight plan
    // There is an unreachable area which is a subset of delta < 2r.
    return undefined
  }
  // If we are within epislon of on-target, return straight line
  if (approximatelyParallel(loc.vx / velocity, loc.vy / velocity, delta_x / delta, delta_y / delta)) {
    // console.log("approximately parallel")
    return undefined
  }
  // Is dest on our left or right?
  const dot = delta_y * loc.vx - delta_x * loc.vy
  const turn1 = dot > 0 ? Turn.Left : Turn.Right
  const c1 = {
    x: loc.x + turn1 * r * loc.vy / velocity,
    y: loc.y - turn1 * r * loc.vx / velocity,
    radius: r
  }
  // Angle from circle center to target
  const center_angle = Math.atan2(dest.x - c1.x, dest.y - c1.y)
  // Commute
  const cdest = distance(c1, dest)
  const offset = turn1 * Math.asin(r / cdest)
  const commute_angle = center_angle + offset
  // const commute_length = Math.sqrt(cdest * cdest - r * r)
  // Last touch of first dubin circle (start of commute home)
  const comm1 = {
    x: c1.x - turn1 * r * Math.cos(commute_angle),
    y: c1.y + turn1 * r * Math.sin(commute_angle)
  }
  const turn = new SegmentTurn(c1, loc, comm1, turn1)
  const line = new SegmentLine(comm1, dest)
  const name = turn1 === Turn.Left ? "naive (L)" : "naive (R)"
  return new Path(name, turn, line)
}

// If we are within angleError of on-target, return straight line
const angleError = toRadians(2) // radians
const angleErrorUnitDistance = 2 * Math.sin(angleError)
/**
 * A fast function to tell you if two vectors are within angleError radians of each other.
 * Vectors must be unit!
 */
function approximatelyParallel(x1: number, y1: number, x2: number, y2: number): boolean {
  const dx = x1 - x2
  const dy = y1 - y2
  return dx * dx + dy * dy < angleErrorUnitDistance * angleErrorUnitDistance
}
