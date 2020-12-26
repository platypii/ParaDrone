import { Point } from "./dtypes"

/**
 * Distance between two points
 */
export function distance(point1: Point, point2: Point): number {
  const dx = point1.x - point2.x
  const dy = point1.y - point2.y
  return Math.sqrt(dx * dx + dy * dy)
}

/**
 * Linear interpolation
 * @param alpha interpolation parameter from 0 (start) to 1 (end)
 */
export function interpolate(start: number, end: number, alpha: number): number {
  return start + alpha * (end - start)
}
