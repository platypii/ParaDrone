import { MotorPosition } from "./paracontrols"

/**
 * Represents toggle position for a paraglider
 */
export class Toggles {
  // Toggle controls
  public currentPosition: MotorPosition = {left: 0, right: 0}
  public targetPosition: MotorPosition = {left: 0, right: 0}

  /**
   * If the current position is not the target position, engage the motors
   */
  public tick(dt: number) {
    // Calculate target delta, and motor speed to get there
    const speedLeft = motorSpeed(this.targetPosition.left - this.currentPosition.left)
    const speedRight = motorSpeed(this.targetPosition.right - this.currentPosition.right)
    // Update position estimate
    this.currentPosition.left += speedLeft * dt / 8
    this.currentPosition.right += speedRight * dt / 8
    this.currentPosition.left = normalizePosition(this.currentPosition.left)
    this.currentPosition.right = normalizePosition(this.currentPosition.right)
  }

  public setTarget(targetPosition: MotorPosition) {
    this.targetPosition = targetPosition
  }

  /**
   * Sustained rate of speed for current toggle position (m/s)
   */
  public turnSpeed(): number {
    const minSpeed = 6 // m/s
    const maxSpeed = 12 // m/s
    return maxSpeed - (this.currentPosition.left + this.currentPosition.right) / 512 * (maxSpeed - minSpeed)
  }

  /**
   * Left/right balance of current toggle position
   */
  public turnBalance(): number {
    return (this.currentPosition.right - this.currentPosition.left) / 255 // [-1..1]
  }
}

/**
 * Return motor speed for a given position delta
 * @return motor speed in range -255..255
 */
function motorSpeed(delta: number): number {
  // Start slowing down when delta < 23
  let speed = delta * 10
  // Minimum speed 32
  if (speed < 0) speed -= 32
  if (speed > 0) speed += 32
  // Max speed 255
  if (speed < -255) return -255
  else if (speed > 255) return 255
  else return speed
}

function normalizePosition(position: number): number {
  return position < 0 ? 0 : (position > 255 ? 255 : position)
}
