import { Autopilot } from "./autopilot"
import * as device from "./device"
import { GeoPointV, LatLngAlt } from "./dtypes"
import * as geo from "./geo/geo"
import { defaultLz } from "./geo/landingzone"
import { DroneMap } from "./map/drone-map"
import { Paraglider } from "./paraglider"
import { Player } from "./player"
import { sim } from "./sim"
import * as test from "./test"
import { distance } from "./util"
import { ApScreen } from "./view/apscreen"
import { ErrorChart } from "./view/error-chart"
import { ToggleView } from "./view/toggleview"
import * as welcome from "./view/welcome"
import { Windgram } from "./view/windgram"

const lz = defaultLz
const para = new Paraglider()
const autopilot = new Autopilot(para, lz)
const wind = new Windgram()
const player = new Player(para, lz, wind)
const apScreen = new ApScreen()
const toggleView = new ToggleView()
const errorChart = new ErrorChart()

let start: GeoPointV
let map: DroneMap

const planEnabled = document.getElementById("plan-enabled") as HTMLInputElement
const planName = document.getElementById("plan-name") as HTMLElement
const planError = document.getElementById("plan-error") as HTMLElement
const simEnabled = document.getElementById("sim-enabled") as HTMLInputElement
const simSection = document.getElementById("sim-section") as HTMLElement
const simError = document.getElementById("sim-error") as HTMLElement

function init() {
  welcome.init()
  // Setup map
  map = new DroneMap(lz)
  map.setState({lz})
  map.onClick((e) => {
    player.stop()
    setStart({
      lat: e.lat,
      lng: e.lng,
      alt: 600 // TODO: Get elevation
    })
  })
  planEnabled.onclick = update
  simEnabled.onclick = update
  test.init(map, wind)
  device.init(para, lz)
  para.onLocationUpdate(() => update())
  update()

  // Default starting position (watertower)
  // setStart({lat: 47.24, lng: -123.163, alt: 800})

  // Setup wind
  wind.onChange(() => {
    update()
  })
}

function setStart(latlng: LatLngAlt) {
  start = {
    millis: 0, // new Date().getTime(),
    lat: latlng.lat,
    lng: latlng.lng,
    alt: latlng.alt,
    vN: 0,
    vE: para.groundSpeed, // TODO: handle init 0
    climb: para.climbRate // TODO: handle init 0
  }
  para.setLocation(start)
}

/**
 * Update views
 */
function update() {
  let plan: LatLngAlt[] = []
  let actual: GeoPointV[] = []
  if (planEnabled.checked && autopilot.plan && para.loc) {
    // Render current plan
    // const alt_agl = para.loc.alt - lz.destination.alt
    plan = autopilot.plan.render3(para)
      .map((p) => lz.toLatLngAlt(p))
    const plan_error = distance(autopilot.plan.path.end, lz.dest)
    planName.innerText = autopilot.plan.path.name
    planError.innerText = `${plan_error.toFixed(0)} m`
  }
  if (simEnabled.checked && para.loc) {
    // Simulate forward
    const simsteps = sim(para.clone(), lz, wind)
    actual = simsteps.map((s) => s.loc)
    const sim_error = geo.distancePoint(actual[actual.length - 1], lz.destination)
    simError.innerText = `${sim_error.toFixed(0)} m`
    // Update error chart
    errorChart.update(simsteps)

    // Update error chart
    errorChart.setFocus(simsteps.length)
  } else {
    simError.innerHTML = "&nbsp;"
  }
  // Show/hide
  simSection.style.maxHeight = simEnabled.checked ? "200px" : "0"

  // Update map
  map.setState({
    current: para.loc,
    plan,
    lz,
    actual
  })
  apScreen.update(para, lz, autopilot.plan)
  toggleView.update(para)
}

// Auto init
init()
