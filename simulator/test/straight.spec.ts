import { straight } from "../ts/plan/planner-straight"
import { assert } from "chai"

describe("Straight planner", () => {
  it("should find a plan", () => {
    const start = {x: 1000, y: 1000, vx: 10, vy: 0}
    const path = straight(start)
    assert.equal(path!.name, "Str")
    assert.equal(path!.segments.length, 1)
    assert.equal(path!.length(), 10)
  })
})
