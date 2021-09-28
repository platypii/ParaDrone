// title      : ParaDrone Bluetooth/LoRa Relay Case
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Radio,relay,remote control,drone,paraglider
// file       : relay.jscad

const { subtract, union } = require('@jscad/modeling').booleans
const { colorize, cssColors } = require('@jscad/modeling').colors
const { extrudeLinear } = require('@jscad/modeling').extrusions
const { mirrorX, rotate, translate } = require('@jscad/modeling').transforms
const { cuboid, polygon, roundedCuboid, roundedRectangle } = require('@jscad/modeling').primitives
const { cube, cylinderV1 } = require('./v1')

const qty = 1

const topY = 32.5
const bottomY = 0

const sizeX = 55
const sizeY = topY - bottomY

const battThic = 6 // 6 for battery
const sizeZ = 12.4 + battThic
const splitZ = 4 + battThic

const leftX = -sizeX / 2
const rightX = sizeX / 2

const sh = 2 // shell width

const colors = {
  esp32: cssColors.white,
  topcase: cssColors.magenta,
  bottomcase: cssColors.purple
}

function main() {
  return union(
    // mechanism(),
    colorize(colors.topcase, topcase())
    // colorize(colors.bottomcase, bottomcase())
  )
}

function topcase() {
  return union(
    subtract(
      shell(0, splitZ, sizeZ), // outer
      inner(),
      ahole(),
      roundedCuboid({center: [leftX + 1, 16.25, splitZ + 4], size: [4, 8, 3], roundRadius: .6, segments: 15 * qty}), // USB hole
      mechanism()
    ),
    subtract(
      bulge(0, splitZ - 1, sizeZ),
      ahole(),
      antenna()
    ),
    subtract(
      topridge(),
      ahole()
    ),
    latches()
  )
}

function bottomcase() {
  const bot = union(
    subtract(
      shell(0, 0, splitZ), // outer
      inner(),
      bottomridge(),
      latchholes(),
      ahole()
    ),
    subtract(
      bulge(0, 0, splitZ - 1),
      ahole()
    )
  )
  return subtract(bot, powerhole())
}
function ahole() {
  const z = sizeZ - 5.5
  return union(
    cylinderV1({radius: 3.2, start: [rightX + 10, topY - 6, z], end: [rightX - 2, topY - 6, z], segments: 25 * qty}), // antenna hole
    translate([rightX + 7, topY - 9.2, z - 4], cube({size: [2, 6.4, 4]})), // antenna top square
    translate([rightX - 3.5, topY - 10, 1.5], cube({size: [11, 8, sizeZ - 3]})) // antenna channel square
  )
}

function antenna() {
  const z = sizeZ - 5.5
  return union(
    cylinderV1({radius: 3.2, start: [rightX + 10, topY - 6, z], end: [rightX - 2, topY - 6, z], segments: 25 * qty}), // antenna hole
    translate([32.9, 26.5, z], rotate([0, Math.PI / 2, 0], extrudeLinear({height: 2.2},
      polygon({points: [[4.6, 0], [2.3, 4], [-2.3, 4], [-4.6, 0], [-2.3, -4], [2.3, -4]]})
    )))
  )
}

function shell(inset, bottom, top) {
  return translate([leftX + inset, inset, bottom],
    roundedRect(sizeX - 2 * inset, sizeY - 2 * inset, top - bottom, 3 - inset, 15)
  )
}
function inner() {
  return shell(sh, 1.5, sizeZ - 1.5)
}
function bottomridge() {
  return subtract(
    shell(0, splitZ - 1, splitZ),
    shell(1.2, splitZ - 1, splitZ)
  )
}
function topridge() {
  return subtract(
    shell(0, splitZ - 1, splitZ + .1),
    shell(0.8, splitZ - 2, splitZ + 1)
  )
}

function bulge(inset, bottom, top) {
  return subtract(
    translate([rightX - 6 + inset, topY - 12 + inset, bottom],
      roundedRect(15 - 2 * inset, 12 - 2 * inset, top - bottom, 3 - inset, 15)
    ),
    shell(0, 0, sizeZ)
  )
}

// Common
function tophalf(z) {
  return cuboid({center: [0, sizeY / 2, z + 100], size: [sizeX, sizeY, 200]})
}

function latches() {
  const latch1 = translate([18, topY - 3, splitZ + 6], latch(Math.PI / 2))
  const latch2 = translate([-18, bottomY + 3, splitZ + 6], latch(-Math.PI / 2))
  return union(
    latch1,
    latch2,
    mirrorX(latch1),
    mirrorX(latch2)
  )
}

function latchholes() {
  const z = splitZ - 3.5
  return union(
    translate([-19, topY - 2.5, z], cube({size: [10, 1, 1.4]})),
    translate([9, topY - 2.5, z], cube({size: [10, 1, 1.4]})),
    translate([-19, bottomY + 1.5, z], cube({size: [10, 1, 1.4]})),
    translate([9, bottomY + 1.5, z], cube({size: [10, 1, 1.4]}))
  )
}

function latch(angle) {
  return rotate(
    [-Math.PI / 2, 0, angle],
    extrudeLinear(
      {height: 8},
      polygon({points: [[1, -1], [1, 6], [.8, 6], [.8, 8], [1.4, 8.3], [1, 9.2], [-.5, 9.5], [-.5, -1]]})
    )
  )
}

function powerhole() {
  return cuboid({center: [leftX + 1, sizeY / 2, 4.9], size: [4, 7, 4.6]})
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
  const circuitboard = extrudeLinear({height: 2.5}, polygon({points: [
    [0, 8.2], [-1, 7.2], [-1, 1.8], [1, 0], [48, 0], [48, 4], [52.3, 8], [52.3, h-8], [48, h-4], [48, h], [1, h], [-1, h - 1.8], [-1, h - 7.2], [0, h - 8.2]
  ]}))
  return translate([leftX + 1.2, 3.5, splitZ - 8], union(
    colorize(colors.esp32, translate([0, 0, 9], circuitboard)),
    colorize([0, 0, 0, 0.4], translate([13.9, 2.7, 12], cube({size: [35, 20.5, 3.8]}))),
    colorize([0, 0, 0, 0.4], translate([24.5, 1, 12], cube({size: [14, 2, 3.8]}))),
    colorize(cssColors.black, translate([19, 8, 12], cube({size: [25.8, 13, 4.4]}))),
    colorize(cssColors.brown, cylinderV1({radius: 3.2, start: [11, 19.5, 10], end: [11, 19.5, 15.8], segments: 25 * qty}))
  ))
}

/**
 * Generate a rounded rectangle with flat top and bottom.
 * @param x x size
 * @param y y size
 * @param z z size
 * @param r corner radius
 * @param q quality setting
 */
function roundedRect(x, y, z, r, q) {
  return extrudeLinear({height: z},
    roundedRectangle({center: [x / 2, y / 2], size: [x, y], roundRadius: r, segments: qty * q})
  )
}

module.exports = { main }
