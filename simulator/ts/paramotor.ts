import { GeoPointV } from "./dtypes"
import * as geo from "./geo/geo"
import { LandingZone } from "./geo/landingzone"
import { toDegrees, toRadians } from "./geo/trig"
import { ParaControls } from "./paracontrols"

export class Paramotor {
  // Constants
  public readonly turnRadius = 50 // Turn radius in meters with 1 toggle buried
  // public readonly yawRate = toDegrees(velocity / turnRadius) // Yaw degrees per second with 1 toggle buried
  public readonly climbRate = -3 // Meters of altitude lost per second
  public readonly groundSpeed = 12 // Meters per second
  public readonly glide = -this.groundSpeed / this.climbRate

  // State
  public loc: GeoPointV = {} as GeoPointV
  public pitch: number = 0
  public roll: number = 0

  // Inputs
  public controls: ParaControls = {left: 0, right: 0}

  public landed(lz: LandingZone): boolean {
    return this.loc !== undefined && this.loc.alt <= lz.destination.alt
  }

  /**
   * Pull the toggles to given deflections
   * @param controls how far to pull the toggles in meters
   */
  public setControls(controls: ParaControls): void {
    this.controls = controls
  }

  /**
   * Set the location of the paramotor
   */
  public setLocation(point: GeoPointV): void {
    this.loc = {...point}
  }

  /**
   * Return a copy of this Paramotor's state
   */
  public copy(): Paramotor {
    const p = new Paramotor()
    p.loc = this.loc && {...this.loc}
    p.pitch = this.pitch
    p.roll = this.roll
    p.controls = {...this.controls}
    return p
  }

  /**
   * Step forward dt seconds of time.
   * Takes into account position, speed, and toggle position.
   */
  public tick(dt: number): void {
    if (this.loc) {
      const vel = Math.hypot(this.loc.vE, this.loc.vN)
      const distance = vel * dt
      const control = this.controls.right - this.controls.left // [-1, 1]
      const toggle_yaw = distance * control / this.turnRadius
      const bearing = toDegrees(Math.atan2(this.loc.vE, this.loc.vN) + toggle_yaw)
      // TODO: Adjust climb toward climbRate based on gravity or WSE
      // Move lat,lng by distance and bearing
      const moved = geo.moveBearing(this.loc.lat, this.loc.lng, bearing, distance)
      this.loc.lat = moved.lat
      this.loc.lng = moved.lng
      this.loc.alt += this.loc.climb
      // Adjust velocity
      this.loc.vN = vel * Math.cos(toRadians(bearing))
      this.loc.vE = vel * Math.sin(toRadians(bearing))
    }
  }

  /**
   * Return the horizontal distance we could cover until landing
   */
  public flightDistanceRemaining(alt: number): number {
    const timeToGround = -alt / this.climbRate
    return this.groundSpeed * timeToGround
  }

}
