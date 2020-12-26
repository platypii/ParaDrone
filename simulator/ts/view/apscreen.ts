import * as geo from "../geo/geo"
import { LandingZone } from "../geo/landingzone"
import { Paraglider } from "../paraglider"

export class ApScreen {
  private readonly latlng = document.getElementById("ap-latlng")!
  private readonly lz = document.getElementById("ap-landingzone")!
  private readonly alt = document.getElementById("ap-alt")!
  private readonly speed = document.getElementById("ap-speed")!

  public update(para: Paraglider, lz: LandingZone) {
    if (para.loc) {
      const dist = geo.distancePoint(para.loc, lz.destination)
      const alt = para.loc.alt - lz.destination.alt
      const vel = Math.sqrt(para.loc.vN * para.loc.vN + para.loc.vE * para.loc.vE) * 2.23694 // mph
      this.latlng.innerText = `${para.loc.lat.toFixed(6)}, ${para.loc.lng.toFixed(6)}`
      this.lz.innerText = `LZ: ${dist.toFixed(0)} m`
      this.alt.innerText = `${alt.toFixed(0)} m AGL`
      this.speed.innerText = `${vel.toFixed(0)} mph`
    } else {
      this.latlng.innerText = ""
      this.lz.innerText = ""
      this.alt.innerText = ""
      this.speed.innerText = ""
    }
  }
}
