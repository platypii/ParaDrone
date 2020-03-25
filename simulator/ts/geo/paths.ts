import { Point } from "../dtypes"
import { ParaControls } from "../paracontrols"
import { LineSegment } from "./segment-line"
import { TurnSegment } from "./segment-turn"

export type Segment = LineSegment | TurnSegment

export class Path {
  readonly start: Point
  readonly end: Point
  private readonly parts: Segment[]

  constructor(...segments: Segment[]) {
    this.parts = segments
    this.start = segments[0].start
    this.end = segments[segments.length - 1].end
  }

  public controls(): ParaControls {
    return this.parts[0].controls()
  }

  public length(): number {
    return this.parts
      .map((s) => s.length())
      .reduce((a, b) => a + b, 0)
  }

  public render(): Point[] {
    // Flatten segments
    return ([] as Point[]).concat(...this.parts.map((s) => s.render()))
  }

  public segments(): Segment[] {
    return this.parts;
  }

  /**
   * Fly a given distance along the path.
   * If the end of the path is reached, extend a straight line out to infinity.
   */
  public fly(distance: number): Path {
    const trimmed: Segment[] = []
    let flown = 0
    let i = 0
    for (; i < this.parts.length - 1; i++) {
      const segment = this.parts[i]
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
    const last = this.parts[i].fly(distance - flown)
    trimmed.push(...last.segments())
    return new Path(...trimmed)
  }

}
