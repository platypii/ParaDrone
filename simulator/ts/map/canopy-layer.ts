import { GeoPointV } from "../dtypes"
import { toDegrees } from "../geo/trig"
import * as icons from "./icons"
import { MarkerLayer } from "./marker-layer"

export class CanopyLayer extends MarkerLayer {
  constructor() {
    super("Pilot", icons.canopy(90))
  }

  public setLocation(loc?: GeoPointV) {
    super.setLocation(loc)
    if (loc) {
      const bearing = toDegrees(Math.atan2(loc.vE, loc.vN))
      this.setIcon(icons.canopy(bearing))
    }
  }
}
