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
    color(colors.topcase, topcase()),
    color(colors.bottomcase, bottomcase())
  )
}

function topcase() {
  return union(
    difference(
      // gentle bevel
      intersection(
        translate([leftX, bottomY, -9.2], cube({size: [sizeX, sizeY, sizeZ + 10], radius: 3, fn: 22 * qty})),
        translate([leftX, bottomY, 0], cube({size: [sizeX, sizeY, sizeZ]}))
      ),
      box(sh, -1, sizeZ - sh), // inner
      connectors(),
      mechanism()
    ),
    topscrews()
  )
}

function bottomcase() {
  const support = cylinder({r: 3.2, start: [0, 0, 1], end: [0, 0, 2.6], fn: 30 * qty})
  return difference(
    union(
      box(sh + 0.2, 0, sh),
      translate([rightX - 2.5, -23.8, 0], cube({size: [2.5, 9.6, sh]})), // motor1 base
      translate([leftX, -23.5, 0], cube({size: [2.5, 19.2, sh]})), // motor2 base
      translate([0, topY - 10, 0], support),
      translate([leftX + 5.8, bottomY + 5.8, 0], support),
      translate([rightX - 5.8, bottomY + 5.8, 0], support)
    ),
    translate([-25, -24, -1], roundedRect(50, 38, 4, 2, 16)), // Save bottom weight
    cylinder({r: 2.2, start: [leftX + 4.7, -9.7, 0.8], end: [leftX + 4.8, -9.7, 3]}), // molex recess
    cylinder({r: 2.2, start: [leftX + 4.7, -19, 0.8], end: [leftX + 4.8, -19, 3]}), // molex recess
    cylinder({r: 2.2, start: [rightX - 4.7, -19.3, 0.8], end: [rightX - 4.8, -19.3, 3]}), // molex recess
    screws()
  )
}

function connectors() {
  const hole = union(
    cube({size: [4, 10.4, 13.5], radius: .6, fn: 10 * qty}),
    translate([0, 2.3, 0], cube({size: [4, 5.6, 16], radius: .6, fn: 10 * qty}))
  )
  return union(
    translate([rightX - 3, -24.2, -1], hole), // motor hole
    translate([leftX - 1, -23.9, -1], hole), // motor hole
    translate([leftX - 1, -14.3, -3.5], hole), // power hole
    translate([leftX - 1, -17, -3.5], cube({size: [4, 6, 16]})), // bump remove
    translate([leftX - 1, 9, sizeZ - 6.6], cube({size: [4, 8, 3], radius: .6, fn: 10 * qty})), // USB hole
    translate([leftX - .1, 7.6, sizeZ - 8.5], cube({size: [1, 10.8, 7], radius: [0, 1, 1], fn: 10 * qty})), // USB reccess
    translate([11, topY - 3, sizeZ - 8], cube({size: [3.4, 4, 2.6], radius: .2, fn: 10 * qty})) // antenna hole
  )
}

function box(delta, z1, z2) {
  const r = 3.4 - delta // corner radius
  return translate(
    [leftX + delta, bottomY + delta, z1],
    roundedRect(sizeX - 2 * delta, sizeY - 2 * delta, z2 - z1, r, 16))
}

function screws() {
  const screw = cylinder({r: 1.6, start: [0, 0, -1], end: [0, 0, 6], fn: 30 * qty})
  const margin = 5.8
  const z = -2
  return union(
    translate([0, topY - 10, z], screw),
    translate([leftX + margin, bottomY + margin, z], screw),
    translate([rightX - margin, bottomY + margin, z], screw)
  )
}

// Common
function roundedRect(x, y, z, r, q) {
  return intersection(
    translate([0, 0, -2 * r], cube({size: [x, y, z + 4 * r], radius: r, fn: qty * q})),
    cube({size: [x, y, z]})
  )
}

function topscrews() {
  const trihole = rotate([90, 0, 0], difference(
    linear_extrude({height: 10}, polygon([[0, 0], [7, 0], [0, 19]])),
    translate([0, 2, 2], cube({size: [10, 20, 6]})),
    cylinder({r: 1.6, start: [3, 0, 5], end: [3, 4, 5], fn: 20 * qty}),
    cylinder({r1: 0, r2: 2.95, start: [3, -1.4, 5], end: [3, 2, 5], fn: 20 * qty})
  ))
  return union(
    translate([rightX, 7, 0], trihole),
    translate([leftX, -3, 0], rotate([0, 0, 180], trihole))
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
