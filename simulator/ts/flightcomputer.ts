import { GeoPointV } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Path } from "./geo/paths"
import { search } from "./plan/search"

export function getPlan(loc: GeoPointV, lz: LandingZone): Path {
  // Convert to coordinate space
  const point = lz.toPoint3V(loc)
  return search(point, lz)
}
