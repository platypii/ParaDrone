import { Point } from "../dtypes"
import { ParaControls } from "../paracontrols"
import { interpolate } from "../util"
import { Path } from "./paths"

export class LineSegment {
  start: Point
  end: Point

  constructor(start: Point, end: Point) {
    this.start = start
    this.end = end
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
    if (distance < 0) {
      console.error("flight distance cannot be negative", distance)
    }
    // Linear interpolate
    const alpha = distance / this.length()
    return new Path(new LineSegment(this.start, {
      x: interpolate(this.start.x, this.end.x, alpha),
      y: interpolate(this.start.y, this.end.y, alpha)
    }))
  }

  public render(): Point[] {
    return [this.start, this.end]
  }
}
