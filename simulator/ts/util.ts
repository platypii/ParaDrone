import { Point } from "./dtypes"

export function distance(point1: Point, point2: Point): number {
  return Math.hypot(point1.x - point2.x, point1.y - point2.y)
}

/**
 * Linear interpolation
 */
export function interpolate(start: number, end: number, alpha: number): number {
  return start + alpha * (end - start)
}
