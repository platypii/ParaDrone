import { Motor } from "../ts/motor"
import { assert } from "chai"

describe("Motor model", () => {
  it("adjust speed based on target", () => {
    const motor = new Motor()
    assert.equal(motor.target, 0)
    assert.equal(motor.position, 0)
    assert.equal(motor.speed, 0)

    motor.target = 40

    motor.update(0) // Set motor speed
    assert.equal(motor.target, 40)
    assert.equal(motor.position, 0) // Movement hasn't happened yet
    assert.equal(motor.speed, 255)

    motor.update(100) // 10 Hz
    assert.equal(motor.target, 40)
    assert.approximately(motor.position, 6.3, 0.1)
    assert.equal(motor.speed, 255)
  })
})
