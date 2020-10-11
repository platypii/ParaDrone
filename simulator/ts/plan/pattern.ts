import { Point3V, Turn } from "../dtypes"
import { LandingZone } from "../geo/landingzone"
import { Paraglider } from "../paraglider"

export class LandingPattern {
  // Ground length of final approach
  public readonly finalDistance: number = 150 // meters

  constructor(public para: Paraglider, public lz: LandingZone) {
  }

  /**
   * Landing pattern: start of final approach
   */
  public startOfFinal(): Point3V {
    return {
      x: -this.finalDistance * this.lz.dest.vx,
      y: -this.finalDistance * this.lz.dest.vy,
      alt: this.lz.dest.alt + this.finalDistance / this.para.glide,
      vx: this.lz.dest.vx,
      vy: this.lz.dest.vy,
      climb: this.para.climbRate
    }
  }

  /**
   * Landing pattern: start of base leg
   */
  public startOfBase(turn: Turn): Point3V {
    return {
      x: this.finalDistance * (-this.lz.dest.vx + turn * this.lz.dest.vy),
      y: this.finalDistance * (-turn * this.lz.dest.vx - this.lz.dest.vy),
      alt: this.lz.dest.alt + 2 * this.finalDistance / this.para.glide, // TODO: Doesn't account for turns
      vx: -this.lz.dest.vx,
      vy: -this.lz.dest.vy,
      climb: this.para.climbRate
    }
  }

  /**
   * Landing pattern: start of downwind leg
   */
  public startOfDownwind(turn: Turn): Point3V {
    return {
      x: this.finalDistance * turn * this.lz.dest.vy,
      y: -this.finalDistance * turn * this.lz.dest.vx,
      alt: this.lz.dest.alt + 3 * this.finalDistance / this.para.glide, // TODO: Doesn't account for turns
      vx: -this.lz.dest.vx,
      vy: -this.lz.dest.vy,
      climb: this.para.climbRate
    }
  }
}
