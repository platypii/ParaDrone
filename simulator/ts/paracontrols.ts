/**
 * Paraglider toggle positions
 * 0 = no deflection
 * 255 = full deflection
 */
export interface MotorPosition {
  left: number
  right: number
}

/**
 * Paraglider toggle positions
 * -255 = no deflection
 * 255 = full deflection
 */
export interface MotorSpeed {
  left: number
  right: number
}
