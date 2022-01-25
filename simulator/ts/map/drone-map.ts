import { GeoPointV, LatLngAlt } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { BaseMap } from "./basemap"
import { CanopyLayer } from "./canopy-layer"
import { HoverLayer } from "./hover-layer"
import { LandingLayer } from "./landing-layer"
import { PathLayer } from "./path-layer"

interface MapState {
  current?: GeoPointV
  lz: LandingZone
  plan?: LatLngAlt[]
  actual?: LatLngAlt[]
}

export class DroneMap extends BaseMap {
  private canopyLayer = new CanopyLayer()
  private destLayer = new LandingLayer()
  private planLayer = new PathLayer("#22b")
  private actualLayer = new PathLayer("#4422bb99")
  private hoverLayer = new HoverLayer()

  constructor(lz: LandingZone) {
    super({
      element: "map",
      center: lz.destination,
      zoom: 15
    })
    this.addLayer(this.destLayer)
    this.addLayer(this.canopyLayer)
    this.addLayer(this.planLayer)
    this.addLayer(this.actualLayer)
    this.addLayer(this.hoverLayer)
    this.onMouseMove((e) => {
      this.hoverLayer.setLocation(e)
    })
  }

  public setState(state: MapState) {
    this.canopyLayer.setLocation(state.current)
    this.destLayer.setLz(state.lz)
    this.planLayer.setPath(state.plan)
    this.actualLayer.setPath(state.actual)
  }
}
