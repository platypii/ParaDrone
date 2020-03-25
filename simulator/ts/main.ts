import { GeoPointV, LatLngAlt } from "./dtypes"
import { getPlan } from "./flightcomputer"
import * as geo from "./geo/geo"
import { kpow } from "./geo/landingzone"
import { DroneMap } from "./map/drone-map"
import { Paramotor } from "./paramotor"
import * as player from "./player"
import { sim } from "./sim"
import { distance } from "./util"

const lz = kpow

let start: GeoPointV
let loc: GeoPointV
let map: DroneMap

export function init() {
  map = new DroneMap()
  map.googleMap.addListener("click", (e) => {
    player.stop()
    setStart({
      lat: e.latLng.lat(),
      lng: e.latLng.lng(),
      alt: 800 // TODO: Get elevation
    })
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
    vE: Paramotor.groundSpeed,
    climb: 0
  }

  // Update UI
  loc = start
  update()

  // Start player
  // player.start(start, lz, (next) => {
  //   loc = next
  //   update()
  // })
}

/**
 * Update views
 */
function update() {
  if (start) {
    const xy = lz.toPoint(start)
    const dist = Math.hypot(xy.x, xy.y)
    document.getElementById("pstatus-start")!.innerText = `Start: ${xy.x.toFixed(1)}, ${xy.y.toFixed(1)} (${dist.toFixed(0)} m)` // Coordinate space
  }

  if (loc) {
    // Compute full plan
    const plan = getPlan(loc, lz)
    const rendered = plan.render()
      .map((p) => lz.toLatLng(p))
    // const actual = sim(start, lz)
    const actual: any[] = []

    // Update map
    map.setState({
      start,
      current: loc,
      dest: lz.destination,
      plan: rendered,
      actual
    })

    const curr = lz.toPoint(loc)
    const dist = geo.distance(loc.lat, loc.lng, lz.destination.lat, lz.destination.lng)
    document.getElementById("pstatus-curr")!.innerText = `Curr: ${curr.x.toFixed(1)}, ${curr.y.toFixed(1)} (${dist.toFixed(0)} m)` // Coordinate space
    document.getElementById("pstatus-alt")!.innerText = `Alt: ${loc.alt.toFixed(0)} m`

    const error = distance(plan.end, lz.dest)
    document.getElementById("pstatus-error")!.innerText = `Error: ${error.toFixed(1)} m`
  }
}
