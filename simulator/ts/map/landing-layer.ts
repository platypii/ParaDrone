import { LandingZone } from "../geo/landingzone"
import { toDegrees } from "../geo/trig"
import { MapLayer } from "./basemap"
import * as icons from "./icons"

export class LandingLayer implements MapLayer {
  private readonly arrowhead = {path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW}
  private arrow?: google.maps.Polyline

  public onAdd(googleMap: google.maps.Map) {
    this.arrow = new google.maps.Polyline({
      path: [],
      map: googleMap,
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
