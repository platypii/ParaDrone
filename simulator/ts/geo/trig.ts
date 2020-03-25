/**
 * Convert radians to degrees
 */
export function toDegrees(degrees: number): number {
  return degrees * 180 / Math.PI
}

/**
 * Convert degrees to radians
 */
export function toRadians(degrees: number): number {
  return degrees * Math.PI / 180
}

/**
 * Normalize an angle in degrees into the normal [-180,180) range of longitude
 */
export function mod360(degrees: number): number {
  return (((degrees % 360) + 540) % 360) - 180
}
