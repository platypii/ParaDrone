import * as Cesium from "cesium"
import { LatLng } from "../dtypes"
import { moveBearing } from "../geo/geo"
import { MapLayer } from "./basemap"

/**
 * Heat map like layer
 */
export class HeatLayer implements MapLayer {
  private readonly location: LatLng
  private readonly color: string
  private readonly size: number
  private entity?: Cesium.Entity

  constructor(location: LatLng, size: number, color: string) {
    this.color = color
    this.location = location
    this.size = size
  }

  public onAdd(map: Cesium.Viewer) {
    this.entity = map.entities.add(new Cesium.Entity({
      name: "HeatPoly",
      polygon: {
        hierarchy: {
          positions: this.positions(),
          holes: []
        },
        material: Cesium.Color.fromCssColorString(this.color)
      }
    }))
  }

  public onRemove(map: Cesium.Viewer) {
    if (this.entity) {
      map.entities.remove(this.entity)
      this.entity = undefined
    }
  }

  /**
   * Generate a polygon centered at the location
   */
  private positions() {
    const sides = 6 // hexagons are the bestagons
    const vertices = []
    for (let i = 0; i < sides; i++) {
      const vertex = moveBearing(this.location.lat, this.location.lng, (i + 0.5) * 2 * Math.PI / sides, this.size)
      vertices.push(vertex.lng, vertex.lat)
    }
    return Cesium.Cartesian3.fromDegreesArray(vertices)
  }
}
