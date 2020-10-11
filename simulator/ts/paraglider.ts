import { GeoPointV } from "./dtypes"
import * as geo from "./geo/geo"
import { LandingZone } from "./geo/landingzone"
import { MotorPosition } from "./paracontrols"
import { Toggles } from "./toggles"

/**
 * Represents a paraglider, with flight characteristics, current location, orientation, and toggle position.
 */
export class Paraglider {
  // Constants
  public readonly turnRadius = 50 // Turn radius in meters with 1 toggle buried
  // public readonly yawRate = toDegrees(velocity / turnRadius) // Yaw degrees per second with 1 toggle buried
  public readonly climbRate = -3 // Meters of altitude lost per second
  public readonly groundSpeed = 12 // Meters per second
  public readonly glide = -this.groundSpeed / this.climbRate

  // State
  public readonly toggles: Toggles = new Toggles()
  public loc?: GeoPointV
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
   */
  public tick(dt: number): void {
    const alpha = 0.5 // moving average filter applied to toggle inputs to simulate the fact that speed and direction don't change instantly
    if (this.loc) {
      let vel = Math.sqrt(this.loc.vE * this.loc.vE + this.loc.vN * this.loc.vN)
      // Update glider speed based on toggle position
      const turnRate = this.toggles.turnRate()
      vel += (turnRate.speed - vel) * alpha
      // Update glider turn (yaw) rate based on toggle position
      const distance = vel * dt
      // The proof of this is beautiful:
      const bearing = Math.atan2(this.loc.vE, this.loc.vN) + distance * turnRate.balance / this.turnRadius / 2
      // TODO: Adjust climb toward climbRate based on gravity or WSE
      // Move lat,lng by distance and bearing
      const moved = geo.moveBearing(this.loc.lat, this.loc.lng, bearing, distance)
      this.loc.lat = moved.lat
      this.loc.lng = moved.lng
      this.loc.alt += this.loc.climb
      // Adjust velocity
      this.loc.vN = vel * Math.cos(bearing)
      this.loc.vE = vel * Math.sin(bearing)
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
