// title      : ParaDrone motor support
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : GPS,data,arduino,flysight
// file       : motorsupport.jscad

const qty = 1

const thic = 2 // wall thickness
const h = 20 // Start of slope
const width = 40
const width2 = width / 2
const domeLen = 9
const len = 16
const sh = 2
const sh2 = 2 * sh
const motorR = 18
const axleZ = 27
const epsilon = 0.2
const epsilon2 = 2 * epsilon
const slope = 2.6
const topZ = 36 - h
const r = 1.5

function main() {
   return union(
    // mechanism(),
    plastic()
  )
}

function plastic() {
  return difference(
    union(
      shell(),
      ramp(),
      base()
    ),
    // wire hole
    translate([-2, -3.5, sh], cube({size: [6, 7, 2.8], radius: 1, fn: 15 * qty})),
    screws()
  )
}

function base() {
  return intersection(
    translate([-8, -width2, -5], cube({size: [12, width, 10], radius: 2, fn: 15 * qty})),
    translate([-8, -width2, 0], cube({size: [11, width, sh]}))
  )
}

function ramp() {
  return difference(
    translate([6, -5, 0], cube({size: [6, 10, 8]})),
    cylinder({r: 6, start: [6, -10, 8], end: [6, 10, 8]})
  )
}

function shell() {
  return intersection(
    union(
      bottom(),
      difference(
        dome(0, domeLen, 0),
        dome(sh, domeLen, sh)
      ),
      support()
    ),
    // round the edges
    translate([0, -width2, -10], cube({size: [len, width, h + topZ + 10], radius: r, fn: 15 * qty}))
  )
}

function dome(x1, x2, domeThic) {
  const domeHeight = topZ - domeThic
  const y1 = -width2 + domeThic// * Math.sqrt(2)
  const y2 = y1 + domeHeight / slope
  const y4 = width2 - domeThic// * Math.sqrt(2)
  const y3 = y4 - domeHeight / slope
  return translate(
    [x1, 0, h],
    rotate(
      [90, 0, 90],
      linear_extrude(
        { height: x2 - x1 },
        polygon([[y1, 0], [y2, domeHeight], [y3, domeHeight], [y4, 0]])
      )
    )
  )
}

function bottom() {
  return difference(
    translate([0, -width2, 0], cube({size: [len, width, h]})), // base
    translate([sh, -width2 + sh, sh], cube({size: [len - sh, width - sh2, h - sh]})) // box hollow
  )
}

function support() {
  return difference(
    translate([12, -10, 0], cube({size: [6, 20, axleZ - 16]})),
    motor()
  )
}

function outer() {
  return union(
    translate([0, -13, h + 15.5], cube({size: [16, 26, sh]})), // top
    translate([0, sh-width2, h - 1], rotate([60, 0, 0], cube({size: [16, 20, sh]}))), // left
    translate([0, width2, h], rotate([120, 0, 0], cube({size: [16, 20, sh]}))) // right
  )
}

function screws() {
  const x = -3
  const y = 15
  const z = -2
  const screw = union(
    cylinder({r: 1.6, start: [0, 0, 0], end: [0, 0, 4], fn: 30 * qty}),
    cylinder({r1: 0, r2: 2.95, start: [0, 0, 0.8], end: [0, 0, 4], fn: 30 * qty})
  )
  return union(
    translate([x, y, z], screw),
    translate([x, -y, z], screw)
  )
}

function mechanism() {
  return color("white", motor())
}

function motor() {
  return cylinder({r: motorR, start: [10, 0, axleZ], end: [40, 0, axleZ], fn: 80 * qty})
}
