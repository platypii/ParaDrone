import { GeoPointV, Wind } from "./dtypes"
import * as geo from "./geo/geo"
import { LandingZone } from "./geo/landingzone"
import { Toggles } from "./toggles"

const zerowind: Wind = {
  vE: 0,
  vN: 0,
  vel: () => 0,
  bear: () => 0
}

/**
 * Represents a paraglider, with flight characteristics, current location, orientation, and toggle position.
 */
export class Paraglider {
  // Constants
  public readonly turnRadius = 40 // Turn radius in meters with 1 toggle buried
  // public readonly yawRate = toDegrees(velocity / turnRadius) // Yaw degrees per second with 1 toggle buried
  public readonly climbRate = -3 // m/s
  public readonly groundSpeed = 12 // m/s
  public readonly glide = -this.groundSpeed / this.climbRate

  // State
  public readonly toggles: Toggles
  public loc?: GeoPointV

  // TODO: Heading, pitch, roll Orientation

  private readonly locationListeners: Array<(point: GeoPointV) => void> = []

  constructor(loc?: GeoPointV, toggles?: Toggles) {
    this.toggles = toggles || new Toggles()
    this.loc = loc
  }

  public landed(lz: LandingZone): boolean {
    return this.loc !== undefined && this.loc.alt <= lz.destination.alt
  }

  /**
   * Set the location of the paramotor
   */
  public setLocation(point: GeoPointV): void {
    this.loc = point
    // Notify listeners
    for (const listener of this.locationListeners) {
      listener(this.loc)
    }
    // TODO: Update internal heading model
    // this.heading = Math.atan2(point.vE, point.vN)
  }

  /**
   * Add a location listener
   */
  public onLocationUpdate(cb: (point: GeoPointV) => void): void {
    this.locationListeners.push(cb)
  }

  /**
   * Step forward dt seconds of time.
   */
  public tick(dt: number, wind: Wind): void {
    if (this.loc) {
      const next = this.predict(dt, wind)!

      // Update toggle position
      this.toggles.update(dt)

      // Notify listeners (eg- autopilot planner)
      this.setLocation(next)
    }
  }

  /**
   * Predict where the glider will be in dt seconds.
   * Takes into account position, speed, and toggle position.
   * TODO: Adjust velocities on WSE
   */
  public predict(dt: number, wind: Wind = zerowind): GeoPointV | undefined {
    const alpha = 0.5 // moving average filter applied to toggle inputs to simulate the fact that speed and direction don't change instantly
    if (this.loc) {
      // Air speed
      const vEair = this.loc.vE - wind.vE
      const vNair = this.loc.vN - wind.vN
      let airSpeed = Math.sqrt(vEair * vEair + vNair * vNair)

      // Update glider turn (yaw) rate and speed based on toggle position
      // TODO: Special case for straight? Faster?
      const turnSpeed = this.toggles.turnSpeed()
      const turnBalance = this.toggles.turnBalance()
      airSpeed += (turnSpeed - airSpeed) * alpha
      const distance = airSpeed * dt
      // Air bearing
      const startBearing = Math.atan2(vEair, vNair)
      const endBearing = startBearing + distance * turnBalance / this.turnRadius
      // The proof of this is beautiful:
      const chordBearing = startBearing + distance * turnBalance / this.turnRadius / 2

      // Move lat,lng by distance and bearing of flight path relative to wind
      const prewind = geo.moveBearing(this.loc.lat, this.loc.lng, chordBearing, distance)
      // Move lat,lng by wind drift
      const postwind = geo.moveBearing(prewind.lat, prewind.lng, wind.bear(), wind.vel() * dt)

      // Adjust velocity
      const vE = airSpeed * Math.sin(endBearing) + wind.vE
      const vN = airSpeed * Math.cos(endBearing) + wind.vN

      // Adjust altitude
      const alt = this.loc.alt + this.loc.climb * dt
      const climb = this.loc.climb + (this.climbRate - this.loc.climb) * alpha

      return {
        millis: this.loc.millis + 1000 * dt,
        ...postwind,
        alt,
        climb,
        vE,
        vN
      }
    } else {
      return undefined
    }
  }

  /**
   * Return the horizontal distance we could cover in a given amount of vertical distance
   */
  public flightDistanceRemaining(alt: number): number {
    const timeToGround = -alt / this.climbRate
    return this.groundSpeed * timeToGround
  }

  public clone(): Paraglider {
    return new Paraglider(this.loc, this.toggles.clone())
  }
}
