import { Point, PointV, TogglePosition } from "../dtypes"
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

  public controls(): TogglePosition {
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

  /**
   * Fly a given distance along the path.
   * If the end of the path is reached, extend a straight line out to infinity.
   */
  public fly(distance: number): Path {
    if (!(distance > 0)) {
      console.error("path fly distance must be positive", distance)
      return this
    }
    const trimmed: Segment[] = []
    let flown = 0
    let i = 0
    for (; i < this.segments.length - 1; i++) {
      const segment = this.segments[i]
      const segmentLength = segment.length()
      if (distance < flown + segmentLength) {
        // End is within segment
        break
      } else {
        trimmed.push(segment)
        flown += segmentLength
      }
    }
    const remaining = distance - flown
    if (remaining > 0) {
      // Fly last segment
      const last = this.segments[i].fly(distance - flown)
      trimmed.push(...last.segments)
    } else if (remaining < 0) {
      console.error("remaining distance must be positive", distance, flown)
    }
    return new Path(this.name, ...trimmed)
  }

}
