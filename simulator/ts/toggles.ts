import { MotorPosition } from "./dtypes"
import { Motor } from "./motor"

/**
 * Represents toggle position for a paraglider
 */
export class Toggles {
  // Toggle motors
  public readonly left: Motor
  public readonly right: Motor

  constructor(left?: Motor, right?: Motor) {
    this.left = left || new Motor()
    this.right = right || new Motor()
  }

  public controls(): MotorPosition {
    return {
      left: this.left.target,
      right: this.right.target
    }
  }

  /**
   * If the current position is not the target position, engage the motors
   */
  public update(dt: number) {
    this.left.update(dt)
    this.right.update(dt)
  }

  public setTarget(left: number, right: number) {
    this.left.target = left
    this.right.target = right
    // TODO: update motor speeds?
  }

  /**
   * Sustained rate of speed for current toggle position (m/s)
   */
  public turnSpeed(): number {
    const minSpeed = 6 // m/s
    const maxSpeed = 12 // m/s
    return maxSpeed - (this.left.position + this.right.position) / 512 * (maxSpeed - minSpeed)
  }

  /**
   * Left/right balance of current toggle position
   */
  public turnBalance(): number {
    return (this.right.position - this.left.position) / 255 // [-1..1]
  }

  public clone(): Toggles {
    return new Toggles(this.left.clone(), this.right.clone())
  }
}
