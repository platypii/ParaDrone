// title      : ParaDrone Motor Strut
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Motor,actuator,linear,drone,paraglider
// file       : strut.jscad

const qty = 1

const axleZ = 21 // center of axle
const wallZ = 27 // slightly above axle
const domeZ = 27 // a bit higher
const topZ = 46 // top top

const leftX = -84
const rightX = -66
const widthX = rightX - leftX
const widthY = 40
const nearY = -widthY / 2
const farY = widthY / 2

const motorRadius = 17.8
const pulleyRadius = 15.5
const pulleyTolerance = 0.5

const colors = {
  motor: "red",
  pulley: "white",
  // topcase: [0, 0, 0.4, 0.4],
  bottomcase: "purple"
}

function main() {
  return union(
    // mechanism(),
    color(colors.bottomcase, bottomcase())
  )
}

function bottomcase() {
  const floor = translate([leftX, -widthY / 2, 0], cube({
    size: [widthX, widthY, 2],
    radius: [1, 1, 0],
    fn: 10 * qty,
    round: true
  }))
  return difference(
    union(
      floor,
      // strutbottom(-60, 18.7)
      strutbottom(-75, 18.3),
      strutbottom(-77, 18.3)
    ),
    boardholes(),
    wirechannel(),
    tophalf(16)
  )
}

function screw() {
  return union(
    cylinder({r: 1.6, start: [0, 0, 0], end: [4, 0, 0], fn: 30 * qty}),
    cylinder({r1: 0, r2: 2.7, start: [1.5, 0, 0], end: [4, 0, 0], fn: 30 * qty})
  )
}

function boardholes() {
  const mountingScrew = rotate([0, -90, 0], screw())
  const margin = 4
  return union(
    translate([leftX + margin, nearY + margin, -2], mountingScrew),
    translate([leftX + margin, farY - margin, -2], mountingScrew),
    translate([rightX - margin, nearY + margin, -2], mountingScrew),
    translate([rightX - margin, farY - margin, -2], mountingScrew)
  )
}

function wirechannel() {
  return union(
    translate([leftX, -18, 4], cube({size: [widthX, 5, 2]})),
    translate([leftX, 13, 4], cube({size: [widthX, 5, 2]}))
  )
}

function strut(x, r) {
  return difference(
    union(
      translate([x, -widthY / 2, 0], cube({size: [2, widthY, domeZ]})),
      cylinder({r: widthY / 2 - 2, start: [x, 0, domeZ], end: [x + 2, 0, domeZ], fn: 120 * qty})
    ),
    cylinder({r: r, start: [leftX, 0, axleZ], end: [rightX, 0, axleZ], fn: 120 * qty})
  )
}

function strutbottom(x, r) {
  return intersection(
    strut(x, r),
    bottomhalf(axleZ -2)
  )
}

function bottomhalf(z) {
  return translate([leftX, -widthY / 2, 0], cube({size: [widthX, widthY, z]}))
}
function tophalf(z) {
  return translate([leftX, -widthY / 2, z], cube({size: [widthX, widthY, 200]}))
}

function motor() {
  return union(
    // uxcell DC 12V 200RPM Gear Box Motor
    cylinder({r: motorRadius, start: [-85, 0, axleZ], end: [-29, 0, axleZ], fn: 80 * qty}), // motor
    cylinder({r: motorRadius, start: [-28, 0, axleZ], end: [0, 0, axleZ], fn: 80 * qty}), // gear box
    cylinder({r: 3,  start: [0, 0, axleZ], end: [14, 0, axleZ]}) // shaft
  )
}

function pulley() {
  return translate([5, 0, 0], union(
    cylinder({r: 8, start: [0, 0, axleZ], end: [9, 0, axleZ], fn: 80 * qty}), // coupling
    cylinder({r: 12, start: [6, 0, axleZ], end: [15, 0, axleZ], fn: 80 * qty}), // inner pulley
    // 1.5 inch material = 38mm diameter
    cylinder({r: pulleyRadius, start: [6, 0, axleZ], end: [8, 0, axleZ], fn: 140 * qty}), // pulley close side
    cylinder({r: pulleyRadius, start: [13, 0, axleZ], end: [15, 0, axleZ], fn: 140 * qty}) // pulley far side
  ))
}

function mechanism() {
  return union(
    color(colors.motor, motor()),
    color(colors.pulley, pulley())
  )
}
