import * as Cesium from "cesium"
import { GeoPointV } from "../dtypes"
import { MapLayer } from "./basemap"

export class CameraLayer implements MapLayer {
  private map?: Cesium.Viewer
  private loc?: GeoPointV
  private viewInside: boolean = false
  private viewButton: HTMLElement = document.getElementById("map-pov")!
  private cameraOrientation: Cesium.HeadingPitchRange = new Cesium.HeadingPitchRange(0, -0.7, 20)
  private lastBearing: number | undefined

  constructor() {
    // Bind camera view buttons
    this.viewButton.addEventListener("click", () => {
      this.viewInside = !this.viewInside
      this.update()
    })
  }

  public setLocation(loc?: GeoPointV) {
    this.loc = loc
    this.update()
  }

  public onAdd(map: Cesium.Viewer) {
    this.map = map
    this.update()
  }

  public onRemove(map: Cesium.Viewer) {
    this.map = undefined
  }

  private update() {
    if (this.map) {
      console.log("WTF", this.map.camera)

      // Update camera position
      if (this.viewInside && this.loc) {
        this.viewButton.classList.add("active")
        const position = Cesium.Cartesian3.fromDegrees(this.loc.lng, this.loc.lat, this.loc.alt)
        const bearing = Math.atan2(this.loc.vE, this.loc.vN)
        if (this.lastBearing === undefined) {
          this.cameraOrientation.heading = bearing
        } else {
          const headingChange = bearing - this.lastBearing
          this.cameraOrientation.heading = this.map.camera.heading + headingChange
          this.cameraOrientation.range = Cesium.Cartesian3.magnitude(this.map.camera.position)
        }
        this.map.camera.lookAt(position, this.cameraOrientation)
        this.lastBearing = bearing
      } else {
        this.viewButton.classList.remove("active")
        // TODO: Remove look-at
      }
    }
  }
}
