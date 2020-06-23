
declare module Cesium {
  export function createWorldTerrain(): CesiumTerrainProvider
  export class Color {
    red: number
    green: number
    blue: number
    alpha: number
    static fromCssColorString(cssColor: string): Color
    static BLACK: Color
    static BLUE: Color
    static PURPLE: Color
    static RED: Color
    constructor(r: number, g: number, b: number, a: number)
  }
  export class Cartesian2 {
  }
  export class Cartesian3 {
    static ZERO: Cartesian3
    static fromDegrees(lng: number, lat: number, alt: number, ellipsoid?: Ellipsoid): Cartesian3
    static fromDegreesArray(coordinates: number[], ellipsoid?: Ellipsoid): Array<Cartesian3>
    static fromDegreesArrayHeights(coordinates: number[], ellipsoid?: Ellipsoid): Array<Cartesian3>
    constructor(x: number, y: number, z: number)
  }
  export class Cartographic {
    latitude: number
    longitude: number
    height: number
  }
  export class Ellipsoid {
    static WGS84: Ellipsoid
    cartesianToCartographic(cartesian: Cartesian3, result?: Cartographic): Cartographic
  }
  export class Viewer {
    constructor(id: string, options: CesiumOptions)
    camera: Camera
    canvas: HTMLCanvasElement
    entities: EntityCollection
    scene: Scene
    terrainProvider: CesiumTerrainProvider
    zoomTo(target: EntityCollection): void
  }
  export class Camera {
    pickEllipsoid(windowPosition: Cartesian2, ellipsoid?: Ellipsoid, result?: Cartesian3): Cartesian3
    setView(options: SetViewOptions): void
  }
  export interface SetViewOptions {
    destination: Cartesian3
    orientation?: any
  }
  export class EntityCollection {
    add(entity: any): Entity
    remove(entity: any): void
  }
  export class Entity {
    position: Cartesian3
    orientation: any
    polyline: any
  }
  export class Scene {
    globe: Globe
    primitives: any
  }
  export class BillboardCollection {
    add(primitive: any, index?: number): any
  }
  export class Billboard {
    height: number
    image: string
    position: Cartesian3
    scale: number
    rotation: number
    show: boolean
  }
  export class PolylineCollection {
    add(primitive: any, index?: number): any
    remove(primitive: Polyline): boolean
  }
  export class Polyline {
    material: Material
    positions: Cartesian3[]
  }
  export class Globe {
    depthTestAgainstTerrain: boolean
    ellipsoid: Ellipsoid
    enableLighting: boolean
  }
  export interface CesiumOptions {
    animation?: boolean // dial in bottom left
    baseLayerPicker?: boolean // alternative map layers
    geocoder?: boolean // geo search box
    homeButton?: boolean // zooms out to earth
    navigationHelpButton?: boolean // ? button in top left
    navigationInstructionsInitiallyVisible?: boolean
    sceneModePicker?: boolean // 2d vs 3d
    timeline?: boolean // bottom timeline
  }
  export class CesiumTerrainProvider {}
  export class Material {
    constructor(options: any)
    uniforms: {
      color: Color
    }
  }
  export class PolylineOutlineMaterialProperty {
    constructor(options: any)
  }
  export class Transforms {
    static headingPitchRollQuaternion(origin: Cartesian3, hpr: any): Quaternion
  }
  export class HeadingPitchRoll {
    constructor(heading: number, pitch: number, roll: number)
  }
  export class Quaternion {
  }
}
