import { GeoPoint, GeoPointV } from "./dtypes"
import { getPlan } from "./flightcomputer"
import { LandingZone } from "./geo/landingzone"
import { Paramotor } from "./paramotor"
import { LandingScore, landing_score } from "./plan/planner"

interface SimStep {
  loc: GeoPointV
  score: LandingScore
}

/**
 * Run a simulation and return the list of points
 */
export function sim(start: GeoPoint, lz: LandingZone): SimStep[] {
  const startV = {
    ...start,
    vN: 0,
    vE: Paramotor.groundSpeed,
    climb: Paramotor.climbRate
  }
  const paramotor = new Paramotor()
  paramotor.setLocation(startV)

  const actual: SimStep[] = []
  while (!paramotor.landed(lz)) {
    const plan = getPlan(paramotor.loc!, lz)
    paramotor.setControls(plan.controls())
    paramotor.tick(1)
    if (paramotor.loc) {
      const error = 0
      actual.push({loc: {...paramotor.loc}, score: landing_score(lz, plan.end)})
    }
  }
  return actual
}
