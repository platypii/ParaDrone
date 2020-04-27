// title      : ParaDrone Autopilot Case
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Motor,actuator,linear,drone,paraglider
// file       : autopilot.jscad

const qty = 1

const topY = 28.4
const bottomY = -31

const sizeX = 76.5
const sizeY = topY - bottomY
const sizeZ = 15

const leftX = -sizeX / 2
const rightX = sizeX / 2

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
    // color(colors.topcase, topcase()),
    color(colors.bottomcase, bottomcase())
  )
}

function both() {
  const shoulder = -10
  const sh = 1.5 // shell width
  const outer = linear_extrude({height: sizeZ}, polygon([
    [leftX, topY], [rightX, topY], [rightX, shoulder], [24, bottomY], [-24, bottomY], [leftX, shoulder]
  ]))
  const shell = difference(
    outer,
    translate([leftX - .1, 9, 9.2], cube({size: [2.2, 8, 3], radius: [0, .6, .6], fn: 15 * qty})), // USB hole
    translate([0,0,sh], scale([(sizeX - 2*sh) / sizeX, (sizeY - 2*sh) / sizeY, (sizeZ - 2*sh) / sizeZ], outer)), // hollow shell
    translate([11, topY - 2, 8.5], cube({size: [4, 3, 4], round: true, radius: [.2, 0, .2], fn: 15 * qty})) // antenna hole
  )
  return union(
    shell,
    corners()
  )
}

function topcase() {
  return union(
    difference(
      both(),
      bottomhalf(6),
      translate([-11, bottomY - 0.1, sizeZ - 10], cube({size: [22, 2, 3.5], round: true, radius: [.5, 0, .5], fn: 15 * qty})), // wire hole
      mechanism()
    ),
    latches(latch)
  )
}

function bottomcase() {
  return union(
    difference(
      both(),
      tophalf(6),
      translate([-19, -20, 0], cube({size: [22, 40, 1.5]})), // Save bottom weight
      latches(latchhole),
      boardholes()
    ),
    supports()
  )
}

function boardholes() {
  const mountingScrew = rotate([0, -90, 0], screw())
  const margin = 5
  return union(
    translate([leftX + margin, topY - margin, -2], mountingScrew),
    translate([rightX - margin, topY - margin, -2], mountingScrew),
    translate([leftX + margin, -8, -2], mountingScrew),
    translate([rightX - margin, -8, -2], mountingScrew),
    translate([-21, bottomY + margin, -2], mountingScrew),
    translate([21, bottomY + margin, -2], mountingScrew)
  )
}

function supports() {
  return difference(
    union(
      translate([24, 9, 1.5], cube({size: [1, 9, 20]})), // gps1
      translate([20, 13, 1.5], cube({size: [9, 1, 20]})), // gps2
    //   translate([-15.6, 0, 1.5], cube({size: [1.5, 4, 20]})), // esp1
    //   corner(-37.9, 22.5, -90), // esp2
      corner(7.6, 2.5, 90), // esp3
      corner(7.6, 22.5, 180) // esp4
    ),
    tophalf(7.5)
  )
}

// Common
function screw() {
  return union(
    cylinder({r: 1.6, start: [0, 0, 0], end: [4, 0, 0], fn: 30 * qty}),
    cylinder({r1: 0, r2: 2.7, start: [1.5, 0, 0], end: [4, 0, 0], fn: 30 * qty})
  )
}

function bottomhalf(z) {
  return translate([leftX, bottomY, 0], cube({size: [sizeX, sizeY, z]}))
}
function tophalf(z) {
  return translate([leftX, bottomY, z], cube({size: [sizeX, sizeY, 200]}))
}

function corners() {
  return union(
    corner(leftX + 50, -1, 90), // esp1
    // corner(leftX + 21.7, -1, 0), // esp2
    corner(rightX - 22.9, 2.4, 0), // gps1
    corner(24, 0, 180), // driver1
    corner(-24, 0, -90), // driver2
    corner(leftX + 4, 0, -90), // bec1
    corner(leftX + 15, -14.2, 90) // bec2
  )
}
function corner(x, y, rot) {
  return translate([x, y, 1.5],
    rotate([0, 0, rot],
      union(
        cube({size: [1, 4, 13]}),
        cube({size: [4, 1, 13]})
      )
    )
  )
}

function latches(gen) {
  const latch1 = translate([rightX - 2.5, -8.4, sizeZ - 1], gen(0))
  const latch2 = translate([24, -26.3, sizeZ - 1], gen(-34.6))
  const latch3 = translate([leftX + 16, topY - 2.4, sizeZ - 1], gen(90))
  const latch4 = translate([5, topY - 2.4, sizeZ - 1], gen(90))
  return union(
    latch1,
    latch2,
    latch3,
    latch4,
    mirror([1, 0, 0], latch1),
    mirror([1, 0, 0], latch2),
    mirror([1, 0, 0], latch3)
  )
}
function latch(angle) {
  return rotate(
    [-90, 0, angle],
    linear_extrude({height: 8}, polygon([[-.5, -1], [-.5, 10.5], [1, 10.2], [1.4, 9.3], [.8, 9], [.8, 8], [1, 8], [1, -1]]))
  )
}
function latchhole(angle) {
  return rotate(
    [-90, 0, angle],
    translate([-.4, 9.2, -1], cube({size: [2, 1.4, 10]}))
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
  return translate([leftX + 1.2, 0, -1], union(
    color(colors.esp32, translate([0, 0, 9], circuitboard)),
    color([0, 0, 0, 0.4], translate([13.9, 2.8, 12], cube({size: [35, 20.4, 3.8]}))),
    color([0, 0, 0, 0.4], translate([24.5, 1, 12], cube({size: [14, 2, 3.8]}))),
    color("black", translate([18.7, 8, 12], cube({size: [26.1, 13, 4]}))),
    color("brown", cylinder({r: 3.2, start: [11, 19.5, 10], end: [11, 19.5, 15.8], fn: 25 * qty}))
  ))
}

function bn220() {
  return translate([rightX - 21.8, 3.5, sizeZ - 7.5], union(
    color(colors.bn220, translate([0, 0, 0], cube({size: [20, 22, 4.2]}))),
    color("brown", translate([1, 2, 4.4], cube({size: [18, 18, 2], round: true, radius: [1, 1, 0], fn: 15 * qty})))
  ))
}

function driver() {
  return color(colors.driver, translate([-23, -29, sizeZ - 7.1], cube({size: [46, 28, 6]})))
}

function bec() {
  return color(colors.bec, translate([leftX + 5, -13, sizeZ - 6], cube({size: [9, 12, 3]})))
}
