import { GeoPoint } from "./dtypes"
import { kpow, LandingZone } from "./geo/landingzone"
import { LandingScore, landing_score } from "./plan/planner"
import { sim } from "./sim"
import { Windgram } from "./view/windgram"

// size x size grid centered on lz
const gridSize = 10
const gridStep = 200 // meters
const gridBound = gridStep * (gridSize - 1) / 2
const startAlt = 600

const wind = new Windgram()

const btn = document.getElementById("test-button") as HTMLButtonElement

export function init() {
  // Bind test button
  btn.onclick = () => {
    btn.disabled = true
    setTimeout(runTests, 1)
  }
}

function runTests() {
  const start = new Date().getTime()
  const avgError = gridSearch()
  document.getElementById("test-score")!.innerText = `Score: ${avgError.score.toFixed(1)}`
  document.getElementById("test-distance")!.innerText = `Δ ${avgError.distance.toFixed(1)} m`
  document.getElementById("test-angle")!.innerText = `θ ${(avgError.angle * 180 / Math.PI).toFixed(1)}°`
  document.getElementById("test-n")!.innerText = `N ${gridSize * gridSize}`
  document.getElementById("test-time")!.innerText = `${(new Date().getTime() - start) / 1000}s`
  btn.disabled = false
}

/**
 * Perform a grid search and return average landing accuracy
 */
function gridSearch(): LandingScore {
  const lz = kpow
  const total: LandingScore = {
    score: 0, distance: 0, angle: 0
  }
  let n = 0
  for (let x = -gridBound; x <= gridBound; x += gridStep) {
    for (let y = -gridBound; y <= gridBound; y += gridStep) {
      const loc: GeoPoint = {
        ...lz.toLatLng({x, y}),
        alt: startAlt,
        millis: 0
      }
      const err = error(loc, kpow)
      if (isNaN(err.score)) {
        console.error("NaN plan score: " + err)
      } else {
        total.score += err.score
        total.distance += err.distance
        total.angle += err.angle
        n++
      }
    }
  }
  total.score /= n
  total.distance /= n
  total.angle /= n
  return total
}

function error(loc: GeoPoint, lz: LandingZone): LandingScore {
  // Immediate path error
  // return plan_score(lz, search(loc, lz))
  // Simulated path error
  const plan = sim(loc, lz, wind)
  const landing = plan[plan.length - 1].loc
  const landingPoint = lz.toPoint3V(landing)
  return landing_score(lz, landingPoint)
}
