import * as Cesium from "cesium"
import { GeoPointV } from "../dtypes"
import { MapLayer } from "./basemap"

export class CanopyLayer implements MapLayer {
  private loc?: GeoPointV
  private rotation: number = 0

  private map?: Cesium.Viewer
  private entity?: Cesium.Entity

  public setLocation(loc?: GeoPointV) {
    this.loc = loc
    this.update()
  }

  public onAdd(map: Cesium.Viewer) {
    this.map = map
    this.update()
  }

  public onRemove(map: Cesium.Viewer) {
    if (this.entity) {
      map.entities.remove(this.entity)
    }
    this.map = undefined
  }

  private update() {
    if (this.map) {
      if (this.entity && this.loc) {
        this.entity.position = new Cesium.ConstantPositionProperty(this.position())
        this.entity.orientation = new Cesium.ConstantProperty(this.orientation())
      } else if (this.loc) {
        this.entity = this.map.entities.add(new Cesium.Entity({
          name: "Canopy",
          position: this.position(),
          orientation: new Cesium.ConstantProperty(this.orientation()),
          model: {
            uri: "img/paraglider.glb",
            scale: 0.02
          }
        }))
      } else if (this.entity) {
        // Remove it
        this.map.entities.remove(this.entity)
      }
    }
  }

  private position(): Cesium.Cartesian3 {
    if (this.loc) {
      return Cesium.Cartesian3.fromDegrees(this.loc.lng, this.loc.lat, this.loc.alt - 25)
    } else {
      return Cesium.Cartesian3.ZERO
    }
  }

  private orientation(): Cesium.Cartesian3 | Cesium.Quaternion {
    if (this.loc) {
      const heading = Math.atan2(this.loc.vE, this.loc.vN) - Math.PI / 2
      const pitch = 0
      const roll = 0
      return Cesium.Transforms.headingPitchRollQuaternion(this.position(), new Cesium.HeadingPitchRoll(heading, pitch, roll))
    } else {
      return Cesium.Cartesian3.ZERO
    }
  }
}
