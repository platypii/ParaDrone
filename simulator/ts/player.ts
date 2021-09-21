import { Wind } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Paraglider } from "./paraglider"

const playButton = document.getElementById("map-play")!

/**
 * Control playback of the simulation
 */
export class Player {
  private readonly tickInterval = 1000 // milliseconds
  private readonly playbackSpeed = 5 // milliseconds

  private readonly para: Paraglider
  private readonly lz: LandingZone
  private readonly wind: Wind

  private player: number | undefined // interval id

  constructor(para: Paraglider, lz: LandingZone, wind: Wind) {
    this.para = para
    this.lz = lz
    this.wind = wind
    playButton.addEventListener("click", () => {
      if (para.loc) {
        if (this.player === undefined) {
          this.start()
        } else {
          this.stop()
        }
      }
    })
    document.getElementById("map-step")!.addEventListener("click", () => this.step())
  }

  public start(): void {
    if (this.player === undefined) {
      playButton.classList.remove("icon-start")
      playButton.classList.add("icon-pause")
      this.player = window.setInterval(() => {
        if (this.para.landed(this.lz)) {
          this.stop()
        } else {
          this.para.tick(this.tickInterval, this.wind)
        }
      }, this.tickInterval / this.playbackSpeed)
    }
  }

  public step(): void {
    if (this.player !== undefined) {
      this.stop()
    }
    if (!this.para.landed(this.lz)) {
      this.para.tick(this.tickInterval, this.wind)
    }
  }

  public stop(): void {
    if (this.player !== undefined) {
      playButton.classList.add("icon-start")
      playButton.classList.remove("icon-pause")
      clearInterval(this.player)
      this.player = undefined
    }
  }

}
