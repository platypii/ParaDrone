import { LatLng } from "../dtypes"
import { moveBearing } from "./geo"

const sqrt3 = Math.sqrt(3)

/**
 * Generate a hexagonal grid
 * https://www.redblobgames.com/grids/hexagons/
 */
export function latLngGrid(center: LatLng, n: number, size: number): LatLng[] {
  return hexGrid(n)
    .map((c) => c.toLatLng(center, size))
}

function hexGrid(n: number): HexCell[] {
  const grid: HexCell[] = []
  // Iterate over axial hex coordinates
  for (let i = -n; i <= n; i++) {
    for (let j = -n; j <= n; j++) {
      // i + j + k = 0
      const k = -i - j
      // Constrain to hexagon (instead of parallelogram)
      if (-n <= k && k <= n) {
        grid.push(new HexCell(i, j))
      }
    }
  }
  return grid
}

class HexCell {
  constructor(public readonly i: number, public readonly j: number) {}

  public toLatLng(center: LatLng, size: number): LatLng {
    const x = size * (this.i * sqrt3 / 2 + this.j * sqrt3)
    const y = size * (this.i * 3 / 2)
    const bear = Math.atan2(y, x)
    const dist = Math.sqrt(x * x + y * y)
    return moveBearing(center.lat, center.lng, bear, dist)
  }
}
