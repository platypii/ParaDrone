import { Autopilot } from "./autopilot"
import { GeoPointV, LatLngAlt } from "./dtypes"
import * as geo from "./geo/geo"
import { defaultLz } from "./geo/landingzone"
import { DroneMap } from "./map/drone-map"
import { MarkerLayer } from "./map/marker-layer"
import { Paraglider } from "./paraglider"
import * as player from "./player"
import { sim } from "./sim"
import * as test from "./test"
import { distance } from "./util"

const lz = defaultLz
const para = new Paraglider()
const autopilot = new Autopilot(para, lz)

let start: GeoPointV
let map: DroneMap

const planEnabled = document.getElementById("plan-enabled") as HTMLInputElement
const planName = document.getElementById("plan-name") as HTMLElement
const planError = document.getElementById("plan-error") as HTMLElement
const simEnabled = document.getElementById("sim-enabled") as HTMLInputElement
const simError = document.getElementById("sim-error") as HTMLElement

export function init() {
  // Setup map
  map = new DroneMap(lz)
  map.setState({lz})
  map.onClick((e) => {
    player.stop()
    setStart({
      lat: e.lat,
      lng: e.lng,
      alt: 800 // TODO: Get elevation
    })
  })
  const hoverLayer = new MarkerLayer("hover", "img/pin.svg")
  map.addLayer(hoverLayer)
  map.onMouseMove((e) => {
    hoverLayer.setLocation(e)
  })
  planEnabled.onclick = update
  simEnabled.onclick = update
  test.init()
  para.onLocationUpdate(() => {
    update()
  })
  update()
}

function setStart(latlng: LatLngAlt) {
  start = {
    millis: new Date().getTime(),
    lat: latlng.lat,
    lng: latlng.lng,
    alt: latlng.alt,
    vN: 0,
    vE: para.groundSpeed, // TODO: handle init 0
    climb: para.climbRate // TODO: handle init 0
  }
  para.setLocation(start)

  // Start player
  player.start(para, lz)
}

/**
 * Update views
 */
function update() {
  let plan: LatLngAlt[] = []
  let actual: GeoPointV[] = []
  if (planEnabled.checked && autopilot.plan && para.loc) {
    // Render current plan
    const alt_agl = para.loc.alt - lz.destination.alt
    plan = autopilot.plan.render3(para, alt_agl)
      .map((p) => lz.toLatLngAlt(p))
    const plan_error = distance(autopilot.plan.end, lz.dest)
    planName.innerText = autopilot.plan.name
    planError.innerText = `${plan_error.toFixed(1)}m`
  }
  if (simEnabled.checked && start) {
    // Simulate forward
    const simsteps = sim(start, lz)
    actual = simsteps.map((s) => s.loc)
    const sim_error = geo.distancePoint(actual[actual.length - 1], lz.destination)
    simError.innerText = `${sim_error.toFixed(1)}m`
  } else {
    simError.innerHTML = "&nbsp;"
  }

  // Update map
  map.setState({
    current: para.loc,
    plan,
    lz,
    actual
  })
  updateApScreen()
}

function updateApScreen() {
  if (para.loc) {
    const dist = geo.distancePoint(para.loc, lz.destination)
    const alt = para.loc.alt - lz.destination.alt
    const vel = Math.hypot(para.loc.vN, para.loc.vE) * 2.23694 // mph
    document.getElementById("ap-latlng")!.innerText = `${para.loc.lat.toFixed(6)}, ${para.loc.lng.toFixed(6)}`
    document.getElementById("ap-landingzone")!.innerText = `LZ: ${dist.toFixed(0)} m`
    document.getElementById("ap-alt")!.innerText = `${alt.toFixed(0)} m AGL`
    document.getElementById("ap-speed")!.innerText = `${vel.toFixed(0)} mph`
  } else {
    document.getElementById("ap-latlng")!.innerText = ""
    document.getElementById("ap-landingzone")!.innerText = ""
    document.getElementById("ap-alt")!.innerText = ""
  }
}
