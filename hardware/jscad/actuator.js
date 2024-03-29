// title      : ParaDrone Actuator
// author     : BASEline
// tags       : Motor,actuator,linear,drone,paraglider

const jscad = require("@jscad/modeling")
const { subtract, union } = jscad.booleans
const { colorize, cssColors } = jscad.colors
const { extrudeLinear } = jscad.extrusions
const { circle, cuboid, cylinder, cylinderElliptic, polygon, roundedRectangle } = jscad.primitives
const { rotate, rotateY, translate } = jscad.transforms

let qty = 1

const axleZ = 26 // center of axle
const topZ = axleZ + 24 // top top

const leftX = 0
const rightX = 13
const sizeX = rightX - leftX
const centerX = leftX + sizeX / 2

const sizeY = 43
const nearY = -sizeY / 2

const motorRadius = 17.8
const pulleyRadius = 18

const sh = 2 // shell thicness

const colors = {
  motor: cssColors.silver,
  spool: cssColors.darkgrey,
  mount: cssColors.black,
  topcase: cssColors.purple
}

function getParameterDefinitions() {
  return [{name: "print", type: "checkbox", checked: false, caption: "Print mode"}]
}

function main(params) {
  if (params.print) {
    qty = 3
    return rotate([-Math.PI / 2, -Math.PI / 2, 0], actuator())
  } else {
    return [
      colorize(colors.topcase, actuator()),
      mechanism()
    ]
  }
}

function actuator() {
  const holeY = 5.5
  return subtract(
    hex(0, rightX, 0),
    // tophole
    cylinder({radius: 1.4, center: [centerX, holeY, topZ], height: 10, segments: 15 * qty}),
    // motor hole
    axialCylinder(11, leftX, rightX, 60 * qty),
    // pulley hole
    axialCylinder(19, sh, rightX, 120 * qty),
    switchhole(),
    screws()
  )
}

function screws() {
  const screw = rotateY(Math.PI / 2, [
    cylinder({radius: 1.6, center: [0, 0, 1.5], height: 3, segments: 15 * qty}),
    cylinderElliptic({startRadius: [0, 0], endRadius: [3, 3], center: [0, 0, 0.5], height: 3.5, segments: 15 * qty})
  ])
  return [
    translate([0, 13.9, axleZ], screw),
    translate([0, -13.9, axleZ], screw),
    translate([0, 0, axleZ + 13.9], screw),
    translate([0, 0, axleZ - 13.9], screw)
  ]
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
  return translate([sh, 0, topZ - 13], rotateY(Math.PI / 2,
    extrudeLinear({height: sizeX}, roundedRectangle({size: [20, 20], roundRadius: 2.5, segments: 30 * qty}))
  ))
}

// Mechanism
function motor() {
  return [
    // uxcell DC 12V 200RPM Gear Box Motor
    axialCylinder(motorRadius - 0.5, -108, -95, 80 * qty), // encoder
    axialCylinder(motorRadius, -94.5, -36.5, 80 * qty), // motor
    axialCylinder(motorRadius - 0.5, -36, -2, 80 * qty), // gear box
    axialCylinder(3, 0, 14, 20 * qty), // shaft
  ]
}

function spool() {
  return [
    axialCylinder(8, 2, 15, 50 * qty), // coupling
    axialCylinder(pulleyRadius, 2, 4, 80 * qty), // spool inside
    axialCylinder(pulleyRadius, 9, 11, 80 * qty) // spool outside
  ]
}

function mount() {
  const base = extrudeLinear({ height: 2 },
    subtract(
      roundedRectangle({ center: [-19, 0], roundRadius: 1.5, size: [38, 36] }),
      circle({ center: [-5, -15], radius: 1 }),
      circle({ center: [-35, -15], radius: 1 }),
      circle({ center: [-5, 15], radius: 1 }),
      circle({ center: [-35, 15], radius: 1 })
    )
  )
  return union(
    base,
    cuboid({center: [-1, 0, 13], size: [2, 36, 26]}),
    axialCylinder(18, -2, 0, 80 * qty)
  )
}

function mechanism() {
  return [
    colorize(colors.spool, spool()),
    colorize(colors.motor, motor()),
    colorize(colors.mount, mount())
  ]
}

/**
 * Return a cylinder along the motor axis
 */
function axialCylinder(radius, x1, x2, segments) {
  const height = x2 - x1
  const center = [-axleZ, 0, x1 + height / 2]
  return rotateY(Math.PI / 2, cylinder({center, height, radius, segments}))
}

module.exports = { getParameterDefinitions, main }
