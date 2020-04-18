import { Point, PointV, Turn } from "../dtypes"
import { Path } from "../geo/paths"
import { SegmentLine } from "../geo/segment-line"
import { SegmentTurn } from "../geo/segment-turn"
import { Paramotor } from "../paramotor"
import { distance } from "../util"

/**
 * Fly naively to a waypoint.
 * This path will consist of a turn, plus a straight line to the target.
 * You will probably not arrive at your destination in the DIRECTION you want though.
 */
export function naive(loc: PointV, dest: Point, r: number): Path | undefined {
  if (Math.hypot(loc.x - dest.x, loc.y - dest.y) < 2 * r) {
    // On top of LZ, no naive path to destination
    return undefined
  }
  // Compute path for naive
  const velocity = Math.hypot(loc.vx, loc.vy)
  if (velocity === 0) {
    // console.log("Zero velocity no tangent")
    return undefined
  }
  // bearing from loc to destination
  const bearing = Math.atan2(dest.x - loc.x, dest.y - loc.y)
  // velocity bearing
  const yaw = Math.atan2(loc.vx, loc.vy)
  // shorter to turn right or left?
  const delta = bearing - yaw
  const turn1 = delta < 0 ? Turn.Left : Turn.Right
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
  const turn = new SegmentTurn(c1, loc, comm1, turn1)
  const line = new SegmentLine(comm1, dest)
  return new Path("naive", turn, line)
}
