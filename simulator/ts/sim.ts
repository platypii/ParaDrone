import { GeoPoint, GeoPointV } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Paramotor } from "./paramotor"
import { LandingScore, landing_score } from "./plan/planner"
import { search } from "./plan/search"

interface SimStep {
  loc: GeoPointV
  score: LandingScore
}

/**
 * Run a simulation and return the list of points
 */
export function sim(start: GeoPoint, lz: LandingZone): SimStep[] {
  const para = new Paramotor()
  const startV = {
    ...start,
    vN: 0,
    vE: para.groundSpeed,
    climb: para.climbRate
  }
  para.setLocation(startV)

  const actual: SimStep[] = []
  while (!para.landed(lz)) {
    const plan = search(lz, para)
    // TODO: Simulate motor input
    para.setControls(plan.controls())
    para.tick(1)
    if (para.loc) {
      const error = 0
      actual.push({loc: {...para.loc}, score: landing_score(lz, plan.end)})
    }
  }
  return actual
}
