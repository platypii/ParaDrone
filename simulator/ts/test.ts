import { GeoPoint } from "./dtypes"
import { latLngGrid } from "./geo/hex"
import { kpow, LandingZone } from "./geo/landingzone"
import { DroneMap } from "./map/drone-map"
import { HeatLayer } from "./map/heat-layer"
import { LandingScore, landing_score } from "./plan/planner"
import { sim } from "./sim"
import { Windgram } from "./view/windgram"

interface TestScore extends LandingScore {
  location: GeoPoint
}

// size x size grid centered on lz
const gridDim = 15
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
  const input: GeoPoint[] = latLngGrid(lz.destination, gridDim, gridStep).map((ll) => {
    return {
      ...ll,
      alt: startAlt,
      millis: 0
    }
  })
  const output: TestScore[] = []
  sleepySearch(input, output, 0)
}

/**
 * Map over a list but with setTimeout to allow the browser to refresh.
 */
function sleepySearch(input: GeoPoint[], output: TestScore[], index: number): void {
  const batchSize = 24
  if (index >= input.length) {
    // Done
    btn.disabled = false
    // TODO: Un-set spinner
    return
  }
  for (let i = index; i < index + batchSize && i < input.length; i++) {
    output[i] = evaluate(input[i])
  }
  update(output)
  setTimeout(() => sleepySearch(input, output, index + batchSize), 80)
}

function evaluate(location: GeoPoint): TestScore {
  const score = {
    location,
    ...error(location, kpow)
  }

  // Update map layer
  // TODO: Color by score
  let color = "#1f15"
  if (score.distance > 100) {
    color = "#f115"
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

function error(loc: GeoPoint, lz: LandingZone): LandingScore {
  // Immediate path error
  // return plan_score(lz, search(loc, lz))
  // Simulated path error
  const plan = sim(loc, lz, wind)
  const landing = plan[plan.length - 1].loc
  const landingPoint = lz.toPoint3V(landing)
  return landing_score(lz, landingPoint)
}
