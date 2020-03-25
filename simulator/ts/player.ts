import { GeoPointV } from "./dtypes"
import { getPlan } from "./flightcomputer"
import { LandingZone } from "./geo/landingzone"
import { Paramotor } from "./paramotor"

const interval = 80

let player: number | undefined // interval id

export function start(loc: GeoPointV, lz: LandingZone, listener: (loc: GeoPointV) => void) {
  const paramotor = new Paramotor()
  paramotor.setLocation(loc)
  player = setInterval(() => {
    const ctrl = getPlan(paramotor.loc!, lz).controls()
    paramotor.setControls(ctrl)
    paramotor.tick(1)
    if (paramotor.landed(lz)) {
      stop()
    }
    listener(paramotor.loc!)
  }, interval)
}

export function stop() {
  if (player !== undefined) {
    clearInterval(player)
    player = undefined
  }
}
