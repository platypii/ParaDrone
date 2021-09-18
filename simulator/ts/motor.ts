
const epsilon = 0.8

// Motor parameters
// const spoolRadius = 0.008 // meters
const spoolCircumference = 0.05 // meters
// const motorRpm = 220 // no-load motor speed
const gearRatio = 27 // 1:27 gear reduction ratio
const encoderPpr = 12 // pulses per rotation
const strokeLength = 1.2 // meters

// Computed motor parameters
const ticksPerUnit = gearRatio * encoderPpr * strokeLength / (255 * spoolCircumference)
// const unitsPerSecond = 255 * spoolCircumference * motorRpm / (60 * strokeLength)

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

  // Simulated encoder ticks
  public ticks: number = 0

  // Physical reality
  public actualPosition: number = 0

  private lastTicks: number = 0

  /**
   * Update motor position estimate, and set motor speed
   * @param dt milliseconds since last update
   */
  public update(dt: number) {
    this.updateActual(dt)

    const tick_delta = this.ticks - this.lastTicks
    this.lastTicks = this.ticks

    // Position change based on encoder ticks
    const tick_distance = tick_delta / ticksPerUnit

    // Position change based on target speed
    const pwm_ticks_per_sec = expected_ticks_per_sec(this.speed, this.position)
    const pwm_distance = pwm_ticks_per_sec / ticksPerUnit * (dt / 1000)

    // Blended position estimate
    const use_ticks = 0.8
    this.position += tick_distance * use_ticks + pwm_distance * (1 - use_ticks)

    this.position = normalizePosition(this.position)

    // Close enough
    if (Math.abs(this.position - this.target) <= epsilon) {
      this.position = this.target
    }

    // Calculate target delta, and motor speed to get there
    this.speed = motorSpeed(this.target - this.position)
  }

  public clone(): Motor {
    const copy = new Motor()
    copy.target = this.target
    copy.position = this.position
    copy.speed = this.speed
    copy.ticks = this.ticks
    copy.actualPosition = this.actualPosition
    copy.lastTicks = this.lastTicks
    return copy
  }

  /**
   * Update the physical simulation of the motor (ground truth)
   */
  private updateActual(dt: number) {
    const previous = this.actualPosition
    // const forceRatio = this.forceRatio()

    // Update position based on expected ticks/sec
    const actualTicksPerSecond = expected_ticks_per_sec(this.speed, this.actualPosition)
    this.actualPosition += actualTicksPerSecond / ticksPerUnit * (dt / 1000)

    // Actual position can be -255 if it winds the wrong way
    if (this.actualPosition <= -255) {
      this.actualPosition = -255
    } else if (this.actualPosition >= 255) {
      this.actualPosition = 255
    }

    this.ticks += Math.floor(this.actualPosition * ticksPerUnit) - Math.floor(previous * ticksPerUnit)
  }

  /**
   * The amount of force pulling up on the toggles as a multiplier
   * @return toggle force multiplier from 1..2
   */
  public forceRatio(): number {
    return 1 + this.actualPosition / 255 // 1..2
  }
}

/**
 * Return motor speed for a given position delta
 * @return motor speed in range -255..255
 */
function motorSpeed(delta: number): number {
  // Start slowing down when delta < 20
  let speed = delta * 10
  // Minimum speed 60
  if (speed < 0) speed -= 60
  if (speed > 0) speed += 60
  // Max speed 255
  if (speed < -255) return -255
  else if (speed > 255) return 255
  else return Math.floor(speed)
}

function normalizePosition(position: number): number {
  return position < 0 ? 0 : (position > 255 ? 255 : position)
}

/**
 * Return expected ticks/sec for a given motor PWM speed
 *      ___/
 *     /
 */
function expected_ticks_per_sec(speed: number, position: number): number {
  if (speed < -64) {
    speed += 64
  } else if (speed > 64) {
    speed -= 64
  } else {
    speed = 0
  }
  speed *= 10.2

  // Adjust for expected force multiplier
  const forceRatio = 1 + position / 255
  return speed < 0 ? speed * forceRatio : speed / forceRatio
}