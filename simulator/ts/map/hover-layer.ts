import * as Cesium from "cesium"
import { LatLngAlt } from "../dtypes"
import { MapLayer } from "./basemap"

export class HoverLayer implements MapLayer {
  private map?: Cesium.Viewer
  private entity?: Cesium.Entity
  private loc?: LatLngAlt

  public setLocation(loc?: LatLngAlt) {
    this.loc = loc
    this.update()
  }

  public onAdd(map: Cesium.Viewer) {
    this.map = map
    this.entity = map.entities.add(new Cesium.Entity({
      cylinder: {
        length: 400,
        topRadius: 15,
        bottomRadius: 15,
        heightReference: new Cesium.ConstantProperty(Cesium.HeightReference.CLAMP_TO_GROUND),
        material: Cesium.Color.fromCssColorString("#8888cc33")
      }
    }))
    this.update()
  }

  public onRemove(map: Cesium.Viewer) {
    if (this.entity) {
      map.entities.remove(this.entity)
    }
    this.map = undefined
  }

  private update() {
    if (this.map && this.entity) {
      if (this.loc) {
        const cart3 = Cesium.Cartesian3.fromDegrees(this.loc.lng, this.loc.lat, this.loc.alt)
        this.entity.position = new Cesium.ConstantPositionProperty(cart3)
        this.entity.show = true
      } else {
        this.entity.show = false
      }
    }
  }
}
