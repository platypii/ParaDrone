import { PointV } from "../dtypes"
import { Path } from "../geo/paths"
import { LineSegment } from "../geo/segment-line"

/**
 * Fly straight forever.
 */
export function straight(loc: PointV): Path {
  // Project velocity
  const dest = {
    x: loc.x + loc.vx,
    y: loc.y + loc.vy
  }
  return new Path(new LineSegment(loc, dest))
}
