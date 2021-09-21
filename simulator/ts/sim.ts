import { Autopilot } from "./autopilot"
import { GeoPointV, TogglePosition, Wind } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Paraglider } from "./paraglider"
import { LandingScore, landing_score } from "./plan/planner"

export interface SimStep {
  loc: GeoPointV
  controls: TogglePosition
  score: LandingScore
}

/**
 * Run a simulation and return the list of points.
 * Warning: para argument will be mutated! Pass para.clone().
 */
export function sim(para: Paraglider, lz: LandingZone, wind: Wind): SimStep[] {
  const dt = 1000 // step size in milliseconds
  const autopilot = new Autopilot(para, lz)

  const actual: SimStep[] = [{
    loc: {...para.loc!},
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
