import { GeoPointV, LatLngAlt } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { BaseMap } from "./basemap"
import { CanopyLayer } from "./canopy-layer"
import { pin } from "./icons"
import { LandingLayer } from "./landing-layer"
import { MarkerLayer } from "./marker-layer"
import { PathLayer } from "./path-layer"

interface MapState {
  start?: LatLngAlt
  current?: GeoPointV
  lz: LandingZone
  plan?: LatLngAlt[]
  actual?: LatLngAlt[]
}

export class DroneMap extends BaseMap {
  private startLayer = new MarkerLayer("Start", pin("#6b00ff"))
  private destLayer = new LandingLayer()
  private canopyLayer = new CanopyLayer()
  private planLayer = new PathLayer("#1e1")
  private actualLayer = new PathLayer("#e11")

  constructor() {
    super({
      element: document.getElementById("map")!,
      center: new google.maps.LatLng(47.239, -123.143),
      zoom: 16
    })
    this.addLayer(this.startLayer)
    this.addLayer(this.destLayer)
    this.addLayer(this.canopyLayer)
    this.addLayer(this.planLayer)
    this.addLayer(this.actualLayer)
  }

  public setState(state: MapState) {
    this.startLayer.setLocation(state.start)
    this.canopyLayer.setLocation(state.current)
    this.destLayer.setLz(state.lz)
    this.planLayer.setPath(state.plan)
    this.actualLayer.setPath(state.actual)
  }
}
