import { LatLngAlt } from "../dtypes"
import { MapLayer } from "./basemap"

export class MarkerLayer implements MapLayer {
  private readonly title: string
  private icon: string
  private rotation: number = 0

  private billboards?: Cesium.BillboardCollection
  private billboard?: Cesium.Billboard

  private loc?: LatLngAlt

  constructor(title: string, icon: string) {
    this.title = title
    this.icon = icon
  }

  public setIcon(icon: string, rotation: number) {
    this.icon = icon
    this.rotation = rotation
    this.update()
  }

  public setLocation(loc?: LatLngAlt) {
    this.loc = loc
    this.update()
  }

  public onAdd(map: Cesium.Viewer) {
    this.billboards = new Cesium.BillboardCollection()
    map.scene.primitives.add(this.billboards)
    this.billboard = this.billboards.add({
      position: Cesium.Cartesian3.ZERO,
      image: this.icon
      // text: this.title
    })
    this.update()
  }

  public onRemove(map: Cesium.Viewer) {
    if (this.billboards) {
      map.scene.primitives.remove(this.billboards)
      this.billboards = undefined
      this.billboard = undefined
    }
  }

  private update() {
    if (this.billboard) {
      if (this.loc) {
        this.billboard.position = Cesium.Cartesian3.fromDegrees(this.loc.lng, this.loc.lat, this.loc.alt)
        this.billboard.rotation = this.rotation
        this.billboard.image = this.icon
        this.billboard.show = true
      } else {
        this.billboard.show = false
      }
    }
  }
}
