import { LandingZone } from "./geo/landingzone"
import { Paraglider } from "./paraglider"
import { Windgram } from "./windgram"

/**
 * Control playback of the simulation
 */
export class Player {
  private readonly interval = 80

  private readonly para: Paraglider
  private readonly lz: LandingZone
  private readonly wind: Windgram

  private player: number | undefined // interval id

  constructor(para: Paraglider, lz: LandingZone, wind: Windgram) {
    this.para = para
    this.lz = lz
    this.wind = wind
    document.getElementById("map-play")!.addEventListener("click", () => {
      this.start()
    })
    document.getElementById("map-stop")!.addEventListener("click", () => {
      this.stop()
    })
  }

  public start(): void {
    if (this.player === undefined) {
      this.player = setInterval(() => {
        this.para.tick(this.wind, 1)
        if (this.para.landed(this.lz)) {
          stop()
        }
      }, this.interval)
    } else {
      console.error("Player double start")
    }
  }

  public stop(): void {
    if (this.player !== undefined) {
      clearInterval(this.player)
      this.player = undefined
    }
  }

}
