// title      : ParaDrone Actuator
// author     : BASEline
// license    : MIT License
// tags       : Motor,actuator,linear,drone,paraglider
// file       : actuator.jscad

const { intersect, subtract, union } = require('@jscad/modeling').booleans
const { colorize, cssColors } = require('@jscad/modeling').colors
const { extrudeLinear } = require('@jscad/modeling').extrusions
const { rotate, translate } = require('@jscad/modeling').transforms
const { cuboid, cylinder, cylinderElliptic, polygon, roundedCuboid } = require('@jscad/modeling').primitives

function cylinderV1(opt) {
  const rot = [0, 0, 0]
  const center = [0, 0, 0]
  if (opt.start && opt.end) {
    center[0] = (opt.start[0] + opt.end[0]) / 2
    center[1] = (opt.start[1] + opt.end[1]) / 2
    center[2] = (opt.start[2] + opt.end[2]) / 2
    opt.height = Math.hypot(opt.start[0] - opt.end[0], opt.start[1] - opt.end[1], opt.start[2] - opt.end[2])
    if (opt.start[0] !== opt.end[0]) {
      rot[1] = -Math.PI / 2 * Math.sign(opt.start[0] - opt.end[0])
    }
    if (opt.start[1] !== opt.end[1]) {
      rot[0] = Math.PI / 2 * Math.sign(opt.start[1] - opt.end[1])
    }
  }
  if (opt.r1 === 0) {
    opt.r1 = 0.01
  }
  if (opt.r2 === 0) {
    opt.r2 = 0.01
  }
  if (opt.r1 && opt.r2) {
    opt.startRadius = [opt.r1, opt.r1]
    opt.endRadius = [opt.r2, opt.r2]
    return translate(center, rotate(rot, cylinderElliptic(opt)))
  } else {
    return translate(center, rotate(rot, cylinder(opt)))
  }
}

const qty = 1

const axleZ = 22 // center of axle
const topZ = 46 // top top

const leftX = 0
const rightX = 13
const widthX = rightX - leftX
const centerX = leftX + widthX / 2

const widthY = 43
const nearY = -widthY / 2
const farY = widthY / 2

const motorRadius = 17.8
const pulleyRadius = 18

const sh = 2 // shell thicness

const colors = {
  motor: cssColors.darkred,
  pulley: cssColors.white,
  topcase: cssColors.purple
}

function main() {
  return union(
    mechanism(),
    colorize(colors.topcase, actuator())
  )
}

function actuator() {
  const holeY = 5.5
  return subtract(
    hex(0, rightX, 0),
    // motor hole
    cylinderV1({radius: 11, start: [leftX, 0, axleZ], end: [rightX, 0, axleZ], segments: 80 * qty}),
    // pulley hole
    cylinderV1({radius: 19, start: [sh, 0, axleZ], end: [rightX, 0, axleZ], segments: 120 * qty}),
    switchhole(),
    // tophole
    cylinderV1({radius: 1.4, start: [centerX, holeY, axleZ], end: [centerX, holeY, 60], segments: 30 * qty}),
    screws()
  )
}

function screws() {
  const screw = union(
    cylinderV1({radius: 1.6, start: [0, 0, axleZ], end: [4, 0, axleZ], segments: 30 * qty}),
    cylinderV1({r1: 0, r2: 3, start: [-1.2, 0, axleZ], end: [2.3, 0, axleZ], segments: 30 * qty})
  )
  return union(
    translate([0, 13.9, 0], screw),
    translate([0, -13.9, 0], screw),
    translate([0, 0, 13.9], screw),
    translate([0, 0, -13.9], screw)
  )
}

function hex(x1, x2, domeThic) {
  const slope = 1.6
  const z0 = topZ - axleZ
  const z1 = 7
  const z2 = 22 // bottom
  const domeHeight = z0 - z1
  const y1 = nearY + domeThic * Math.sqrt(2)
  const y2 = y1 + domeHeight / slope
  const y3 = y2
  return translate(
    [x1, 0, axleZ],
    rotate(
      [Math.PI / 2, 0, Math.PI / 2],
      extrudeLinear(
        { height: x2 - x1 },
        polygon({ points: [
          [y1, -z1],
          [y3, -z2],
          [-y3, -z2],
          [-y1, -z1],
          [-y1, z1],
          [-y2, z0],
          [y2, z0],
          [y1, z1],
        ]})
      )
    )
  )
}

function switchhole() {
  return translate([sh, -10, topZ - 23],
    intersect(
      roundedCuboid({size: [30, 20, 20], center: [9, 10, 10], roundRadius: 2.5, segments: 40 * qty}),
      cuboid({size: [20, 20, 20], center: [10, 10, 10]})
    )
  )
}

// Mechanism
function motor() {
  return union(
    // uxcell DC 12V 200RPM Gear Box Motor
    cylinderV1({radius: motorRadius, start: [-85, 0, axleZ], end: [-29, 0, axleZ], segments: 80 * qty}), // motor
    cylinderV1({radius: motorRadius, start: [-28, 0, axleZ], end: [0, 0, axleZ], segments: 80 * qty}), // gear box
    cylinderV1({radius: 3,  start: [0, 0, axleZ], end: [14, 0, axleZ]}) // shaft
  )
}

function pulley() {
  return union(
    cylinderV1({radius: 8, start: [2, 0, axleZ], end: [15, 0, axleZ], segments: 80 * qty}), // coupling
    cylinderV1({radius: pulleyRadius, start: [2, 0, axleZ], end: [4, 0, axleZ], segments: 140 * qty}), // pulley close side
    cylinderV1({radius: pulleyRadius, start: [9, 0, axleZ], end: [11, 0, axleZ], segments: 140 * qty}) // pulley far side
  )
}

function mechanism() {
  return union(
    colorize(colors.pulley, pulley()),
    colorize(colors.motor, motor())
  )
}

module.exports = { main }
