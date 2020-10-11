import { Autopilot } from "./autopilot"
import { GeoPoint, GeoPointV } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Paraglider } from "./paraglider"
import { LandingScore, landing_score } from "./plan/planner"

interface SimStep {
  loc: GeoPointV
  score: LandingScore
}

/**
 * Run a simulation and return the list of points
 */
export function sim(start: GeoPoint, lz: LandingZone): SimStep[] {
  const para = new Paraglider()
  const autopilot = new Autopilot(para, lz)
  const startV = {
    ...start,
    vN: 0,
    vE: para.groundSpeed,
    climb: para.climbRate
  }
  para.setLocation(startV)

  const actual: SimStep[] = []
  while (!para.landed(lz)) {
    para.tick(1)
    if (para.loc) {
      const error = 0
      actual.push({loc: {...para.loc}, score: landing_score(lz, autopilot.plan!.end)})
    }
  }
  return actual
}
