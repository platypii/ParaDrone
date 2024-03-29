import { Cartesian3 } from "cesium"
import { LatLng, LatLngAlt } from "../dtypes"

export function formatLatLng(point: LatLng): string {
  if (point && point.lat !== undefined && point.lng !== undefined) {
    return point.lat.toFixed(6) + ", " + point.lng.toFixed(6)
  } else {
    return ""
  }
}

export function formatLatLngAlt(point: LatLngAlt): string {
  if (point && point.lat !== undefined && point.lng !== undefined) {
    return point.lat.toFixed(6) + ", " + point.lng.toFixed(6) + ", " + point.alt.toFixed(0) + " m"
  } else {
    return ""
  }
}

export function latLngToCart(ll: LatLngAlt): Cartesian3 {
  return Cartesian3.fromDegrees(ll.lng, ll.lat, ll.alt)
}
