import { GeoPointV } from "./dtypes"
import { latLngGrid } from "./geo/hex"
import { kpow, LandingZone } from "./geo/landingzone"
import { DroneMap } from "./map/drone-map"
import { HeatLayer } from "./map/heat-layer"
import { Paraglider } from "./paraglider"
import { LandingScore, landing_score } from "./plan/planner"
import { sim } from "./sim"
import { Windgram } from "./view/windgram"

interface TestScore extends LandingScore {
  location: GeoPointV
}

// size x size grid centered on lz
const gridDim = 13
const gridStep = 100 // meters
const startAlt = 600

const btn = document.getElementById("test-button") as HTMLButtonElement
let droneMap: DroneMap
let wind: Windgram

let startTime: number

const gridLayers: HeatLayer[] = []

export function init(map: DroneMap, windgram: Windgram) {
  droneMap = map
  wind = windgram
  // Bind test button
  btn.addEventListener("click", () => runTests())
}

function update(output: TestScore[]) {
  const avgError = avgScore(output)
  document.getElementById("test-score")!.innerText = `Score: ${avgError.score.toFixed(1)}`
  document.getElementById("test-distance")!.innerText = `Δ ${avgError.distance.toFixed(1)} m`
  document.getElementById("test-angle")!.innerText = `θ ${(avgError.angle * 180 / Math.PI).toFixed(1)}°`
  document.getElementById("test-n")!.innerText = `N ${output.length}`
  document.getElementById("test-time")!.innerText = `${(new Date().getTime() - startTime) / 1000}s`
}

/**
 * Perform a grid search
 */
function runTests() {
  btn.disabled = true
  // Clear layers
  for (const layer of gridLayers) {
    droneMap.removeLayer(layer)
  }
  gridLayers.length = 0
  startTime = new Date().getTime()
  const lz = kpow
  const para = new Paraglider()
  const input: GeoPointV[] = latLngGrid(lz.destination, gridDim, gridStep).map((ll) => {
    return {
      ...ll,
      alt: startAlt,
      vN: 0,
      vE: para.groundSpeed,
      climb: para.climbRate,
      millis: 0
    }
  })
  const output: TestScore[] = []
  sleepySearch(input, output, 0)
}

/**
 * Map over a list but with setTimeout to allow the browser to refresh.
 */
function sleepySearch(input: GeoPointV[], output: TestScore[], index: number): void {
  const batchSize = 24
  if (index >= input.length) {
    // Done
    btn.disabled = false
    return
  }
  for (let i = index; i < index + batchSize && i < input.length; i++) {
    output[i] = evaluate(input[i])
  }
  update(output)
  setTimeout(() => sleepySearch(input, output, index + batchSize), 80)
}

function evaluate(location: GeoPointV): TestScore {
  const score = {
    location,
    ...error(location, kpow)
  }

  // Update map layer
  let color = "#1e15"
  if (score.distance > 200) {
    color = "#e115" // red
  } else if (score.distance > 50) {
    color = "#ee15" // yellow
  }
  const layer = new HeatLayer(location, gridStep, color)
  gridLayers.push(layer)
  droneMap.addLayer(layer)

  return score
}

/**
 * Compute average scores from a list of scores
 */
function avgScore(scores: LandingScore[]): LandingScore {
  const total: LandingScore = {score: 0, distance: 0, angle: 0}
  let n = 0
  for (const err of scores) {
    if (isNaN(err.score)) {
      console.error("NaN plan score: " + err)
    } else {
      total.score += err.score
      total.distance += err.distance
      total.angle += err.angle
      n++
    }
  }
  total.score /= n
  total.distance /= n
  total.angle /= n
  return total
}

/**
 * Landing error, based on location and direction
 */
function error(loc: GeoPointV, lz: LandingZone): LandingScore {
  // Immediate path error
  // return plan_score(lz, search(loc, lz))
  // Simulated path error
  const plan = sim(new Paraglider(loc), lz, wind)
  const landing = plan[plan.length - 1].loc
  const landingPoint = lz.toPoint3V(landing)
  return landing_score(lz, landingPoint)
}
