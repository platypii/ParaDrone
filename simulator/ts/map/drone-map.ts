import { GeoPointV, LatLng } from "../dtypes"
import { BaseMap } from "./basemap"
import { CanopyLayer } from "./canopy-layer"
import { pin } from "./icons"
import { MarkerLayer } from "./marker-layer"
import { PathLayer } from "./path-layer"

interface MapState {
  start: LatLng
  current: GeoPointV
  dest: LatLng
  plan: LatLng[]
  actual: LatLng[]
}

export class DroneMap extends BaseMap {
  private startLayer = new MarkerLayer("Start", pin("#6b00ff"))
  private canopyLayer = new CanopyLayer()
  private destLayer = new MarkerLayer("Start", pin("#952"))
  private planLayer = new PathLayer("#1e1")
  private actualLayer = new PathLayer("#e11")

  constructor() {
    super({
      element: document.getElementById("map")!,
      center: new google.maps.LatLng(47.239, -123.143),
      zoom: 16
    })
    this.addLayer(this.startLayer)
    this.addLayer(this.canopyLayer)
    this.addLayer(this.destLayer)
    this.addLayer(this.planLayer)
    this.addLayer(this.actualLayer)
  }

  public setState(state: MapState) {
    this.startLayer.setLocation(state.start)
    this.canopyLayer.setLocation(state.current)
    this.destLayer.setLocation(state.dest)
    this.planLayer.setPath(state.plan)
    this.actualLayer.setPath(state.actual)
  }
}
