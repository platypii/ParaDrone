import { GeoPointV } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Paramotor } from "./paramotor"
import { search } from "./plan/search"

const interval = 80

let player: number | undefined // interval id

export function start(loc: GeoPointV, lz: LandingZone, listener: (loc: GeoPointV) => void) {
  const paramotor = new Paramotor()
  paramotor.setLocation(loc)
  player = setInterval(() => {
    const ctrl = search(lz, paramotor).controls()
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
