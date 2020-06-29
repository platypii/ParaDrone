// title      : ParaDrone Actuator
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Motor,actuator,linear,drone,paraglider
// file       : actuator.jscad

const qty = 1

const axleZ = 21 // center of axle
const wallZ = 27 // slightly above axle
const domeZ = 27 // a bit higher
const topZ = 46 // top top
const strutZ = axleZ - 2

const leftX = 0
const rightX = 31
const widthX = rightX - leftX
const widthY = 43
const nearY = -widthY / 2
const farY = widthY / 2

const motorRadius = 17.8
const pulleyRadius = 15.5
const pulleyTolerance = 0.5
const pulleyHole = pulleyRadius + pulleyTolerance

const colors = {
  motor: "red",
  pulley: "white",
  limitswitch: "black",
  // topcase: [0, 0, 0.4, 0.4],
  topcase: "magenta",
  bottomcase: "purple"
}

function main() {
  return union(
    // mechanism(),
    color(colors.topcase, topcase())
    // color(colors.bottomcase, bottomcase())
  )
}


// Top
function topcase() {
  return union(
    dome(0.25, 2.75),
    switchmount(),
    difference( // top walls
      translate([4.25, -widthY / 2 + 2, strutZ], cube({size: [rightX - 8.5, widthY - 4, wallZ - strutZ]})),
      translate([4.25, -widthY / 2 + 3.9, strutZ], cube({size: [rightX - 8.5, widthY - 7.8, wallZ - strutZ]})),
      linchpin()
    ),
    difference(
      union(
        struttop(10, pulleyHole),
        struttop(11, pulleyHole),
        struttop(18, pulleyHole),
        struttop(19, pulleyHole)
      ),
      tophalf(topZ),
      walls(),
      translate([8, -8, 33], cube({size: [16, 16, 12]})) // cutout for switch
    )
  )
}

function shell(x1, x2, domeThic) {
  const slope = 2
  const domeHeight = topZ - domeZ - domeThic
  const y1 = nearY + domeThic * Math.sqrt(2)
  const y2 = y1 + domeHeight / slope
  const y4 = farY - domeThic * Math.sqrt(2)
  const y3 = y4 - domeHeight / slope
  return translate(
    [x1, 0, domeZ],
    rotate(
      [90, 0, 90],
      linear_extrude(
        { height: x2 - x1 },
        polygon([[y1, 0], [y2, domeHeight], [y3, domeHeight], [y4, 0]])
      )
    )
  )
}

function dome(domeToleranceX, domeThic) {
  const x1 = 2 + domeToleranceX
  const x2 = rightX - 2 - domeToleranceX
  const holeY = 7
  return difference(
    shell(x1, x2, 0),
    shell(x1, x2, domeThic),
    cylinder({r: 1.4, start: [15.5, holeY, 0], end: [15.5, holeY, 60], fn: 30 * qty}) // tophole
  )
}

function switchmount() {
const domeThic = 2.8
  const latch = rotate(
    [-90, 0, 0],
    linear_extrude({height: 27}, polygon([[0, 0], [0, 7.8], [0.8, 7.7], [1.1, 6.7], [0.8, 5.7], [0.8, 0]]))
  )
  const latch1 = translate([4.5, -13.5, topZ - domeThic], latch)
  const latch2 = translate([26.5, -13.5, topZ - domeThic], mirror([1, 0, 0], latch))
  return intersection(
    union(
      latch1,
      latch2,
      cylinder({r: 1.0, start: [10.75, -4, topZ - 4], end: [10.75, -4, topZ - 3], fn: 40 * qty}),
      cylinder({r: 1.0, start: [20.25, -4, topZ - 4], end: [20.25, -4, topZ - 3], fn: 40 * qty})
    ),
    shell(0, rightX, 0)
  )
}


// Bottom
function bottomcase() {
  const floor = translate([leftX, -widthY / 2, 0], cube({size: [widthX, widthY, 2]}))
  return difference(
    union(
      floor,
      walls(),
      strutbottom(11, pulleyHole),
      strutbottom(18, pulleyHole),
      bracket(),
      endbracket()
    ),
    linchpin(),
    boardholes(),
    wirechannel(),
    tophalf(topZ)
  )
}

function walls() {
  return difference(
    translate([0, -widthY / 2, 0], cube({size: [rightX, widthY, wallZ]})),
    translate([0, -widthY / 2 + 2, 0], cube({size: [rightX, widthY - 4, wallZ]}))
  )
}

function bracket() {
  return difference(
    union(
      translate([0, -widthY / 2, 0], cube({size: [4, widthY, wallZ]})),
      shell(0, 4, 0)
    ),
    motorhole(),
    translate([0, -15.5, axleZ], screw()),
    translate([0, 15.5, axleZ], screw()),
    dome(0, 3)
  )
}

