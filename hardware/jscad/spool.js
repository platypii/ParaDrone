// title      : ParaDrone Actuator Pulley Spool
// author     : BASEline
// tags       : Motor,actuator,linear,drone,paraglider

const jscad = require("@jscad/modeling")
const { subtract, union } = jscad.booleans
const { cuboid, cylinder } = jscad.primitives
const { colorize } = jscad.colors

let qty = 1

// oversized washers
// https://www.mcmaster.com/large-diameter-washers/od~1-1-2/
const washerThic = 1.5
const washerOuter = 1.5 * 12.7 // outer radius
const washerInnerSmall = 6.7
const washerInnerLarge = 8.3
const washerColor = [0.1, 0.1, 0.1, 0.99]

//    ||    core
// ===||=== washer
//   ||||   core
// ==||||== washer
//  ||||||  core
const z1 = 3
const z2 = 10
const z3 = 13
const shaftRadius = 4.2
const threadRadius = 1.1

function getParameterDefinitions() {
  return [
    {name: "print", type: "checkbox", checked: false, caption: "Print mode"}
  ]
}

function main(params) {
  if (params.print) {
    qty = 3
    return core()
  } else {
    return [
      washer1(),
      washer2(),
      core()
    ]
  }
}

const washer1 = () => {
  const center = [0, 0, z1 + washerThic / 2]
  const height = washerThic
  const washer = subtract(
    cylinder({ center, height, radius: washerOuter, segments: qty * 120 }),
    cylinder({ center, height, radius: washerInnerLarge, segments: qty * 100 })
  )
  return colorize(washerColor, washer)
}

const washer2 = () => {
  const center = [0, 0, z2 + washerThic / 2]
  const height = washerThic
  const washer = subtract(
    cylinder({ center, height, radius: washerOuter, segments: qty * 120 }),
    cylinder({ center, height, radius: washerInnerSmall, segments: qty * 100 }),
    threadhole()
  )
  return colorize(washerColor, washer)
}

const core = () => {
  return subtract(
    union(
      cylinder({radius: 0.58 * washerOuter, height: z1, center: [0, 0, z1 / 2], segments: qty * 100}), // axle
      cylinder({radius: washerInnerLarge, height: z2 - z1, center: [0, 0, z1 + (z2 - z1) / 2], segments: qty * 100}), // axle
      cylinder({radius: washerInnerSmall, height: z3 - z2, center: [0, 0, z2 + (z3 - z2) / 2], segments: qty * 100}) // axle
    ),
    dShaft()
  )
}

/**
 * D-shaft
 */
function dShaft() {
  const shaft = cylinder({radius: shaftRadius, height: 18, center: [0, 0, 9], segments: qty * 80})
  return subtract(
    shaft,
    cuboid({size: [100, 100, 100], center: [0, 53, 50]})
  )
}

/**
 * Thread hole to secure pulley string
 */
function threadhole() {
  const center = [0, washerInnerSmall + threadRadius * 2, z2 + washerThic / 2]
  return cylinder({radius: threadRadius, height: washerThic, center, segments: qty * 24})
}

module.exports = { getParameterDefinitions, main }
