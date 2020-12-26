import { LandingZone } from "./geo/landingzone"
import { Path } from "./geo/paths"
import { SegmentLine } from "./geo/segment-line"
import { Paraglider } from "./paraglider"
import { LandingPattern } from "./plan/pattern"
import { best_plan, no_turns_below, path_no_turns_below } from "./plan/planner"
import { naive } from "./plan/planner-naive"
import { straight } from "./plan/planner-straight"
import { viaWaypoints } from "./plan/planner-waypoints"
import { shortestDubins } from "./plan/shortest-dubins"

/**
 * Controls a paraglider toward a landing zone
 */
export class Autopilot {
  public plan?: Path

  private readonly lz: LandingZone
  private readonly para: Paraglider
  private readonly pattern: LandingPattern
  private readonly effectiveRadius: number // toggles take time

  constructor(para: Paraglider, lz: LandingZone) {
    this.lz = lz
    this.para = para
    this.pattern = new LandingPattern(this.para, this.lz)
    this.effectiveRadius = this.para.turnRadius * 2
    // Subscribe to gps updates
    para.onLocationUpdate(() => {
      // Replan with new location
      this.plan = this.replan()
      // Apply to paraglider toggles
      para.setControls(this.plan.controls())
    })
  }

  /**
   * Apply autopilot rules, and then search over waypoint paths
   */
  private replan(): Path {
    const loc = this.lz.toPoint3V(this.para.loc!)
    if (isNaN(loc.x)) {
      console.error("invalid location", this.para.loc)
      throw new Error("invalid location")
    }
    // How much farther can we fly with available altitude?
    const flight_distance_remaining = this.para.flightDistanceRemaining(loc.alt)

    const dist = Math.sqrt(loc.x * loc.x + loc.y * loc.y)

    // No velocity, hard to plan, and holding into the wind seems good
    if (loc.vx === 0 && loc.vy === 0) {
      return new Path("default", new SegmentLine(loc, this.lz.dest))
    }

    // Construct flight paths
    const straightPath = straight(loc).fly(Math.max(1, flight_distance_remaining))

    if (loc.alt <= no_turns_below) {
      // No turns under 100ft
      return straightPath
    } else if (dist > 1000) {
      const naivePath = naive(loc, this.pattern.startOfFinal(), this.effectiveRadius) || straightPath
      return path_no_turns_below(this.para, naivePath, loc.alt)
    } else {
      const paths = [
        ...viaWaypoints(loc, this.pattern, this.effectiveRadius),
        // dubins(loc, dest, r, Turn.Right, Turn.Right), // rsr
        // dubins(loc, dest, r, Turn.Right, Turn.Left), // rsl
        // dubins(loc, dest, r, Turn.Left, Turn.Right), // lsr
        // dubins(loc, dest, r, Turn.Left, Turn.Left), // lsl
        shortestDubins(loc, this.lz.dest, this.effectiveRadius),
        // naive(loc, dest, r),
        straightPath
      ]
      .filter((path): path is Path => !!path)
      .map((p) => path_no_turns_below(this.para, p, loc.alt))
      return best_plan(this.lz, paths) || straightPath
    }
  }
}
