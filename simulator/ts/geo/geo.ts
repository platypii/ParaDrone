import { LatLng } from "../dtypes"
import { mod360, toDegrees, toRadians } from "./trig"

const R = 6371000 // earth radius in meters

/**
 * Compute bearing from one point to another
 * @return bearing in degrees
 */
export function bearing(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const lat1r = toRadians(lat1)
  const lat2r = toRadians(lat2)
  // const latDelta = toRadians(lat2 - lat1)
  const lngDelta = toRadians(lng2 - lng1)

  const y = Math.sin(lngDelta) * Math.cos(lat2r)
  const x = Math.cos(lat1r) * Math.sin(lat2r) - Math.sin(lat1r) * Math.cos(lat2r) * Math.cos(lngDelta)
  return toDegrees(Math.atan2(y, x))
}

/**
 * Distance between two points in meters
 */
export function distance(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const lat1r = toRadians(lat1)
  const lat2r = toRadians(lat2)
  const latDelta = toRadians(lat2 - lat1)
  const lngDelta = toRadians(lng2 - lng1)

  const sinLat = Math.sin(latDelta / 2)
  const sinLng = Math.sin(lngDelta / 2)

  const a = (sinLat * sinLat) + (Math.cos(lat1r) * Math.cos(lat2r) * sinLng * sinLng)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

  return R * c
}
export function distancePoint(ll1: LatLng, ll2: LatLng): number {
  return distance(ll1.lat, ll1.lng, ll2.lat, ll2.lng)
}

/**
 * Moves the location along a bearing (radians) by a given distance (meters)
 */
export function moveBearing(lat_degrees: number, lng_degrees: number, bear: number, dist: number): LatLng {
  const d: number = dist / R

  const lat = toRadians(lat_degrees)
  const lng = toRadians(lng_degrees)

  // Precompute trig
  const sin_d = Math.sin(d)
  const cos_d = Math.cos(d)
  const sin_lat = Math.sin(lat)
  const cos_lat = Math.cos(lat)
  const sin_d_cos_lat = sin_d * cos_lat

  const lat2 = Math.asin(sin_lat * cos_d + sin_d_cos_lat * Math.cos(bear))
  const lng2 = lng + Math.atan2(Math.sin(bear) * sin_d_cos_lat, cos_d - sin_lat * Math.sin(lat2))

  const lat3 = toDegrees(lat2)
  const lng3 = mod360(toDegrees(lng2))

  return {lat: lat3, lng: lng3}
}
