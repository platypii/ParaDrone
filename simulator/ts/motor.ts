import { MotorPosition } from "./dtypes"

/**
 * Represents a paradrone motor
 */
export class Motor {
  // Target motor position
  // 0 = no deflection, 255 = full deflection
  public target: number = 0

  // Current motor position estimate
  // 0 = no deflection, 255 = full deflection
  public position: number = 0

  // Current motor speed
  // -255 = full speed up, 255 = full speed down
  public speed: number = 0

  public constructor(target?: number, position?: number, speed?: number) {
    this.target = target || 0
    this.position = position || 0
    this.speed = speed || 0
  }

  /**
   * If the current position is not the target position, engage the motor
   */
  public update(dt: number) {
    // Calculate target delta, and motor speed to get there
    this.speed = motorSpeed(this.target - this.position)
    // Update position estimate
    this.position += this.speed * dt / 8
    this.position = normalizePosition(this.position)
  }

  public clone(): Motor {
    return new Motor(this.target, this.position, this.speed)
  }
}

/**
 * Return motor speed for a given position delta
 * @return motor speed in range -255..255
 */
function motorSpeed(delta: number): number {
  // Start slowing down when delta < 23
  let speed = delta * 10
  // Minimum speed 40
  if (speed < 0) speed -= 40
  if (speed > 0) speed += 40
  // Max speed 255
  if (speed < -255) return -255
  else if (speed > 255) return 255
  else return speed
}

function normalizePosition(position: number): number {
  return position < 0 ? 0 : (position > 255 ? 255 : position)
}
