import { GeoPointV, LatLng, LatLngAlt, Point, Point3V, Turn } from "../dtypes"
import * as geo from "./geo"
import { toDegrees, toRadians } from "./trig"

/**
 * Defines landing zone parameters.
 * Also defines the coordinate system, centered on the landing zone, north aligned.
 */
export class LandingZone {
  public readonly destination: GeoPointV
  /** Landing direction in radians */
  public readonly landingDirection: number

  // Destination, as origin of coordinate system
  public readonly dest: Point3V

  constructor(destination: LatLngAlt, landingDirection: number) {
    this.destination = {
      ...destination,
      vN: 0,
      vE: 0,
      climb: 0,
      millis: 0
    }
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
    const bear = Math.atan2(point.x, point.y)
    const dist = Math.sqrt(point.x * point.x + point.y * point.y)
    return geo.moveBearing(this.destination.lat, this.destination.lng, bear, dist)
  }

  /**
   * Convert x, y, alt agl coordinates to lat, lng, alt msl
   */
  public toLatLngAlt(point: Point3V): LatLngAlt {
    const bear = Math.atan2(point.x, point.y)
    const dist = Math.sqrt(point.x * point.x + point.y * point.y)
    return {
      ...geo.moveBearing(this.destination.lat, this.destination.lng, bear, dist),
      alt: point.alt + this.destination.alt
    }
  }

}

export const kpow = new LandingZone({lat: 47.239, lng: -123.143, alt: 84}, toRadians(32))
export const chelan = new LandingZone({lat: 47.864, lng: -119.944, alt: 378}, 0) // chelan
export const milehi = new LandingZone({lat: 40.162, lng: -105.164, alt: 1535}, 0) // mile hi

export const defaultLz = kpow
