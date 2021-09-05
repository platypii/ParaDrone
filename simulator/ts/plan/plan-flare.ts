import { TogglePosition } from "../dtypes"
import { Path } from "../geo/path"

/**
 * A path exactly like the passed path, but with the toggles buried.
 */
export class FlarePath extends Path {
  constructor(path: Path) {
    super("Flare", ...path.segments)
    if (path.segments.length !== 1) {
      throw new Error(`Invalid straight line argument ${path}`)
    }
  }
  public controls(): TogglePosition {
    return {
      left: 255,
      right: 255
    }
  }
}
