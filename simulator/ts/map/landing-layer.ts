import { LandingZone } from "../geo/landingzone"
import { latLngToCart } from "../geo/latlng"
import { Paraglider } from "../paraglider"
import { LandingPattern } from "../plan/pattern"
import { MapLayer } from "./basemap"

export class LandingLayer implements MapLayer {
  private entity?: Cesium.Entity
  private lz?: LandingZone
  private pattern?: LandingPattern
  private readonly para = new Paraglider()

  public onAdd(map: Cesium.Viewer) {
    this.entity = map.entities.add({
      name: "Final",
      polyline: {
        positions: this.positions(),
        material: Cesium.Color.fromCssColorString("#22bd"),
        width: 6
      }
    })

    this.update()
  }

  public setLz(lz: LandingZone) {
    if (this.lz !== lz) {
      this.lz = lz
      this.pattern = new LandingPattern(this.para, lz)
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
    if (this.lz && this.pattern) {
      const final = this.lz.toLatLngAlt(this.pattern.startOfFinal())
      const lz = this.lz.destination
      return [final, lz].map((p) => latLngToCart(p))
    } else {
      return []
    }
  }
}
