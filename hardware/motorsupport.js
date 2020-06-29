// title      : ParaDrone motor support
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : GPS,data,arduino,flysight
// file       : motorsupport.jscad

const qty = 1

const thic = 2 // wall thickness
const h = 30
const width = 46.5
const width2 = width / 2
const len = 16
const sh = 2
const sh2 = 2 * sh
const motorR = 18
const axleZ = 21
const epsilon = 0.2
const epsilon2 = 2 * epsilon

function main() {
//   return top()
  return bottom()
}

function bottom() {
  const b = 1.5 // board thicness
  return union(
    difference(
      shell(),
      translate([sh, -width2 + sh, sh], cube({size: [len - sh2, width - sh2, h - sh]})), // hollow
      translate([len - sh, -motorR, sh + b], cube({size: [sh, motorR * 2, h]})), // motor top
      // cylinder({r: motorR, start: [10, 0, axleZ], end: [40, 0, axleZ], fn: 80 * qty}), // motor hole
      translate([0, -15, sh + b], cube({size: [sh2, 7.6, 6]})), // wire hole
      screws()
    ),
    shelf()
  )
}

function shell() {
  return translate([0, -width2, 0], cube({size: [len, width, h], radius: [1, 1, 0], fn: 15 * qty}))
}

function top() {
  return translate([sh + epsilon, -width2 + sh + epsilon, h - sh], cube({size: [8, width - sh2 - epsilon2, sh]}))
}

function shelf() {
  const inset = 3
  return difference(
    translate([0, -width2, h - sh2], cube({size: [len - sh, width, sh], radius: [1, 1, 0], fn: 15 * qty})),
    translate([inset, -width2 + inset, h - sh2], cube({size: [len - inset, width - inset * 2, sh]}))
  )
}

function screws() {
  const z = -2
  const margin = 5
  return union(
    translate([len / 2, width2 - margin, z], screw()),
    translate([len / 2, -width2 + margin, z], screw())
  )
}
function screw(shaftRadius) {
  const r = shaftRadius || 1.6
  return union(
    cylinder({r: r, start: [0, 0, 0], end: [0, 0, 4], fn: 30 * qty}),
    cylinder({r1: 0, r2: 2.7, start: [0, 0, 1.5], end: [0, 0, 4], fn: 30 * qty})
  )
}
