import { GeoPointV, Wind } from "./dtypes"
import * as geo from "./geo/geo"
import { LandingZone } from "./geo/landingzone"
import { MotorPosition } from "./paracontrols"
import { Toggles } from "./toggles"

/**
 * Represents a paraglider, with flight characteristics, current location, orientation, and toggle position.
 */
export class Paraglider {
  // Constants
  public readonly turnRadius = 40 // Turn radius in meters with 1 toggle buried
  // public readonly yawRate = toDegrees(velocity / turnRadius) // Yaw degrees per second with 1 toggle buried
  public readonly climbRate = -3 // Meters of altitude lost per second
  public readonly groundSpeed = 12 // Meters per second
  public readonly glide = -this.groundSpeed / this.climbRate

  // State
  public readonly toggles: Toggles = new Toggles()
  public loc?: GeoPointV
  public heading: number = 0 // radians
  public pitch: number = 0
  public roll: number = 0

  private readonly locationListeners: Array<(point: GeoPointV) => void> = []

  public landed(lz: LandingZone): boolean {
    return this.loc !== undefined && this.loc.alt <= lz.destination.alt
  }

  /**
   * Pull the toggles to given deflections
   * @param targetPosition how far to pull the toggles in meters
   */
  public setControls(targetPosition: MotorPosition): void {
    this.toggles.setTarget(targetPosition)
  }

  /**
   * Set the location of the paramotor
   */
  public setLocation(point: GeoPointV): void {
    this.loc = {...point}
    // Notify listeners
    for (const listener of this.locationListeners) {
      listener(this.loc)
    }
    // TODO: Update internal heading model
    this.heading = Math.atan2(point.vE, point.vN)
  }

  /**
   * Add a location listener
   */
  public onLocationUpdate(cb: (point: GeoPointV) => void): void {
    this.locationListeners.push(cb)
  }

  /**
   * Return a copy of this Paramotor's state
   */
  public copy(): Paraglider {
    const p = new Paraglider()
    p.loc = this.loc && {...this.loc}
    p.pitch = this.pitch
    p.roll = this.roll
    p.toggles.currentPosition = {...this.toggles.currentPosition}
    p.toggles.targetPosition = {...this.toggles.targetPosition}
    return p
  }

  /**
   * Step forward dt seconds of time.
   * Takes into account position, speed, and toggle position.
   * TODO: Adjust velocities on WSE
   */
  public tick(wind: Wind, dt: number): void {
    const alpha = 0.5 // moving average filter applied to toggle inputs to simulate the fact that speed and direction don't change instantly
    if (this.loc) {
      // Ground speed
      const vE = this.loc.vE
      const vN = this.loc.vN
      const groundSpeed = Math.sqrt(vE * vE + vN * vN)

      // Air speed
      const vEair = this.loc.vE - wind.vE
      const vNair = this.loc.vN - wind.vN
      let airSpeed = Math.sqrt(vEair * vEair + vNair * vNair)

      // Update glider turn (yaw) rate and speed based on toggle position
      // TODO: Special case for straight? Faster?
      const turnRate = this.toggles.turnRate()
      airSpeed += (turnRate.speed - airSpeed) * alpha
      const distance = airSpeed * dt
      // Air bearing
      const startBearing = Math.atan2(vEair, vNair)
      const endBearing = startBearing + distance * turnRate.balance / this.turnRadius
      // The proof of this is beautiful:
      const chordBearing = startBearing + distance * turnRate.balance / this.turnRadius / 2

      // Move lat,lng by distance and bearing of flight path relative to wind
      const prewind = geo.moveBearing(this.loc.lat, this.loc.lng, chordBearing, distance)
      // Move lat,lng by wind drift
      const postwind = geo.moveBearing(prewind.lat, prewind.lng, wind.bear(), wind.vel() * dt)
      this.loc.lat = postwind.lat
      this.loc.lng = postwind.lng

      this.loc.alt += this.loc.climb * dt
      this.loc.climb += (this.climbRate - this.loc.climb) * alpha

      // Adjust velocity
      this.loc.vE = airSpeed * Math.sin(endBearing) + wind.vE
      this.loc.vN = airSpeed * Math.cos(endBearing) + wind.vN
      // Update motor position
      this.toggles.tick(dt)

      // Notify listeners
      for (const listener of this.locationListeners) {
        listener(this.loc)
      }
    }
  }

  /**
   * Return the horizontal distance we could cover in a given amount of vertical distance
   */
  public flightDistanceRemaining(alt: number): number {
    const timeToGround = -alt / this.climbRate
    return this.groundSpeed * timeToGround
  }
}
