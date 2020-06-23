import { Point, Point3V, PointV } from "../dtypes"
import { ParaControls } from "../paracontrols"
import { Paramotor } from "../paramotor"
import { SegmentLine } from "./segment-line"
import { SegmentTurn } from "./segment-turn"

export type Segment = SegmentLine | SegmentTurn

export class Path {
  readonly name: string
  readonly start: PointV
  readonly end: PointV
  readonly segments: Segment[]

  constructor(name: string, ...segments: Segment[]) {
    this.name = name
    this.segments = segments
    this.start = segments[0].start
    this.end = segments[segments.length - 1].end
  }

  public controls(): ParaControls {
    return this.segments[0].controls()
  }

  public length(): number {
    return this.segments
      .map((s) => s.length())
      .reduce((a, b) => a + b, 0)
  }

  public render(): Point[] {
    // Flatten segments
    return ([] as Point[]).concat(...this.segments.map((s) => s.render()))
  }

  public render3(startAlt: number): Point3V[] {
    // Render and compute altitudes
    let alt = startAlt
    let lastPoint: Point
    return this.render().map((p) => {
      if (lastPoint) {
        const lastDistance = Math.hypot(p.x - lastPoint.x, p.y - lastPoint.y)
        alt -= lastDistance / Paramotor.glide
      }
      lastPoint = p
      return {
        ...p,
        alt,
        vx: 0,
        vy: 0,
        climb: Paramotor.climbRate
      }
    })
  }

  /**
   * Fly a given distance along the path.
   * If the end of the path is reached, extend a straight line out to infinity.
   */
  public fly(distance: number): Path {
    if (distance <= 0) {
      console.error("path flight distance must be positive", distance)
    }
    const trimmed: Segment[] = []
    let flown = 0
    let i = 0
    for (; i < this.segments.length - 1; i++) {
      const segment = this.segments[i]
      const segmentLength = segment.length()
      if (distance < flown + segmentLength) {
        // End point is within segment
        break;
      } else {
        trimmed.push(segment)
        flown += segmentLength
      }
    }
    // Fly last segment
    const last = this.segments[i].fly(distance - flown)
    trimmed.push(...last.segments)
    return new Path(this.name, ...trimmed)
  }

}
