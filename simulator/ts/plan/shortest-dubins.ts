import { PointV, Turn } from "../dtypes"
import { Path } from "../geo/paths"
import { dubins } from "./planner-dubins"

/**
 * Find the shortest dubins path from A to B
 */
export function shortestDubins(loc: PointV, dest: PointV, r: number): Path | undefined {
  // Construct flight paths
  const paths = [
    dubins(loc, dest, r, Turn.Right, Turn.Right), // rsr
    dubins(loc, dest, r, Turn.Right, Turn.Left), // rsl
    dubins(loc, dest, r, Turn.Left, Turn.Right), // lsr
    dubins(loc, dest, r, Turn.Left, Turn.Left) // lsl
  ]
  .filter((path): path is Path => !!path)

  return shortestPath(paths)
}

/**
 * Find the path that minimizes path length
 */
function shortestPath(paths: Path[]): Path | undefined {
  let best
  let bestScore = Infinity
  for (const path of paths) {
    const score = path.length()
    if (score < bestScore) {
      best = path
      bestScore = score
    }
  }
  return best
}
