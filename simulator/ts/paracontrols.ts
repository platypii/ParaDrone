/**
 * Paramotor input controls (left and right toggle)
 * 0.0 = no deflection
 * 1.0 = full deflection
 */
export interface ParaControls {
  left: number
  right: number
}

export const defaultControls: ParaControls = {
  left: 0,
  right: 0
}

/**
 * Return control inputs to achieve a desired turn rate
 * Positive is right, negative is left
 * @param turnRate desired turn rate in degrees / second
 */
export function getControl(turnRate: number): ParaControls {
  const pull = Math.min(1, Math.abs(turnRate) * 0.05)
  if (turnRate < 5) {
    return {left: pull, right: 0}
  } else if (turnRate > 5) {
    return {left: 0, right: pull}
  } else {
    return {left: 0, right: 0}
  }
}
