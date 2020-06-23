import { LatLng } from "../dtypes"
import { MapLayer } from "./basemap"

export class PathLayer implements MapLayer {
  private readonly color: string
  private polyline?: google.maps.Polyline
  private points: LatLng[] = []

  constructor(color: string) {
    this.color = color
  }

  public setPath(points?: LatLng[]) {
    this.points = points || []
    this.update()
  }

  public onAdd(map: google.maps.Map) {
    this.polyline = new google.maps.Polyline({
      map,
      path: this.points,
      strokeColor: this.color,
      strokeOpacity: 0.8,
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
