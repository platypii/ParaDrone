import { GeoPointV, LatLngAlt } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { BaseMap } from "./basemap"
import { CanopyLayer } from "./canopy-layer"
import { LandingLayer } from "./landing-layer"
import { PathLayer } from "./path-layer"

interface MapState {
  current?: GeoPointV
  lz: LandingZone
  plan?: LatLngAlt[]
  actual?: LatLngAlt[]
}

export class DroneMap extends BaseMap {
  private destLayer = new LandingLayer()
  private canopyLayer = new CanopyLayer()
  private planLayer = new PathLayer("#2b2")
  private actualLayer = new PathLayer("#b22")

  constructor(lz: LandingZone) {
    super({
      element: "map",
      center: lz.destination,
      zoom: 16
    })
    this.addLayer(this.destLayer)
    this.addLayer(this.canopyLayer)
    this.addLayer(this.planLayer)
    this.addLayer(this.actualLayer)
  }

  public setState(state: MapState) {
    this.canopyLayer.setLocation(state.current)
    this.destLayer.setLz(state.lz)
    this.planLayer.setPath(state.plan)
    this.actualLayer.setPath(state.actual)
  }
}
