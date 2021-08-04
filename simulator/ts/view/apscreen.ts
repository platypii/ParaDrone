import * as geo from "../geo/geo"
import { LandingZone } from "../geo/landingzone"
import { Path3 } from "../geo/path3"
import { Paraglider } from "../paraglider"

export class ApScreen {
  private readonly latlng = document.getElementById("ap-latlng")!
  private readonly alt = document.getElementById("ap-alt")!
  private readonly speed = document.getElementById("ap-speed")!
  private readonly flightMode = document.getElementById("ap-flightmode")!
  private readonly lz = document.getElementById("ap-landingzone")!

  public update(para: Paraglider, lz: LandingZone, plan: Path3 | undefined) {
    if (para.loc) {
      const dist = geo.distancePoint(para.loc, lz.destination)
      const alt = para.loc.alt - lz.destination.alt
      const vel = Math.sqrt(para.loc.vN * para.loc.vN + para.loc.vE * para.loc.vE) * 2.23694 // mph
      const bear = geo.bearing(para.loc.lat, para.loc.lng, lz.destination.lat, lz.destination.lng) // degrees
      this.latlng.innerText = `${para.loc.lat.toFixed(6)}, ${para.loc.lng.toFixed(6)}`
      this.alt.innerText = `${alt.toFixed(0)} m AGL`
      this.speed.innerText = `${vel.toFixed(0)} mph`

      // LZ
      if (dist >= 10000) {
        this.lz.innerText = `LZ ${(dist * 1e-3).toFixed(0)} km`
      } else if (dist >= 1000) {
        this.lz.innerText = `LZ ${(dist * 1e-3).toFixed(1)} km`
      } else {
        this.lz.innerText = `LZ ${dist.toFixed(0)} m`
      }
      this.lz.innerText += " " + geo.bearing2(bear)

      // Flight mode
      this.flightMode.innerText = plan ? plan.path.name : ""
    } else {
      this.latlng.innerText = ""
      this.alt.innerText = ""
      this.speed.innerText = ""
      this.lz.innerText = ""
    }
  }
}
