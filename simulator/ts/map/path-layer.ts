import * as Cesium from "cesium"
import { LatLngAlt } from "../dtypes"
import { latLngToCart } from "../geo/latlng"
import { lineMaterial, MapLayer } from "./basemap"

export class PathLayer implements MapLayer {
  private readonly color: string
  private polylines?: Cesium.PolylineCollection
  private polyline?: Cesium.Polyline
  private points: LatLngAlt[] = []

  constructor(color: string) {
    this.color = color
  }

  public setPath(points?: LatLngAlt[]) {
    if (this.points !== points) {
      this.points = points || []
      this.update()
    }
  }

  public onAdd(map: Cesium.Viewer) {
    this.polylines = new Cesium.PolylineCollection()
    map.scene.primitives.add(this.polylines)
    this.polyline = this.polylines.add({
      positions: this.points.map((d: LatLngAlt) => Cesium.Cartesian3.fromDegrees(d.lng, d.lat, d.alt)),
      // material: lineMaterial(this.color),
      width: 4
      // strokeOpacity: 0.8 // TODO
    })
    this.polyline!.material.uniforms.color = Cesium.Color.fromCssColorString(this.color)
    this.update()
  }

  public onRemove(map: Cesium.Viewer) {
    if (this.polylines) {
      map.scene.primitives.remove(this.polylines)
      this.polylines = undefined
      this.polyline = undefined
    }
  }

  private update() {
    if (this.polyline && this.points) {
      this.polyline.positions = this.positions()
    }
  }

  private positions(): Cesium.Cartesian3[] {
    return this.points.map((p) => latLngToCart(p))
  }
}
