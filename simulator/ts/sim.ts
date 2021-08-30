import { Autopilot } from "./autopilot"
import { GeoPoint, GeoPointV, MotorPosition, Wind } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Paraglider } from "./paraglider"
import { LandingScore, landing_score } from "./plan/planner"

export interface SimStep {
  loc: GeoPointV
  controls: MotorPosition
  score: LandingScore
}

/**
 * Run a simulation and return the list of points
 */
export function sim(start: GeoPointV, lz: LandingZone, wind: Wind): SimStep[] {
  const dt = 1 // step size in seconds
  const para = new Paraglider()
  const autopilot = new Autopilot(para, lz)
  const startV = {...start}
  para.setLocation(startV)

  const actual: SimStep[] = [{
    loc: startV,
    controls: para.toggles.controls(),
    score: landing_score(lz, autopilot.plan!.path.end)
  }]
  while (!para.landed(lz)) {
    para.tick(dt, wind)
    if (para.loc) {
      actual.push({
        loc: {...para.loc},
        controls: para.toggles.controls(),
        score: landing_score(lz, autopilot.plan!.path.end)
      })
    }
  }
  return actual
}
