import { shortestDubins } from "../ts/plan/shortest-dubins"
import { kpow } from "../ts/geo/landingzone"
import { assert } from 'chai'

const r = 100

describe('Shortest dubins planner', () => {
  it('should find a plan', () => {
    const start = {x: 1000, y: 1000, vx: 10, vy: 0}
    const end = {x: 0, y: 0, vx: 10, vy: 0}
    const path = shortestDubins(start, kpow.dest, r)
    assert.equal(path!.name, "dubins")
    assert.equal(path!.segments.length, 3)
    assert.approximately(path!.length(), 1848.3, 0.1)
  })
})
