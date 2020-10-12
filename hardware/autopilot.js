// title      : ParaDrone Autopilot Case
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Microcontroller,case,arduino,autopilot,drone,paraglider
// file       : autopilot.jscad

const qty = 1

const topY = 28.9
const bottomY = -31.8

const sizeX = 77.5
const sizeY = topY - bottomY
const sizeZ = 21.8

const leftX = -sizeX / 2
const rightX = sizeX / 2

const sh = 2 // shell width

const colors = {
  esp32: "white",
  bn220: "blue",
  r8ef: "black",
  driver: "green",
  topcase: "magenta",
  bottomcase: "purple",
  bec: "yellow"
}

function main() {
  return union(
    // mechanism(),
    color(colors.topcase, topcase())
    // color(colors.bottomcase, bottomcase())
  )
}

function both() {
  return difference(
    box(0, 0, sizeZ), // outer
    box(sh, -1, sizeZ - sh) // inner
  )
}

function topcase() {
  const hole = union(
    cube({size: [4, 10.2, 13.5], radius: .6, fn: 10 * qty}),
    translate([0, 2.1, 0], cube({size: [4, 6, 16], radius: .6, fn: 10 * qty}))
  )
  return difference(
    intersection(
      both(),
      translate([leftX, bottomY, -3], cube({size: [sizeX, sizeY, sizeZ + 4], radius: 2.5, fn: 22 * qty})) // rounding
    ),
    translate([rightX - 3, -24.6, -1], hole), // motor hole
    translate([leftX - 1, -24.6, -1], hole), // motor hole
    translate([leftX - 1, -15.6, -3.5], hole), // power hole
    translate([leftX - 1, -18, -3.5], cube({size: [4, 6, 16]})), // power hole // bump remove
    translate([leftX - 1, 9, sizeZ - 6.6], cube({size: [4, 8, 3], radius: .6, fn: 10 * qty})), // USB hole
    translate([leftX - .1, 7.6, sizeZ - 8.5], cube({size: [1, 10.8, 7], radius: [0, 1, 1], fn: 10 * qty})), // USB reccess
    translate([11, topY - 3, sizeZ - 8], cube({size: [4, 4, 4], radius: .2, fn: 10 * qty})), // antenna hole
    latches(latchhole),
    screwholes(),
    mechanism()
  )
}

function bottomcase() {
  return union(
    difference(
      box(sh + 0.2, 0, sh),
      translate([-25, -24, -1], roundedRect(50, 45, 4, 2, 16)), // Save bottom weight
      cylinder({r: 2.1, start: [leftX + 4.7, -10.2, 0.8], end: [leftX + 4.8, -10.2, 3]}), // molex recess
      cylinder({r: 2.1, start: [leftX + 4.7, -19.5, 0.8], end: [leftX + 4.8, -19.5, 3]}), // molex recess
      cylinder({r: 2.1, start: [rightX - 4.7, -19.5, 0.8], end: [rightX - 4.8, -19.5, 3]}), // molex recess
      boardholes()
    ),
    translate([rightX - 2.5, -24.3, 0], cube({size: [2.5, 9.6, 2]})), // motor1 base
    translate([leftX, -24.3, 0], cube({size: [2.5, 18.6, 2]})), // motor2 hole
    screwblocks(),
    latches(latch)
  )
}

function box(delta, z1, z2) {
  const r = 3 - delta // corner radius
  return translate(
    [leftX + delta, bottomY + delta, z1],
    roundedRect(sizeX - 2 * delta, sizeY - 2 * delta, z2 - z1, r, 16))
}

function boardholes() {
  const mountingScrew = rotate([0, -90, 0], screw())
  const margin = 6
  const z = -2
  return union(
    translate([leftX + margin, topY - margin, z], mountingScrew),
    translate([rightX - margin, topY - margin, z], mountingScrew),
    translate([leftX + margin, bottomY + margin, z], mountingScrew),
    translate([rightX - margin, bottomY + margin, z], mountingScrew)
  )
}

function screwblocks() {
  return difference(
    union(
      translate([leftX + 9, bottomY + 2.2, 2], cube({size: [6, 4.5, 4.5]})),
      translate([rightX - 15, bottomY + 2.2, 2], cube({size: [6, 4.5, 4.5]}))
    ),
    screwholes()
  )
}

function screwholes() {
  const scrw = rotate([0, 0, -90], screw())
  return union(
    translate([leftX + 12, bottomY + 4, 4], scrw),
    translate([rightX - 12, bottomY + 4, 4], scrw)
  )
}

// Common
function roundedRect(x, y, z, r, q) {
  return intersection(
    translate([0, 0, -2 * r], cube({size: [x, y, z + 4 * r], radius: r, fn: qty * q})),
    cube({size: [x, y, z]})
  )
}
function screw() {
  return union(
    cylinder({r: 1.6, start: [-1, 0, 0], end: [4, 0, 0], fn: 30 * qty}),
    cylinder({r1: 0, r2: 2.95, start: [0.8, 0, 0], end: [4, 0, 0], fn: 30 * qty})
  )
}

function bottomhalf(z) {
  return translate([leftX, bottomY, 0], cube({size: [sizeX, sizeY, z]}))
}
function tophalf(z) {
  return translate([leftX, bottomY, z], cube({size: [sizeX, sizeY, 200]}))
}

function latches(gen) {
  return translate([-25, topY - 4.21, 0], gen(90, 50))
}
function latch(angle, len) {
  return rotate(
    [90, 0, angle],
    linear_extrude({height: len}, polygon(
      [[2, 0], [2, 2], [3.1, 2]]
    ))
  )
}
function latchhole(angle, len) {
  return rotate(
    [90, 0, angle],
    linear_extrude({height: len}, polygon(
      [[2, 0], [2, 2], [3, 2]]
    ))
  )
}

// Mechanism
function mechanism() {
  return union(
    esp32(),
    bn220(),
    driver(),
    bec()
  )
}

function esp32() {
  const h = 25.4
  const circuitboard = linear_extrude({height: 2.5}, polygon([
    [0,8.2], [-1,7.2], [-1,1.8], [1,0], [48,0], [48,4], [52.3,8], [52.3,h-8], [48,h-4], [48,h], [1,h], [-1,h-1.8], [-1,h-7.2], [0,h-8.2]
  ]))
  return translate([leftX + 1.8, 0, sizeZ - 16.4], union(
    color(colors.esp32, translate([0, 0.3, 9], circuitboard)),
    color([0, 0, 0, 0.4], translate([13.9, 2.8, 12], cube({size: [35, 20.4, 3.8]}))),
    color([0, 0, 0, 0.4], translate([24.5, 1, 12], cube({size: [14, 2, 3.8]}))),
    color("black", translate([18.7, 8, 12], cube({size: [26.1, 13, 4.8]}))),
    color("brown", cylinder({r: 3.2, start: [11, 19.5, 10], end: [11, 19.5, 15.8], fn: 25 * qty}))
  ))
}

function bn220() {
  return translate([rightX - 22.1, 3.5, sizeZ - 8.2], union(
    color(colors.bn220, translate([0, 0, 0], cube({size: [20, 22, 4.2]}))),
    color("brown", translate([1, 2, 4.4], cube({size: [18, 18, 2], round: true, radius: [1, 1, 0], fn: 15 * qty})))
  ))
}

function driver() {
  return color(colors.driver, translate([-23, -29, sizeZ - 7.8], cube({size: [46, 28, 6]})))
}

function bec() {
  return color(colors.bec, translate([leftX + 5, -13, sizeZ - 6], cube({size: [9, 12, 3]})))
}
