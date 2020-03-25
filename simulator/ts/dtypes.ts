import { LandingZone } from "./geo/landingzone"
import { ParaControls } from "./paracontrols"

export interface LatLng {
  lat: number
  lng: number
}

export interface LatLngAlt {
  lat: number
  lng: number
  alt: number
}

export interface GeoPointV {
  millis: number
  lat: number
  lng: number
  alt: number
  climb: number
  vN: number
  vE: number
}

export interface GeoPoint {
  millis: number
  lat: number
  lng: number
  alt: number
}

export interface Point {
  x: number
  y: number
}

export interface PointV {
  x: number
  y: number
  vx: number
  vy: number
}

export interface Point3V {
  x: number
  y: number
  alt: number
  vx: number
  vy: number
  climb: number
}

export enum Turn {
  Left = -1,
  Right = 1
}

export interface Circle {
  x: number
  y: number
  radius: number
}