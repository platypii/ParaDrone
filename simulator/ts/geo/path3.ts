import { Point, Point3V } from "../dtypes"
import { Paraglider } from "../paraglider"
import { Path } from "./path"

export class Path3 {
  readonly path: Path
  readonly startAlt: number

  constructor(path: Path, startAlt: number) {
    this.path = path
    this.startAlt = startAlt
  }

  public render3(para: Paraglider): Point3V[] {
    // Render and compute altitudes
    let alt = this.startAlt
    let lastPoint: Point
    return this.path.render().map((p) => {
      if (lastPoint) {
        const dx = p.x - lastPoint.x
        const dy = p.y - lastPoint.y
        const lastDistance = Math.sqrt(dx * dx + dy * dy)
        alt -= lastDistance / para.glide
      }
      lastPoint = p
      return {
        ...p,
        alt,
        vx: 0,
        vy: 0,
        climb: para.climbRate
      }
    })
  }

}
