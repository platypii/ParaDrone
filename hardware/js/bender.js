// title      : ParaDrone Bending Guide Thingy
// author     : BASEline
// license    : MIT License
// tags       : Motor,actuator,linear,drone,paraglider
// file       : bender.jscad

const { cuboid, roundedCuboid } = require('@jscad/modeling').primitives
const { intersect, union } = require('@jscad/modeling').booleans

const mountWidth = 36.5
const baseLen = 40

function main() {
  return intersect(
    union(
      cuboid({size: [baseLen + 4, 4, 4], center: [baseLen / 2 + 2, 2, 2]}), // side1
      cuboid({size: [baseLen + 4, 4, 4], center: [baseLen / 2 + 2, 6 + mountWidth, 2]}), // side2
      cuboid({size: [4, mountWidth, 4], center: [2, mountWidth / 2 + 4, 2]}), // bumper
      cuboid({size: [4, mountWidth, 2], center: [6, mountWidth / 2 + 4, 1]}), // support
      cuboid({size: [4, mountWidth, 2], center: [baseLen + 2, mountWidth / 2 + 4, 1]}), // rule
    ),
    roundedCuboid({size: [baseLen + 4, mountWidth + 8, 20], center: [baseLen / 2 + 2, mountWidth / 2 + 4, 0], roundRadius: 2, segments: 40})
  )
}

module.exports = { main }
