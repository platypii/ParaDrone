// title      : ParaDrone motor support
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : GPS,data,arduino,flysight
// file       : motorsupport.jscad

const qty = 1

const thic = 2 // wall thickness
const h = 14 // Start of slope
const width = 50
const width2 = width / 2
const len = 16
const sh = 2
const sh2 = 2 * sh
const motorR = 18
const axleZ = 21
const epsilon = 0.2
const epsilon2 = 2 * epsilon
const b = sh + 2.5

function main() {
   return union(
    // mechanism(),
    plastic()
  )
}

function plastic() {
  return difference(
    shell(),
    // translate([len - sh, -motorR, b], cube({size: [sh, motorR * 2, h]})), // motor square hole
    // cylinder({r: motorR, start: [10, 0, axleZ], end: [40, 0, axleZ], fn: 80 * qty}), // motor round hole
    translate([0, -4, b], cube({size: [sh2, 8, 6]})), // wire hole
    screws()
  )
}

function shell() {
  return intersection(
    union(
      bottom(),
      difference(
        dome(0, 12, 0),
        dome(sh, 12, sh)
      ),
      support()
    ),
    translate([0, -width2, 0], cube({size: [len, width, 80], radius: [1, 1, 0], fn: 15 * qty}))
  )
}

function dome(x1, x2, domeThic) {
  const slope = 2
  const domeHeight = 18 - domeThic
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
    translate([0, -width2, 0], cube({size: [len, width, h]})), // radius: [1, 1, 0], fn: 15 * qty})), // base
    translate([sh, -width2 + sh, sh], cube({size: [len - sh, width - sh2, h - sh]})) // box hollow
  )
}

function support() {
  return difference(
    translate([14, -10, 0], cube({size: [4, 20, 5]})),
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
  const z = 0
  const y = 12
  return union(
    translate([len / 2, y, z], screw()),
    translate([len / 2, -y, z], screw())
  )
}
function screw(shaftRadius) {
  const r = shaftRadius || 1.7
  return union(
    cylinder({r: r, start: [0, 0, 0], end: [0, 0, 4], fn: 30 * qty}),
    cylinder({r1: 0, r2: 2.7, start: [0, 0, 1.5], end: [0, 0, 4], fn: 30 * qty})
  )
}

function mechanism() {
  return color("white", union(
    motor(),
    translate([10, -21, b], cube({size: [7.5, 7.5, 5.7]})),
    translate([10, 13.5, b], cube({size: [7.5, 7.5, 5.7]})),
    translate([0, -3.75, b], cube({size: [7.5, 7.5, 5.7]})) // front
  ))
}

function motor() {
  return cylinder({r: motorR, start: [10, 0, axleZ], end: [40, 0, axleZ], fn: 80 * qty})
}
