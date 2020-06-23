import { GeoPointV, LatLng, LatLngAlt, Point, Point3V, Turn } from "../dtypes"
import { Paramotor } from "../paramotor"
import * as geo from "./geo"
import { toDegrees, toRadians } from "./trig"

/**
 * Defines landing zone parameters.
 * Also defines the coordinate system, centered on the landing zone, north aligned.
 */
export class LandingZone {
  public readonly destination: LatLngAlt
  /** Landing direction in radians */
  public readonly landingDirection: number

  // Ground length of final approach
  public readonly finalDistance: number = 150 // meters

  // Destination, as origin of coordinate system
  public readonly dest: Point3V

  constructor(destination: LatLngAlt, landingDirection: number) {
    this.destination = destination
    this.landingDirection = landingDirection
    this.dest = {
      x: 0,
      y: 0,
      alt: 0,
      vx: Math.sin(landingDirection),
      vy: Math.cos(landingDirection),
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
      climb: Paramotor.climbRate
    }
  }

  /**
   * Landing pattern: start of base leg
   */
  public startOfBase(turn: Turn): Point3V {
    return {
      x: this.finalDistance * (-this.dest.vx + turn * this.dest.vy),
      y: this.finalDistance * (-turn * this.dest.vx - this.dest.vy),
      alt: this.dest.alt + 2 * this.finalDistance / Paramotor.glide, // TODO: Doesn't account for turns
      vx: -this.dest.vx,
      vy: -this.dest.vy,
      climb: Paramotor.climbRate
    }
  }

  /**
   * Landing pattern: start of downwind leg
   */
  public startOfDownwind(turn: Turn): Point3V {
    return {
      x: this.finalDistance * turn * this.dest.vy,
      y: -this.finalDistance * turn * this.dest.vx,
      alt: this.dest.alt + 3 * this.finalDistance / Paramotor.glide, // TODO: Doesn't account for turns
      vx: -this.dest.vx,
      vy: -this.dest.vy,
      climb: Paramotor.climbRate
    }
  }

  /**
   * Convert lat, lng to x, y meters centered at destination
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
   * Convert lat, lng, alt msl to x, y meters, alt agl, centered at destination
   */
  public toPoint3V(ll: GeoPointV): Point3V {
    const bearing = toRadians(geo.bearing(this.destination.lat, this.destination.lng, ll.lat, ll.lng))
    const distance = geo.distance(this.destination.lat, this.destination.lng, ll.lat, ll.lng)
    return {
      x: distance * Math.sin(bearing),
      y: distance * Math.cos(bearing),
      alt: ll.alt - this.destination.alt,
      vx: ll.vE,
      vy: ll.vN,
      climb: ll.climb
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

  /**
   * Convert x, y, alt agl coordinates to lat, lng, alt msl
   */
  public toLatLngAlt(point: Point3V): LatLngAlt {
    const bear = toDegrees(Math.atan2(point.x, point.y))
    const dist = Math.sqrt(point.x * point.x + point.y * point.y)
    return {
      ...geo.moveBearing(this.destination.lat, this.destination.lng, bear, dist),
      alt: point.alt + this.destination.alt
    }
  }

}

export const kpow = new LandingZone({lat: 47.239, lng: -123.143, alt: 84}, toRadians(32))
