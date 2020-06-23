import { Point, PointV } from "../dtypes"
import { ParaControls } from "../paracontrols"
import { interpolate } from "../util"
import { Path } from "./paths"

/**
 * 2D line segment.
 */
export class SegmentLine {
  start: PointV
  end: PointV

  constructor(start: Point, end: Point) {
    const dx = end.x - start.x
    const dy = end.y - start.y
    const len = Math.hypot(dx, dy)
    this.start = {
      ...start,
      vx: dx / len,
      vy: dy / len
    }
    this.end = {
      ...end,
      vx: dx / len,
      vy: dy / len
    }
    if (len === 0) {
      console.error("Invalid line: same start and end", start)
    }
  }

  public controls(): ParaControls {
    return {
      left: 0,
      right: 0
    }
  }

  public length() {
    const dx = this.end.x - this.start.x
    const dy = this.end.y - this.start.y
    return Math.hypot(dx, dy)
  }

  /**
   * Fly a given distance along the path.
   * If the end of the path is reached, extend a straight line out to infinity.
   */
  public fly(distance: number): Path {
    if (distance <= 0) {
      console.error("line flight distance must be positive", distance)
    }
    // Linear interpolate
    const alpha = distance / this.length()
    return new Path("line-fly", new SegmentLine(this.start, {
      x: interpolate(this.start.x, this.end.x, alpha),
      y: interpolate(this.start.y, this.end.y, alpha)
    }))
  }

  public render(): Point[] {
    return [this.start, this.end]
  }
}
