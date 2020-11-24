import { LandingZone } from "./geo/landingzone"
import { Paraglider } from "./paraglider"
import { Windgram } from "./windgram"

const interval = 80

let player: number | undefined // interval id

export function start(para: Paraglider, lz: LandingZone, wind: Windgram) {
  if (player === undefined) {
    player = setInterval(() => {
      para.tick(wind, 1)
      if (para.landed(lz)) {
        stop()
      }
    }, interval)
  } else {
    console.error("Player double start")
  }
}

export function stop() {
  if (player !== undefined) {
    clearInterval(player)
    player = undefined
  }
}
