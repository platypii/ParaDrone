import { Circle, Point, PointV, TogglePosition, Turn } from "../dtypes"
import { Path } from "./path"
import { SegmentLine } from "./segment-line"
import { toRadians } from "./trig"

// If turn is less than minimum_turn, then don't bury a toggle
const minimum_turn = toRadians(8)
// If turn is at least maximum_turn, then do bury a toggle
const maximum_turn = toRadians(30)

/**
 * 2D curved segment with fixed turn radius.
 */
export class SegmentTurn {
  circle: Circle
  start: PointV
  end: PointV
  turn: Turn

  constructor(circle: Circle, start: Point, end: Point, turn: Turn) {
    this.circle = circle
    this.turn = turn
    const start_dx = start.x - circle.x
    const start_dy = start.y - circle.y
    this.start = {
      ...start,
      vx: start_dx / circle.radius,
      vy: start_dy / circle.radius
    }
    const end_dx = end.x - circle.x
    const end_dy = end.y - circle.y
    this.end = {
      ...end,
      vx: end_dx / circle.radius,
      vy: end_dy / circle.radius
    }
  }

  public controls(): TogglePosition {
    // Sigmoid-ish activation function.
    const arc = this.arcs()
    let activation = 1
    if (arc < minimum_turn) {
      // 0..min_turn
      activation = 0.01 * arc / minimum_turn
    } else if (arc <= maximum_turn) {
      // min_turn..max_turn
      activation = 0.01 + 0.99 * (arc - minimum_turn) / (maximum_turn - minimum_turn)
    }
    // Normalized and ranged to match AP protocol
    const deflect = Math.round(activation * 255)
    if (deflect < 0 || deflect > 255) {
      throw new Error("Invalid deflection")
    }
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
    if (distance <= 0) {
      console.error("turn flight distance must be positive", distance)
    }
    const len = this.length()
    if (distance < len) {
      const theta = this.angle1() + this.turn * distance / this.circle.radius
      const end = {
        x: this.circle.x + this.circle.radius * Math.sin(theta),
        y: this.circle.y + this.circle.radius * Math.cos(theta)
      }
      return new Path("turn-fly", new SegmentTurn(this.circle, this.start, end, this.turn))
    } else {
      // Line extending from end of this turn
      const remaining = distance - len
      const dx = this.end.x - this.circle.x
      const dy = this.end.y - this.circle.y
      const extension = {
        x: this.end.x + this.turn * remaining * dy / this.circle.radius,
        y: this.end.y - this.turn * remaining * dx / this.circle.radius
      }
      return new Path("turn-fly", this, new SegmentLine(this.end, extension))
    }
  }

  public render(): Point[] {
    const points: Point[] = [this.start]
    const angle1 = this.angle1()
    const step = 0.05 // ~3 degrees
    const arc = this.arcs()
    for (let delta = step; delta < arc; delta += step) {
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
   * Always positive
   */
  private arcs(): number {
    let arc = this.turn * (this.angle2() - this.angle1())
    if (arc < 0) arc += 2 * Math.PI
    return arc
  }

  /**
   * Angle from center of circle to start
   * @returns the angle in radians [-\pi, \pi] between the positive x-axis and the start point
   */
  private angle1(): number {
    return Math.atan2(this.start.x - this.circle.x, this.start.y - this.circle.y)
  }

  /**
   * Angle from center of circle to end
   * @returns the angle in radians [-\pi, \pi] between the positive x-axis and the end point
   */
  private angle2(): number {
    return Math.atan2(this.end.x - this.circle.x, this.end.y - this.circle.y)
  }
}
