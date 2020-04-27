// title      : ParaDrone Bluetooth/LoRa Relay Case
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Motor,actuator,linear,drone,paraglider
// file       : relay.jscad

const qty = .7

const topY = 32.5
const bottomY = 0

const sizeX = 55
const sizeY = topY - bottomY

const battThic = 6 // 6 for battery
const sizeZ = 12 + battThic
const splitZ = 4 + battThic

const leftX = -sizeX / 2
const rightX = sizeX / 2

const sh = 2 // shell width

const colors = {
  esp32: "white",
  bn220: "blue",
  r8ef: "black",
  driver: "green",
  topcase: "magenta",
  bottomcase: "purple"
}

function main() {
  return union(
    // mechanism(),
    // color(colors.topcase, topcase())
    color(colors.bottomcase, bottomcase())
  )
}

function topcase() {
  return union(
    difference(
      shell(0, splitZ, sizeZ), // outer
      inner(),
      ahole(),
      translate([leftX - .1, 12.25, sizeZ - 5.5], cube({size: [2.2, 8, 3], radius: [0, .6, .6], fn: 15 * qty})), // USB hole
      mechanism()
    ),
    difference(
      bulge(0, splitZ - 1, sizeZ),
      ahole(),
      antenna()
    ),
    difference(
      topridge(),
      ahole()
    ),
    latches()
  )
}

function bottomcase() {
  const bot = union(
    difference(
      shell(0, 0, splitZ), // outer
      inner(),
      bottomridge(),
      // translate([-22, 8, 0], cube({size: [43, 16, 2]})), // TODO: window
      // translate([-15, 4, 0], cube({size: [30, 22, 2]})), // TODO: window
      latchholes(),
      ahole()
    ),
    difference(
      bulge(0, 0, splitZ - 1),
      ahole()
    )
  )
  if (battThic) {
    return difference(bot, powerhole())
  } else {
    return union(bot, supports())
  }
}
function ahole() {
  const z = sizeZ - 5.5
  return union(
    cylinder({r: 3.2, start: [rightX + 10, topY - 6, z], end: [rightX - 2, topY - 6, z], fn: 25 * qty}), // antenna hole
    translate([rightX + 7, topY - 9.2, z - 4], cube({size: [2, 6.4, 4]})), // antenna top square
    translate([rightX - 3.5, topY - 10, 1.5], cube({size: [11, 8, sizeZ - 3]})) // antenna channel square
  )
}

function antenna() {
  const z = sizeZ - 5.5
  return union(
    cylinder({r: 3.2, start: [rightX + 10, topY - 6, z], end: [rightX - 2, topY - 6, z], fn: 25 * qty}), // antenna hole
    translate([32.9, 26.5, z], rotate([0, 90, 0], linear_extrude({height: 2.2},
      polygon([[4.6,0], [2.3,4], [-2.3,4], [-4.6,0], [-2.3,-4], [2.3,-4]])
    )))
  )
}

function shell(inset, bottom, top) {
  return translate([leftX + inset, inset, bottom], cube({size: [sizeX - 2*inset, sizeY - 2*inset, top - bottom], radius: [3 - inset, 3 - inset, 0], fn: 15 * qty}))
}
function outer() {
  return shell(0, 0, sizeZ)
}
function inner() {
  return shell(sh, 1.5, sizeZ - 1.5)
}
function bottomridge() {
  return difference(
    shell(0, splitZ - 1, splitZ),
    shell(1.2, splitZ - 1, splitZ)
  )
}
function topridge() {
  return difference(
    shell(0, splitZ - 1, splitZ + .1),
    shell(0.8, splitZ - 2, splitZ + 1)
  )
}

function bulge(inset, bottom, top) {
  return difference(
    translate([rightX - 6 + inset, topY - 12 + inset, bottom], cube({size: [15 - 2*inset, 12 - 2*inset, top - bottom], radius: [3 - inset, 3 - inset, 0], fn: 15 * qty})),
    shell(0, 0, sizeZ)
  )
}

// Common
function bottomhalf(z) {
  return translate([leftX, bottomY, 0], cube({size: [sizeX + 10, sizeY, z]}))
}
function tophalf(z) {
  return translate([leftX, bottomY, z], cube({size: [sizeX, sizeY, 200]}))
}

function latches() {
  const latch1 = translate([18, topY - 3, sizeZ - 2], latch(90))
  const latch2 = translate([-18, bottomY + 3, sizeZ - 2], latch(-90))
  return union(
    latch1,
    latch2,
    mirror([1, 0, 0], latch1),
    mirror([1, 0, 0], latch2)
  )
}

function latchholes() {
  const z = sizeZ - 10.5
  return union(
    translate([-19, topY - 2.5, z], cube({size: [10, 1, 1.4]})),
    translate([9, topY - 2.5, z], cube({size: [10, 1, 1.4]})),
    translate([-19, bottomY + 1.5, z], cube({size: [10, 1, 1.4]})),
    translate([9, bottomY + 1.5, z], cube({size: [10, 1, 1.4]}))
  )
}

function latch(angle) {
  return rotate(
    [-90, 0, angle],
    linear_extrude({height: 8}, polygon([[-.5, -1], [-.5, 8.5], [1, 8.2], [1.4, 7.3], [.8, 7], [.8, 6], [1,6], [1, -1]]))
  )
}

function supports() {
  return difference(
    union(
      corner(-19, 4, 0),
      corner(-19, 28, -90),
      corner(17, 4, 90),
      corner(17, 28, 180)
    ),
    tophalf(4.8)
  )
}

function corner(x, y, rot) {
  return translate([x, y, 1.5],
    rotate([0, 0, rot],
      union(
        cube({size: [1, 4, 14]}),
        cube({size: [4, 1, 14]})
      )
    )
  )
}

function powerhole() {
  return union(
    translate([leftX - 1, sizeY / 2 - 3.5, 2.6], cube({size: [4, 7, 4.6]})),
    translate([leftX + 1.8, sizeY / 2 - 6.5, 1.3], cube({size: [6.6, 13, 7]}))
  )
}

// Mechanism
function mechanism() {
  return union(
    esp32(),
    antenna()
  )
}

function esp32() {
  const h = 25.4
  const circuitboard = linear_extrude({height: 2.5}, polygon([
    [0,8.2], [-1,7.2], [-1,1.8], [1,0], [48,0], [48,4], [52.3,8], [52.3,h-8], [48,h-4], [48,h], [1,h], [-1,h-1.8], [-1,h-7.2], [0,h-8.2]
  ]))
  return translate([leftX + 1.1, 3.5, sizeZ - 16], union(
    color(colors.esp32, translate([0, 0, 9], circuitboard)),
    color([0, 0, 0, 0.4], translate([13.9, 2.7, 12], cube({size: [35, 20.5, 3.8]}))),
    color([0, 0, 0, 0.4], translate([24.5, 1, 12], cube({size: [14, 2, 3.8]}))),
    color("black", translate([19, 8, 12], cube({size: [25.8, 13, 4]}))),
    color("brown", cylinder({r: 3.2, start: [11, 19.5, 10], end: [11, 19.5, 15.8], fn: 25 * qty}))
  ))
}
