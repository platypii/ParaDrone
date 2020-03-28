import { LatLng } from "../dtypes"
import { MapLayer } from "./basemap"

export class PointsLayer implements MapLayer {
  private points: LatLng[] = []
  private polyline?: google.maps.Polyline

  public setPoints(points: LatLng[]) {
    this.points = points
    this.update()
  }

  public onAdd(googleMap: google.maps.Map) {
    this.polyline = new google.maps.Polyline({
      map: googleMap,
      strokeColor: "#eee",
      strokeWeight: 4
    })
    this.update()
  }

  public onRemove() {
    if (this.polyline) {
      this.polyline.setMap(null)
      this.polyline = undefined
    }
  }

  private update() {
    if (this.polyline && this.points) {
      this.polyline.setPath(this.points)
    }
  }
}
