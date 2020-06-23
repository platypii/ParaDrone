import { GeoPointV, LatLngAlt } from "./dtypes"
import { getPlan } from "./flightcomputer"
import * as geo from "./geo/geo"
import { kpow } from "./geo/landingzone"
import { DroneMap } from "./map/drone-map"
import { Paramotor } from "./paramotor"
import * as player from "./player"
import { sim } from "./sim"
import * as test from "./test"
import { distance } from "./util"

const lz = kpow

let start: GeoPointV
let loc: GeoPointV
let map: DroneMap

export function init() {
  // Setup map
  map = new DroneMap()
  map.setState({lz})
  map.onClick((e) => {
    player.stop()
    setStart({
      lat: e.lat,
      lng: e.lng,
      alt: 800 // TODO: Get elevation
    })
  })
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
    vE: Paramotor.groundSpeed,
    climb: Paramotor.climbRate
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
    // Compute full plan
    const alt_agl = loc.alt - lz.destination.alt
    const plan = getPlan(loc, lz)
    const rendered = plan.render3(alt_agl)
      .map((p) => lz.toLatLngAlt(p))
    const simsteps = sim(start, lz)
    const actual = simsteps.map((s) => s.loc)
    // const actual: any[] = []

    // Update map
    map.setState({
      current: loc,
      plan: rendered,
      lz,
      actual
    })

    const error = distance(plan.end, lz.dest)
    document.getElementById("pstatus-name")!.innerText = "Plan: " + plan.name
    document.getElementById("pstatus-error")!.innerText = `Error: ${error.toFixed(1)} m`
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
