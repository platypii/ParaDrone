import { Wind } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Paraglider } from "./paraglider"

/**
 * Control playback of the simulation
 */
export class Player {
  private readonly interval = 50
  private readonly dt = 0.5 // seconds

  private readonly para: Paraglider
  private readonly lz: LandingZone
  private readonly wind: Wind

  private player: number | undefined // interval id

  constructor(para: Paraglider, lz: LandingZone, wind: Wind) {
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
        this.para.tick(this.dt, this.wind)
        if (this.para.landed(this.lz)) {
          this.stop()
        }
      }, this.interval)
    }
  }

  public stop(): void {
    if (this.player !== undefined) {
      clearInterval(this.player)
      this.player = undefined
    }
  }

}
