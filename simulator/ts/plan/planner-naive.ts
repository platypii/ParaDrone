import { Point, PointV, Turn } from "../dtypes"
import { Path } from "../geo/paths"
import { LineSegment } from "../geo/segment-line"
import { TurnSegment } from "../geo/segment-turn"
import { Paramotor } from "../paramotor"
import { distance } from "../util"

/**
 * Fly naively toward a waypoint.
 * This path will consist of a turn, plus a straight line to the target.
 * Essentially this is dubins with unconstrained target direction.
 */
export function naive(loc: PointV, dest: Point, r: number): Path | undefined {
  if (Math.hypot(loc.x - dest.x, loc.y - dest.y) < 2 * r) {
    // console.error("Naive planner on top of lz")
    return undefined
  }
  // bearing from loc to destination
  const bearing = Math.atan2(dest.x - loc.x, dest.y - loc.y)
  // velocity bearing
  const yaw = Math.atan2(loc.vx, loc.vy)
  // shorter to turn right or left?
  const delta = bearing - yaw
  const turn1 = delta < 0 ? Turn.Left : Turn.Right
  // Compute path for naive
  const velocity = Math.hypot(loc.vx, loc.vy)
  const c1 = {
    x: loc.x + turn1 * r * loc.vy / velocity,
    y: loc.y - turn1 * r * loc.vx / velocity,
    radius: Paramotor.turnRadius
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
  const turn = new TurnSegment(c1, loc, comm1, turn1)
  const line = new LineSegment(comm1, dest)
  return new Path(turn, line)
}
