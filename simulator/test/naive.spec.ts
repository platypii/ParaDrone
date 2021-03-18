import { naive } from "../ts/plan/planner-naive"
import { assert } from "chai"

const r = 100

describe("Naive planner", () => {
  it("should find a plan", () => {
    const start = {x: 1000, y: 1000, vx: 10, vy: 0}
    const end = {x: 0, y: 0, vx: 10, vy: 0}
    const path = naive(start, end, r)
    assert.equal(path!.name, "NaiveR")
    assert.equal(path!.segments.length, 2)
    assert.approximately(path!.length(), 1590, 0.1)
  })
})
