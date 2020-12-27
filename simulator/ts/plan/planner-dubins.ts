import { PointV, Turn } from "../dtypes"
import { Path } from "../geo/path"
import { SegmentLine } from "../geo/segment-line"
import { SegmentTurn } from "../geo/segment-turn"

/**
 * Find dubins path
 */
export function dubins(loc: PointV, dest: PointV, r: number, turn1: Turn, turn2: Turn): Path | undefined {
  if (isNaN(loc.x)) {
    throw new Error("invalid location")
  }
  // First dubins circle, perpendicular to velocity
  const velocity = Math.sqrt(loc.vx * loc.vx + loc.vy * loc.vy)
  if (velocity === 0) {
    console.error("Zero velocity no tangent")
    return undefined
  }
  const c1 = {
    x: loc.x + turn1 * r * loc.vy / velocity,
    y: loc.y - turn1 * r * loc.vx / velocity,
    radius: r
  }
  // Second dubins circle
  const dest_velocity = Math.sqrt(dest.vx * dest.vx + dest.vy * dest.vy)
  if (dest_velocity === 0) {
    console.error("Zero dest velocity no tangent")
    return undefined
  }
  const c2 = {
    x: dest.x + turn2 * r * dest.vy / dest_velocity,
    y: dest.y - turn2 * r * dest.vx / dest_velocity,
    radius: r
  }
  // Delta of dubin circles
  const cx_delta = c2.x - c1.x
  const cy_delta = c2.y - c1.y
  const c_dist = Math.sqrt(cx_delta * cx_delta + cy_delta * cy_delta)
  if (turn1 !== turn2 && c_dist < 2 * r) {
    // console.error("Intersecting dubins circles", c2, dest)
    return undefined
  }
  // Angle from center to center
  const center_angle = Math.atan2(cx_delta, cy_delta)
  // Commute
  // If turn1 != turn2, then cross circles
  let turn_delta = 0
  if (turn1 !== turn2) {
    turn_delta = (turn1 - turn2) * r / c_dist
    turn_delta = Math.max(-1, Math.min(1, turn_delta))
    turn_delta = Math.asin(turn_delta)
  }
  const commute_angle = center_angle + turn_delta
  // const commute_length = Math.sqrt(c_dist * c_dist - turn_delta * turn_delta)
  if (isNaN(commute_angle)) {
    // Happens when c1 intersects c2
    console.error("NaN commute angle", commute_angle, Math.asin(turn_delta * r / c_dist), turn_delta * r / c_dist)
  }
  // Last touch of first dubin circle (start of commute home)
  const comm1 = {
    x: c1.x - turn1 * r * Math.cos(commute_angle),
    y: c1.y + turn1 * r * Math.sin(commute_angle)
  }
  // First touch of second dubin circle (beginning of turn to final)
  const comm2 = {
    x: c2.x - turn2 * r * Math.cos(commute_angle),
    y: c2.y + turn2 * r * Math.sin(commute_angle)
  }
  return new Path(
    "dubins",
    new SegmentTurn(c1, loc, comm1, turn1),
    new SegmentLine(comm1, comm2),
    new SegmentTurn(c2, comm2, dest, turn2)
  )
}
