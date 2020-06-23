import { LandingZone } from "../geo/landingzone"
import { MapLayer } from "./basemap"

export class LandingLayer implements MapLayer {
  private readonly arrowhead = {path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW}
  private arrow?: google.maps.Polyline

  public onAdd(map: google.maps.Map) {
    this.arrow = new google.maps.Polyline({
      map,
      path: [],
      strokeColor: "#eee",
      icons: [{
        icon: this.arrowhead,
        offset: "100%"
      }]
    })
  }

  public setLz(lz: LandingZone) {
    this.arrow!.setPath([lz.toLatLng(lz.startOfFinal()), lz.destination])
  }

  public onRemove() {
    if (this.arrow) {
      this.arrow.setMap(null)
      this.arrow = undefined
    }
  }
}
