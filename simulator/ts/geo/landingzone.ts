import { LatLng, LatLngAlt, Point, Point3V, PointV, Turn } from "../dtypes"
import * as geo from "./geo"
import { toDegrees, toRadians } from "./trig"
import { Paramotor } from "../paramotor"

/**
 * Defines landing zone parameters.
 * Also defines the coordinate system, centered on the landing zone, north aligned.
 */
export class LandingZone {
  public readonly destination: LatLngAlt
  public readonly landingDirection: number // radians

  // Ground length of final approach
  public readonly finalDistance: number = 100 // meters

  // Destination, as origin of coordinate system
  public readonly dest: Point3V

  constructor(destination: LatLngAlt, landingDirection: number) {
    this.destination = destination
    this.landingDirection = landingDirection
    this.dest = {
      x: 0,
      y: 0,
      alt: destination.alt,
      vx: Math.cos(this.landingDirection),
      vy: Math.sin(this.landingDirection),
      climb: 0
    }
  }

  /**
   * Landing pattern: start of final approach
   */
  public startOfFinal(): Point3V {
    return {
      x: -this.finalDistance * this.dest.vx,
      y: -this.finalDistance * this.dest.vy,
      alt: this.dest.alt + this.finalDistance / Paramotor.glide,
      vx: this.dest.vx,
      vy: this.dest.vy,
      climb: Paramotor.descentRate
    }
  }

  /**
   * Landing pattern: start of base leg
   */
  public startOfBase(turn: Turn): Point3V {
    return {
      x: -this.finalDistance * (this.dest.vx + this.dest.vy),
      y: -this.finalDistance * (turn * this.dest.vx + this.dest.vy),
      alt: this.dest.alt + 2 * this.finalDistance / Paramotor.glide, // TODO: Doesn't account for turns
      vx: -turn * this.dest.vy,
      vy: turn * this.dest.vx,
      climb: Paramotor.descentRate
    }
  }

  /**
   * Convert lat, lng to x, y meters centered at current location
   */
  public toPoint(ll: LatLng): Point {
    const bearing = toRadians(geo.bearing(this.destination.lat, this.destination.lng, ll.lat, ll.lng))
    const distance = geo.distance(this.destination.lat, this.destination.lng, ll.lat, ll.lng)
    return {
      x: distance * Math.sin(bearing),
      y: distance * Math.cos(bearing)
    }
  }

  /**
   * Convert x, y coordinates to lat, lng
   */
  public toLatLng(point: Point): LatLng {
    const bear = toDegrees(Math.atan2(point.x, point.y))
    const dist = Math.sqrt(point.x * point.x + point.y * point.y)
    return geo.moveBearing(this.destination.lat, this.destination.lng, bear, dist)
  }

}

export const kpow = new LandingZone({lat: 47.239, lng: -123.143, alt: 84}, 1.0472)
