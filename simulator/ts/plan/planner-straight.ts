import { PointV } from "../dtypes"
import { Path } from "../geo/path"
import { SegmentLine } from "../geo/segment-line"

/**
 * Fly straight forever.
 */
export function straight(loc: PointV): Path {
  // Project velocity
  const dest = {
    x: loc.x + loc.vx,
    y: loc.y + loc.vy
  }
  return new Path("Str", new SegmentLine(loc, dest))
}
