
const epsilon = 0.8

// Motor parameters
// const spoolRadius = 0.008 // meters
const spoolCircumference = 0.05 // meters
const motorRpm = 220 // no-load motor speed
const strokeLength = 1.2 // meters

// Computed motor parameters
const unitsPerSecond = 255 * spoolCircumference * motorRpm / (60 * strokeLength)

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
   * Update motor position estimate, and set motor speed
   * @param dt milliseconds since last update
   */
  public update(dt: number) {
    // Update position estimate
    this.position += unitsPerSecond * (this.speed / 255) * (dt / 1000)
    this.position = normalizePosition(this.position)

    // Close enough
    if (Math.abs(this.position - this.target) <= epsilon) {
      this.position = this.target
    }

    // Calculate target delta, and motor speed to get there
    this.speed = motorSpeed(this.target - this.position)
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
