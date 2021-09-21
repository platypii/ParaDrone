import { Toggles } from "../ts/toggles"
import { assert } from "chai"

describe("Toggles model", () => {
  it("adjust speed based on target", () => {
    const toggles = new Toggles()
    assert.equal(toggles.left.target, 0)
    assert.equal(toggles.right.target, 0)
    assert.equal(toggles.left.position, 0)
    assert.equal(toggles.right.position, 0)
    assert.equal(toggles.left.speed, 0)
    assert.equal(toggles.right.speed, 0)

    toggles.setToggles(40, 0)
    toggles.update(100) // 10 Hz
    toggles.update(100) // 10 Hz

    assert.equal(toggles.left.target, 40)
    assert.equal(toggles.right.target, 0)
    assert.approximately(toggles.left.position, 3.9, 0.01)
    assert.equal(toggles.right.position, 0)
    assert.equal(toggles.left.speed, 255)
    assert.equal(toggles.right.speed, 0)
  })
})
