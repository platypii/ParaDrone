import { GeoPointV, LatLngAlt } from "./dtypes"
import * as geo from "./geo/geo"
import { defaultLz } from "./geo/landingzone"
import { DroneMap } from "./map/drone-map"
import { MarkerLayer } from "./map/marker-layer"
import { Paramotor } from "./paramotor"
import { search } from "./plan/search"
import * as player from "./player"
import { sim } from "./sim"
import * as test from "./test"
import { distance } from "./util"

const lz = defaultLz
const para = new Paramotor()

let start: GeoPointV
let loc: GeoPointV
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
    // console.log("WTF", e)
    // TODO: Show billboard
    hoverLayer.setLocation(e)
  })
  planEnabled.onclick = update
  simEnabled.onclick = update
  test.init()
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

  // Update UI
  loc = start
  update()

  // Start player
  player.start(start, lz, (next) => {
    loc = next
    update()
  })
}

/**
 * Update views
 */
function update() {
  if (loc) {
    para.setLocation(loc)
    // Compute full plan
    const alt_agl = loc.alt - lz.destination.alt
    const plan = search(lz, para)
    const rendered = plan.render3(para, alt_agl)
      .map((p) => lz.toLatLngAlt(p))
    const plan_error = distance(plan.end, lz.dest)
    planName.innerText = plan.name
    planError.innerText = `${plan_error.toFixed(1)}m`

    // Simulate forward
    let actual: GeoPointV[] = []
    if (simEnabled.checked) {
      const simsteps = sim(start, lz)
      actual = simsteps.map((s) => s.loc)
      const sim_error = geo.distancePoint(actual[actual.length - 1], lz.destination)
      simError.innerText = `${sim_error.toFixed(1)}m`
    } else {
      simError.innerHTML = "&nbsp;"
    }

    // Update map
    map.setState({
      current: loc,
      plan: planEnabled.checked ? rendered : [],
      lz,
      actual
    })
  }
  updateApScreen()
}

function updateApScreen() {
  if (loc) {
    const dist = geo.distance(loc.lat, loc.lng, lz.destination.lat, lz.destination.lng)
    const alt = loc.alt - lz.destination.alt
    document.getElementById("ap-latlng")!.innerText = `${loc.lat.toFixed(6)}, ${loc.lng.toFixed(6)}`
    document.getElementById("ap-landingzone")!.innerText = `LZ: ${dist.toFixed(0)} m`
    document.getElementById("ap-alt")!.innerText = `Alt: ${alt.toFixed(0)} m AGL`
  } else {
    document.getElementById("ap-latlng")!.innerText = ""
    document.getElementById("ap-landingzone")!.innerText = "LZ:"
    document.getElementById("ap-alt")!.innerText = "Alt:"
  }
}
