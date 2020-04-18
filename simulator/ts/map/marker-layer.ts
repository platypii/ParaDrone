import { LatLng } from "../dtypes"
import { MapLayer } from "./basemap"

export class MarkerLayer implements MapLayer {
  private readonly title: string
  private icon: google.maps.Symbol
  private marker?: google.maps.Marker
  private loc?: LatLng

  constructor(title: string, icon: google.maps.Symbol) {
    this.title = title
    this.icon = icon
  }

  public setIcon(icon: google.maps.Symbol) {
    this.icon = icon
    this.update()
  }

  public setLocation(loc?: LatLng) {
    this.loc = loc
    this.update()
  }

  public onAdd(googleMap: google.maps.Map) {
    this.marker = new google.maps.Marker({
      map: googleMap,
      title: this.title,
      icon: this.icon
    })
    this.update()
  }

  public onRemove() {
    if (this.marker) {
      this.marker.setMap(null)
      this.marker = undefined
    }
  }

  private update() {
    if (this.marker) {
      if (this.loc) {
        this.marker.setPosition(this.loc)
        this.marker.setIcon(this.icon)
        this.marker.setVisible(true)
      } else {
        this.marker.setVisible(false)
      }
    }
  }
}
