import { viaWaypoints } from "../ts/plan/planner-waypoints"
import { Paraglider } from "../ts/paraglider"
import { kpow } from "../ts/geo/landingzone"
import { LandingPattern } from "../ts/plan/pattern"
import { assert } from 'chai'

const r = 100

describe('Waypoint planner', () => {
  it('should find 8 plans', () => {
    const para = new Paraglider()
    const pattern = new LandingPattern(para, kpow)
    const loc = {x: 1000, y: 1000, alt: 800, climb: -3, vx: 10, vy: 0}
    const paths = viaWaypoints(loc, pattern, r)
    assert.equal(paths.length, 8)
    assert.equal(paths[0].name, "waypoints")
    assert.equal(paths[0].segments.length, 12)
    assert.equal(paths[1].segments.length, 9)
    assert.equal(paths[2].segments.length, 6)
    assert.equal(paths[3].segments.length, 3)
    assert.approximately(paths[0].length(), 2979.3, 0.1)
    assert.approximately(paths[1].length(), 2793.1, 0.1)
    assert.approximately(paths[2].length(), 2778.8, 0.1)
    assert.approximately(paths[3].length(), 1995.5, 0.1)
  })
})
