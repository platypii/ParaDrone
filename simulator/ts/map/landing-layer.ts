import { LandingZone } from "../geo/landingzone"
import { latLngToCart } from "../geo/latlng"
import { Paramotor } from "../paramotor"
import { MapLayer } from "./basemap"

export class LandingLayer implements MapLayer {
  private entity?: Cesium.Entity
  private lz?: LandingZone
  private readonly para = new Paramotor()

  public onAdd(map: Cesium.Viewer) {
    this.entity = map.entities.add({
      name: "Final",
      polyline: {
        positions: this.positions(),
        material: Cesium.Color.BLUE,
        width: 4
      }
    })

    this.update()
  }

  public setLz(lz: LandingZone) {
    if (this.lz !== lz) {
      this.lz = lz
      this.update()
    }
  }

  public onRemove(map: Cesium.Viewer) {
    if (this.entity) {
      map.entities.remove(this.entity)
      this.entity = undefined
    }
  }

  private update() {
    if (this.entity) {
      this.entity.polyline.positions = this.positions()
    }
  }

  private positions(): Cesium.Cartesian3[] {
    if (this.lz) {
      const final = this.lz.toLatLngAlt(this.lz.startOfFinal(this.para))
      const lz = this.lz.destination
      return [final, lz].map((p) => latLngToCart(p))
    } else {
      return []
    }
  }
}
