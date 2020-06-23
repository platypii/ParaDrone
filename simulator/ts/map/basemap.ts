
interface MapOptions {
  element: HTMLElement
  center?: google.maps.LatLng | google.maps.LatLngLiteral
  draggableCursor?: string
  maxZoom?: number
  zoom?: number
}

export interface MapLayer {
  onAdd(map: google.maps.Map): void
  onRemove(): void
}

export class BaseMap {
  public readonly map: google.maps.Map
  private layers: MapLayer[] = []

  private readonly defaultCenter = new google.maps.LatLng(39, -100) // USA
  private readonly defaultZoom = 4 // USA

  constructor(options: MapOptions) {
    this.map = new google.maps.Map(options.element, {
      center: options.center || this.defaultCenter,
      draggableCursor: options.draggableCursor,
      mapTypeId: google.maps.MapTypeId.HYBRID,
      maxZoom: options.maxZoom,
      scaleControl: true,
      zoom: options.zoom || this.defaultZoom
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
      layer.onRemove()
    }
  }
}
