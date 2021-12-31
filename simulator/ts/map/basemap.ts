import * as Cesium from "cesium"
import { LatLngAlt } from "../dtypes"
import { toDegrees } from "../geo/trig"

interface MapOptions {
  element: string
  center?: LatLngAlt
  draggableCursor?: string
  maxZoom?: number
  zoom?: number
}

export interface MapLayer {
  onAdd(map: Cesium.Viewer): void
  onRemove(map: Cesium.Viewer): void
}

Cesium.Ion.defaultAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI0OTY0YWEzMi1iYTEzLTRkNTktOWU4Ni1kYTAzZTlhYzg1MDgiLCJpZCI6MTA1MCwiaWF0IjoxNTI2ODU0NTMwfQ.GPRBDfdeQZv-8jHMR4uZoccEuMcxuSTN88nCxrdqW1Y"

const cesiumOptions = {
  animation: false, // dial in bottom left
  baseLayerPicker: true, // alternative map layers
  geocoder: false, // geo search box
  homeButton: false, // zooms out to earth
  navigationHelpButton: false, // ? button in top left
  navigationInstructionsInitiallyVisible: false,
  sceneModePicker: false, // 2d vs 3d
  selectionIndicator: false,
  infoBox: false,
  timeline: false // bottom timeline
}

export class BaseMap {
  public readonly map: Cesium.Viewer
  private layers: MapLayer[] = []

  constructor(options: MapOptions) {
    this.map = new Cesium.Viewer(options.element, cesiumOptions)
    this.map.terrainProvider = Cesium.createWorldTerrain()
    this.map.scene.globe.depthTestAgainstTerrain = true

    // Set sun position
    this.map.clock.currentTime = Cesium.JulianDate.fromDate(new Date(1597792800000))

    // Fix resolution scale to make lines nice
    this.map.useBrowserRecommendedResolution = false

    // Center
    if (options.center) {
      const target = Cesium.Cartesian3.fromDegrees(options.center.lng, options.center.lat, options.center.alt)
      this.map.camera.lookAt(target, new Cesium.HeadingPitchRange(0, -0.8, 2400))
      this.map.camera.lookAtTransform(Cesium.Matrix4.IDENTITY)
    }
  }

  public onClick(cb: (e: LatLngAlt) => void) {
    this.map.canvas.addEventListener("click", (e: any) => {
      if (!e.ctrlKey && !e.shiftKey) {
        const xy = new Cesium.Cartesian2(e.layerX, e.layerY)
        const carte = this.map.camera.pickEllipsoid(xy)
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

  public onMouseMove(cb: (e: LatLngAlt) => void) {
    this.map.canvas.addEventListener("mousemove", (e: any) => {
      if (!e.ctrlKey && !e.shiftKey) {
        const xy = new Cesium.Cartesian2(e.layerX, e.layerY)
        // const carte = this.map.camera.pickEllipsoid(xy)
        const carte = this.map.scene.pickPosition(xy)
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

export function lineMaterial(colorString: string): Cesium.PolylineOutlineMaterialProperty {
  return new Cesium.PolylineOutlineMaterialProperty({
    color: Cesium.Color.fromCssColorString(colorString),
    outlineColor: Cesium.Color.BLACK,
    outlineWidth: 2
  })
}