// Specially crafted to be easier to print
function motorhole() {
  const r = 6.3
  return difference(
    union(
      cylinder({r: r, start: [leftX, 0, axleZ], end: [rightX, 0, axleZ], fn: 80 * qty}), // motor hole
      translate([0, 0, axleZ], rotate([45, 0, 0], cube({size: [4, r, r]})))
    ),
    tophalf(axleZ + r)
  )
}

function endbracket() {
  return difference(
    union(
      translate([rightX - 4, -widthY / 2, 0], cube({size: [4, widthY, wallZ]})),
      shell(rightX - 2, rightX, 0)
    ),
    cylinder({r: pulleyHole, start: [rightX - 4, 0, axleZ], end: [rightX, 0, axleZ], fn: 120 * qty}), // Front hole
    tophalf(wallZ + 4),
    translate([rightX - 4, -pulleyHole, axleZ], cube({size: [4, pulleyHole * 2, 100]})),
    dome(0, 3)
  )
}

function boardholes() {
  const mountingScrew = rotate([0, -90, 0], screw())
  const margin = 5.5
  return union(
    translate([leftX + margin + 2, nearY + margin, -2], mountingScrew),
    translate([leftX + margin + 2, farY - margin, -2], mountingScrew),
    translate([rightX - margin - 2, nearY + margin, -2], mountingScrew),
    translate([rightX - margin - 2, farY - margin, -2], mountingScrew)
  )
}

function wirechannel() {
  const w = 6
  const h = 3
  return union(
    translate([leftX, nearY + 2, 2], cube({size: [widthX - 4, w, h]})),
    translate([leftX, farY - w - 2, 2], cube({size: [widthX - 4, w, h]}))
  )
}

function linchpin() {
  return union(
    translate([8.4, nearY + 4, 23], rotate([90, 0, -90], screw(1.4))),
    translate([8.4, farY - 4, 23], rotate([90, 0, 90], screw(1.4))),
    translate([22.6, nearY + 4, 23], rotate([90, 0, -90], screw(1.4))),
    translate([22.6, farY - 4, 23], rotate([90, 0, 90], screw(1.4)))
  )
}


// Common
function screw(shaftRadius) {
  const r = shaftRadius || 1.6
  return union(
    cylinder({r: r, start: [0, 0, 0], end: [4, 0, 0], fn: 30 * qty}),
    cylinder({r1: 0, r2: 2.7, start: [1.5, 0, 0], end: [4, 0, 0], fn: 30 * qty})
  )
}

function strut(x, r) {
  return difference(
    union(
      translate([x, -widthY / 2, 0], cube({size: [2, widthY, domeZ]})),
      shell(x, x + 2, 0)
    ),
    cylinder({r: r, start: [leftX, 0, axleZ], end: [rightX, 0, axleZ], fn: 120 * qty})
  )
}
function strutbottom(x, r) {
  return intersection(
    strut(x, r),
    bottomhalf(strutZ)
  )
}
function struttop(x, r) {
  return difference(
    strut(x, r),
    translate([x, -pulleyHole, strutZ], cube({size: [2, pulleyHole * 2, 2]})),
    bottomhalf(strutZ)
  )
}


// Util
function bottomhalf(z) {
  return translate([leftX, -widthY / 2, 0], cube({size: [widthX, widthY, z]}))
}
function tophalf(z) {
  return translate([leftX, -widthY / 2, z], cube({size: [widthX, widthY, 200]}))
}
function righthalf(x) {
  return translate([x, -widthY / 2, 0], cube({size: [widthX, widthY, topZ]}))
}
function lefthalf(x) {
  return translate([0, -widthY / 2, 0], cube({size: [x, widthY, topZ]}))
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
  return translate([5, 0, 0], union(
    cylinder({r: 8, start: [0, 0, axleZ], end: [9, 0, axleZ], fn: 80 * qty}), // coupling
    cylinder({r: 12, start: [6, 0, axleZ], end: [15, 0, axleZ], fn: 80 * qty}), // inner pulley
    // 1.5 inch material = 38mm diameter
    cylinder({r: pulleyRadius, start: [6, 0, axleZ], end: [8, 0, axleZ], fn: 140 * qty}), // pulley close side
    cylinder({r: pulleyRadius, start: [13, 0, axleZ], end: [15, 0, axleZ], fn: 140 * qty}) // pulley far side
  ))
}

function limitswitch() {
  return translate([6, -7.99, topZ - 2.8 - 6.5], cube({size: [20, 10.6, 6.5]}))
}

function mechanism() {
  return union(
    color(colors.limitswitch, limitswitch()),
    color(colors.motor, motor()),
    color(colors.pulley, pulley())
  )
}
