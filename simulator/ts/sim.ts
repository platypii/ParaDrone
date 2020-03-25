import { GeoPoint, GeoPointV } from "./dtypes"
import { getPlan } from "./flightcomputer"
import { LandingZone } from "./geo/landingzone"
import { Paramotor } from "./paramotor"

/**
 * Run a simulation and return the list of points
 */
export function sim(start: GeoPoint, lz: LandingZone): GeoPointV[] {
  const startV = {
    ...start,
    vN: 0,
    vE: Paramotor.groundSpeed,
    climb: 0
  }
  const paramotor = new Paramotor()
  paramotor.setLocation(startV)

  const actual: GeoPointV[] = [startV]
  while (!paramotor.landed(lz)) {
    const plan = getPlan(paramotor.loc!, lz)
    paramotor.setControls(plan.controls())
    paramotor.tick(1)
    if (paramotor.loc) {
      actual.push({...paramotor.loc})
    }
  }
  return actual
}
