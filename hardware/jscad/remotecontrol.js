// title      : ParaDrone Bluetooth/LoRa Relay Case
// author     : BASEline
// tags       : Radio,relay,remote control,drone,paraglider

const jscad = require("@jscad/modeling")
const { subtract, union } = jscad.booleans
const { colorize, cssColors } = jscad.colors
const { extrudeLinear } = jscad.extrusions
const { cuboid, cylinder, polygon, roundedCuboid, roundedRectangle } = jscad.primitives
const { mirrorX, rotate, rotateX, rotateY, translate } = jscad.transforms

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

const sh = 2 // shell thicness

const colors = {
  topcase: cssColors.magenta,
  bottomcase: cssColors.purple
}

function getParameterDefinitions() {
  return [{name: "print", type: "checkbox", checked: false, caption: "Print mode"}]
}

function main(params) {
  if (params.print) {
    return [
      translate([0, -5, sizeZ], rotateX(Math.PI, topcase())),
      bottomcase()
    ]
  } else {
    return [
      mechanism(),
      colorize(colors.topcase, topcase()),
      colorize(colors.bottomcase, bottomcase())
    ]
  }
}

function topcase() {
  return [
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
  ]
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
    antenna(),
    cuboid({center: [rightX + 8, topY - 6, z - 2], size: [2, 6.4, 4]}), // antenna top square
    cuboid({center: [rightX + 2, topY - 6, sizeZ / 2], size: [11, 8, sizeZ - 3]}) // antenna channel square
  )
}

function antenna() {
  const z = sizeZ - 5.5
  return translate([rightX + 5.4, topY - 6, z], rotateY(Math.PI / 2, union(
    cylinder({radius: 3.2, height: 12, segments: 25 * qty}), // antenna hole
    extrudeLinear({height: 2.2},
      polygon({points: [[4.6, 0], [2.3, 4], [-2.3, 4], [-4.6, 0], [-2.3, -4], [2.3, -4]]})
    )
  )))
}

function shell(inset, bottom, top) {
  return translate([leftX + inset, inset, bottom],
    roundedRect(sizeX - 2 * inset, sizeY - 2 * inset, top - bottom, 3 - inset, 15 * qty)
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
      roundedRect(15 - 2 * inset, 12 - 2 * inset, top - bottom, 3 - inset, 15 * qty)
    ),
    shell(0, 0, sizeZ)
  )
}

function latches() {
  const latch1 = translate([18, topY - 3, splitZ + 6], latch(Math.PI / 2))
  const latch2 = translate([-18, bottomY + 3, splitZ + 6], latch(-Math.PI / 2))
  return [
    latch1,
    latch2,
    mirrorX(latch1),
    mirrorX(latch2)
  ]
}

function latchholes() {
  const z = splitZ - 2.8
  return [
    cuboid({center: [-14, topY - 2, z], size: [10, 1, 1.4]}),
    cuboid({center: [14, topY - 2, z], size: [10, 1, 1.4]}),
    cuboid({center: [-14, bottomY + 2, z], size: [10, 1, 1.4]}),
    cuboid({center: [14, bottomY + 2, z], size: [10, 1, 1.4]})
  ]
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
  return [
    esp32(),
    colorize(cssColors.gold, antenna())
  ]
}

function esp32() {
  const h = 25.4
  const circuitboard = extrudeLinear({height: 2.5}, polygon({points: [
    [0, 8.2], [-1, 7.2], [-1, 1.8], [1, 0], [48, 0], [48, 4], [52.3, 8], [52.3, h - 8], [48, h - 4], [48, h], [1, h], [-1, h - 1.8], [-1, h - 7.2], [0, h - 8.2]
  ]}))
  return translate([leftX + 1.2, 3.5, splitZ - 8], [
    colorize(cssColors.white, translate([0, 0, 9], circuitboard)),
    colorize([0, 0, 0, 0.4], cuboid({center: [31.5, 13, 13.9], size: [35, 20.5, 3.8]})), // recess
    colorize([0, 0, 0, 0.4], cuboid({center: [31.5, 2, 13.9], size: [14, 2, 3.8]})), // ribbon
    colorize(cssColors.black, cuboid({center: [31.9, 14.5, 14.2], size: [25.8, 13, 4.4]})), // screen
    colorize(cssColors.brown, cylinder({radius: 3.2, center: [11, 19.5, 12.9], height: 5.8, segments: 25 * qty}))
  ])
}

/**
 * Generate a rounded rectangle with flat top and bottom.
 * @param x x size
 * @param y y size
 * @param z z size
 * @param roundRadius corner radius
 * @param segments number of corner segments
 */
function roundedRect(x, y, z, roundRadius, segments) {
  return extrudeLinear({height: z},
    roundedRectangle({center: [x / 2, y / 2], size: [x, y], roundRadius, segments})
  )
}

module.exports = { getParameterDefinitions, main }
