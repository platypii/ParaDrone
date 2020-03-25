import { Circle, Point, Turn } from "../dtypes"
import { ParaControls } from "../paracontrols"
import { Path } from "./paths"
import { LineSegment } from "./segment-line"
import { toRadians } from "./trig"

// If turn is less than minimum_turn, then don't bury a toggle
const minimum_turn = toRadians(6)

export class TurnSegment {
  circle: Circle
  start: Point
  end: Point
  turn: Turn

  constructor(circle: Circle, start: Point, end: Point, turn: Turn) {
    this.circle = circle
    this.start = start
    this.end = end
    this.turn = turn
  }

  public controls(): ParaControls {
    const deflect = Math.min(this.arcs() / minimum_turn, 1)
    if (this.turn === Turn.Right) {
      return {left: 0, right: deflect}
    } else {
      return {left: deflect, right: 0}
    }
  }

  public length() {
    return this.circle.radius * this.arcs()
  }

  /**
   * Fly a given distance along the path.
   * If the end of the path is reached, extend a straight line out to infinity.
   */
  public fly(distance: number): Path {
    if (distance < 0) {
      console.error("flight distance cannot be negative", distance)
    }
    const len = this.length()
    if (distance < len) {
      const theta = this.angle1() + this.turn * distance / this.circle.radius
      const end = {
        x: this.circle.x + this.circle.radius * Math.sin(theta),
        y: this.circle.y + this.circle.radius * Math.cos(theta)
      }
      return new Path(new TurnSegment(this.circle, this.start, end, this.turn))
    } else {
      // Line extending from end of this turn
      const remaining = distance - len
      const dx = this.end.x - this.circle.x
      const dy = this.end.y - this.circle.y
      const extension = {
        x: this.end.x + this.turn * remaining * dy / this.circle.radius,
        y: this.end.y - this.turn * remaining * dx / this.circle.radius
      }
      return new Path(this, new LineSegment(this.end, extension))
    }
  }

  public render(): Point[] {
    const points = [this.start]
    const angle1 = this.angle1()
    const step = 0.05 // ~3 degrees
    const arcs = this.arcs()
    for (let delta = step; delta < arcs; delta += step) {
      const theta = angle1 + this.turn * delta
      points.push({
        x: this.circle.x + this.circle.radius * Math.sin(theta),
        y: this.circle.y + this.circle.radius * Math.cos(theta)
      })
    }
    points.push(this.end)
    return points
  }

  /**
   * The arc angle in radians
   */
  private arcs(): number {
    let arcs = this.turn * (this.angle2() - this.angle1())
    if (arcs < 0) arcs += 2 * Math.PI
    return arcs
  }

  /**
   * Angle from center of circle to start
   */
  private angle1(): number {
    return Math.atan2(this.start.x - this.circle.x, this.start.y - this.circle.y)
  }

  /**
   * Angle from center of circle to end
   */
  private angle2(): number {
    return Math.atan2(this.end.x - this.circle.x, this.end.y - this.circle.y)
  }
}
