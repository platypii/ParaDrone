import { Turn } from "../ts/dtypes"
import { SegmentTurn } from "../ts/geo/segment-turn"
import { assert } from "chai"

describe("Turn segment", () => {
  const circle = {x: 0, y: 0, radius: 10}

  it("hard right", () => {
    const start = {x: 10, y: 0}
    const end = {x: 0, y: 10}
    const turn = new SegmentTurn(circle, start, end, Turn.Right)
    assert.equal(turn.controls().left, 0)
    assert.equal(turn.controls().right, 255)
  })

  it("hard left", () => {
    const start = {x: 10, y: 0}
    const end = {x: 0, y: 10}
    const turn = new SegmentTurn(circle, start, end, Turn.Left)
    assert.equal(turn.controls().left, 255)
    assert.equal(turn.controls().right, 0)
  })

  it("moderate turn using activation function", () => {
    const start = {x: 10, y: 0}
    const end = {x: 9.6, y: 2.8}
    const turn = new SegmentTurn(circle, start, end, Turn.Left)
    assert.equal(turn.controls().left, 97)
    assert.equal(turn.controls().right, 0)
  })

  it("no turn", () => {
    const start = {x: 10, y: 0}
    const turn = new SegmentTurn(circle, start, start, Turn.Left)
    assert.equal(turn.controls().left, 0)
    assert.equal(turn.controls().right, 0)
  })
})
