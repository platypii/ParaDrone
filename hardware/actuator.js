// title      : ParaDrone Actuator
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Motor,actuator,linear,drone,paraglider
// file       : actuator.jscad

const qty = 1

const axleZ = 22 // center of axle
const wallZ = 27 // slightly above axle
const domeZ = 27 // a bit higher
const topZ = 50 // top top
const strutZ = axleZ - 2

const leftX = 0
const rightX = 18
const widthX = rightX - leftX
const centerX = leftX + widthX / 2

const widthY = 43
const nearY = -widthY / 2
const farY = widthY / 2

const motorRadius = 17.8
const pulleyRadius = 18
const pulleyTolerance = 0.5
const pulleyHole = pulleyRadius + pulleyTolerance

const bumpX = 6.5
const bumpY = -1.9

const sh = 2 // shell thicness

const colors = {
  motor: "darkred",
  pulley: "white",
  limitswitch: "grey",
  topcase: "purple"
}

function main() {
  return union(
    // mechanism(),
    color(colors.topcase, actuator())
  )
}

function actuator() {
  const holeY = 5.5
  return union(
    difference(
      hex(0, rightX, 0),
      // motor hole
      cylinder({r: 6.3, start: [leftX, 0, axleZ], end: [rightX, 0, axleZ], fn: 80 * qty}),
      // pulley hole
      cylinder({r: 19, start: [sh, 0, axleZ], end: [rightX, 0, axleZ], fn: 120 * qty}),
      switchhole(),
      // wire hole
      translate([-1, -9, topZ - 8], cube({size: [6, 2.2, 5], radius: 1, fn: 30 * qty})),
      // tophole
      cylinder({r: 1.4, start: [centerX, holeY, axleZ], end: [centerX, holeY, 60], fn: 30 * qty}),
      screws()
    ),
    cylinder({r1: 0, r2: 0.9, start: [centerX - bumpX / 2, bumpY, topZ - 4.5], end: [centerX - bumpX / 2, bumpY, topZ - 3], fn: 40 * qty}), // bump 1
    cylinder({r1: 0, r2: 0.9, start: [centerX + bumpX / 2, bumpY, topZ - 4.5], end: [centerX + bumpX / 2, bumpY, topZ - 3], fn: 40 * qty}) // bump 2
  )
}

function screws() {
  const screw = union(
    cylinder({r: 1.6, start: [0, 0, axleZ], end: [4, 0, axleZ], fn: 30 * qty}),
    cylinder({r1: 0, r2: 3, start: [-1.2, 0, axleZ], end: [2.3, 0, axleZ], fn: 30 * qty})
  )
  return union(
    translate([0, 15.5, 0], screw),
    translate([0, -15.5, 0], screw),
    translate([0, 7.75, 13.4], screw),
    translate([0, -7.75, 13.4], screw),
    translate([0, 7.75, -13.4], screw),
    translate([0, -7.75, -13.4], screw)
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
      [90, 0, 90],
      linear_extrude(
        { height: x2 - x1 },
        polygon([
          [y1, z1],
          [y2, z0],
          [-y2, z0],
          [-y1, z1],
          [-y1, -z1],
          [-y3, -z2],
          [y3, -z2],
          [y1, -z1]
        ])
      )
    )
  )
}

function switchhole() {
  return translate([sh, -9, topZ - 23],
    intersection(
      translate([-6, 0, 0], cube({size: [30, 18, 20], radius: 2.5, fn: 40 * qty})),
      union(
        cube({size: [20, 18, 20]}),
        // wire hole
        translate([-3, 0, 14], cube({size: [6, 2.2, 5], radius: 1.1, fn: 30 * qty}))
      )
    )
  )
}

// Mechanism
function motor() {
  return union(
    // uxcell DC 12V 200RPM Gear Box Motor
    cylinder({r: motorRadius, start: [-85, 0, axleZ], end: [-29, 0, axleZ], fn: 80 * qty}), // motor
    cylinder({r: motorRadius, start: [-28, 0, axleZ], end: [0, 0, axleZ], fn: 80 * qty}), // gear box
    cylinder({r: 3,  start: [0, 0, axleZ], end: [14, 0, axleZ]}) // shaft
  )
}

function pulley() {
  return union(
    cylinder({r: 8, start: [2, 0, axleZ], end: [9, 0, axleZ], fn: 80 * qty}), // coupling
    cylinder({r: 12, start: [6, 0, axleZ], end: [15, 0, axleZ], fn: 80 * qty}), // inner pulley
    cylinder({r: pulleyRadius, start: [6, 0, axleZ], end: [8, 0, axleZ], fn: 140 * qty}), // pulley close side
    cylinder({r: pulleyRadius, start: [13, 0, axleZ], end: [15, 0, axleZ], fn: 140 * qty}) // pulley far side
  )
}

function limitswitch() {
  return difference(
    translate([2.1, -5, topZ - sh - 5.7], cube({size: [13, 6.5, 5.7]})),
    cylinder({r: 0.9, start: [centerX - bumpX / 2, bumpY, topZ - 8], end: [centerX - bumpX / 2, bumpY, topZ - 3], fn: 40 * qty}), // bump 1
    cylinder({r: 0.9, start: [centerX + bumpX / 2, bumpY, topZ - 8], end: [centerX + bumpX / 2, bumpY, topZ - 3], fn: 40 * qty}) // bump 2
  )
}

function mechanism() {
  return union(
    // color(colors.pulley, pulley()),
    color(colors.limitswitch, limitswitch()),
    color(colors.motor, motor())
  )
}
