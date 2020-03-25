import { GeoPointV } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Path } from "./geo/paths"
import { search } from "./plan/search"

export function getPlan(loc: GeoPointV, lz: LandingZone): Path {
  // Convert to coordinate space
  const currentLocation = lz.toPoint(loc)
  const point = {...currentLocation, alt: loc.alt, vx: loc.vE, vy: loc.vN, climb: loc.climb}
  return search(point, lz)
}
