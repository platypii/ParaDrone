import { LatLng } from "../dtypes"
import { toDegrees } from "../geo/trig"

interface MapOptions {
  element: string
  center?: LatLng
  draggableCursor?: string
  maxZoom?: number
  zoom?: number
}

export interface MapLayer {
  onAdd(map: Cesium.Viewer): void
  onRemove(map: Cesium.Viewer): void
}

const cesiumOptions: Cesium.CesiumOptions = {
  animation: false, // dial in bottom left
  baseLayerPicker: true, // alternative map layers
  geocoder: false, // geo search box
  homeButton: false, // zooms out to earth
  navigationHelpButton: false, // ? button in top left
  navigationInstructionsInitiallyVisible: false,
  sceneModePicker: false, // 2d vs 3d
  timeline: false // bottom timeline
}

export class BaseMap {
  public readonly map: Cesium.Viewer
  private layers: MapLayer[] = []

  private readonly defaultCenter = {lat: 39, lng: -100} // USA
  private readonly defaultZoom = 4 // USA

  constructor(options: MapOptions) {
    this.map = new Cesium.Viewer(options.element, cesiumOptions)
    if (options.center) {
      this.map.camera.setView({
        destination: Cesium.Cartesian3.fromDegrees(options.center.lng, options.center.lat, 4000)
      })
    }
  }

  public onClick(cb: (e: LatLng) => void) {
    this.map.canvas.addEventListener("click", (e) => {
      if (!e.ctrlKey && !e.shiftKey) {
        const carte = this.map.camera.pickEllipsoid(e)
        if (carte) {
          const ellipsoid = this.map.scene.globe.ellipsoid
          const carto = ellipsoid.cartesianToCartographic(carte)
          const ll = {
            lat: toDegrees(carto.latitude),
            lng: toDegrees(carto.longitude),
            alt: carto.height
          }
          cb(ll)
        }
      }
    })
  }

  public addLayer(layer?: MapLayer): void {
    if (layer && this.layers.indexOf(layer) < 0) {
      this.layers.push(layer)
      layer.onAdd(this.map)
    }
  }

  public removeLayer(layer?: MapLayer): void {
    if (layer) {
      this.layers = this.layers.filter((l) => l !== layer)
      layer.onRemove(this.map)
    }
  }
}
