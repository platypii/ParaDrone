import { Paraglider } from "../ts/paraglider"
import { kpow } from "../ts/geo/landingzone"
import { assert } from "chai"
import { search } from "../ts/autopilot"

describe("Autopilot planner", () => {
  const para = new Paraglider()

  it("autopilot far", () => {
    const far = {x: 1000, y: 1000, alt: 800, climb: -3, vx: 10, vy: 0}
    const path = search(far, para, kpow)
    const score = Math.hypot(path.end.x, path.end.y)
    assert.equal(path.length(), 3200)
    assert.equal(path.name, "NaiveR")
    assert.equal(path.segments.length, 2)
    assert.approximately(score, 1701.7, 0.1)
  })

  it("autopilot near", () => {
    const ll = {millis: 1000, lat: 47.24, lng: -123.14, alt: 884, climb: -3, vN: 0, vE: -10};
    const near = kpow.toPoint3V(ll);
    const path = search(near, para, kpow)
    const score = Math.hypot(path.end.x, path.end.y)
    assert.approximately(path.length(), 3200, 0.1)
    assert.equal(path.name, "Waypoint")
    assert.equal(path.segments.length, 13)
    assert.approximately(score, 2330.9, 0.1)
  })
})
