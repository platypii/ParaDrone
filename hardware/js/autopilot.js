// title      : ParaDrone Autopilot Case
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Microcontroller,case,arduino,autopilot,drone,paraglider
// file       : autopilot.jscad

const { intersect, subtract, union } = require('@jscad/modeling').booleans
const { colorize, cssColors } = require('@jscad/modeling').colors
const { extrudeLinear } = require('@jscad/modeling').extrusions
const { rotate, translate } = require('@jscad/modeling').transforms
const { cuboid, polygon, roundedCuboid, roundedRectangle } = require("@jscad/modeling").primitives
const { cube, cylinderV1 } = require('./v1')

const qty = 1

const topY = 28.9
const bottomY = -31.8

const sizeX = 79.5
const sizeY = topY - bottomY
const sizeZ = 20.0

const leftX = -sizeX / 2
const rightX = sizeX / 2

const sh = 2 // shell width

const colors = {
  esp32: cssColors.white,
  bn220: cssColors.blue,
  driver: cssColors.green,
  topcase: cssColors.magenta,
  bottomcase: cssColors.purple,
  bec: cssColors.yellow
}

function main() {
  return union(
    // mechanism(),
    colorize(colors.topcase, topcase()),
    // colorize(colors.bottomcase, bottomcase())
  )
}

function topcase() {
  return union(
    subtract(
      // gentle bevel
      intersect(
        translate([leftX, bottomY, -9.2], cube({size: [sizeX, sizeY, sizeZ + 10], radius: 3, segments: 22 * qty})),
        translate([leftX, bottomY, 0], cube({size: [sizeX, sizeY, sizeZ]}))
      ),
      box(sh, -1, sizeZ - sh), // inner
      connectors(),
      esp32()
    ),
    topscrews()
  )
}

function bottomcase() {
  const support = cylinderV1({radius: 3.2, start: [0, 0, 1], end: [0, 0, 2.6], segments: 30 * qty})
  return subtract(
    union(
      box(sh + 0.2, 0, sh),
      cuboid({center: [leftX + 1.25, bottomY + 16, sh / 2], size: [2.5, 24, sh]}), // left connector base
      cuboid({center: [rightX - 1.25, bottomY + 15, sh / 2], size: [2.5, 22, sh]}), // right connector base
      translate([0, topY - 10, 0], support),
      translate([rightX - 16, bottomY + 10, 0], support)
    ),
    translate([-25, bottomY + 14, -1], roundedRect(50, 30, 4, 2, 16)), // Save bottom weight
    cylinderV1({radius: 2.2, start: [leftX + 4.8, bottomY + 21.1, 0.8], end: [leftX + 4.8, bottomY + 21.1, 3]}), // molex recess
    cylinderV1({radius: 2.2, start: [leftX + 4.8, bottomY + 11.5, 0.8], end: [leftX + 4.8, bottomY + 11.5, 3]}), // molex recess
    cylinderV1({radius: 2.2, start: [rightX - 4.8, bottomY + 11.5, 0.8], end: [rightX - 4.8, bottomY + 11.5, 3]}), // molex recess
    screws()
  )
}

function connectors() {
  const hole = union(
    cube({size: [6, 14.4, 13.5], radius: .6, segments: 10 * qty}),
    translate([0, 3.8, 0], cube({size: [6, 6.6, 16], radius: .6, segments: 10 * qty}))
  )
  return union(
    translate([leftX, bottomY + 2 -29.8, 0], cube({size: [sizeX, 4, 3]})), // pcb clearance
    translate([rightX - 3, bottomY + 14.8, -1], cube({size: [4, 12, 9], radius: .6})), // switch hole
    translate([rightX - 4, bottomY + 3, -1], hole), // motor hole
    translate([leftX - 2, bottomY + 3, -1], hole), // motor hole
    translate([leftX - 1, bottomY + 18.4, -3.5], cube({size: [4, 10.4, 13.5], radius: .6, segments: 10 * qty})), // power hole
    translate([leftX - 1, bottomY + 12.8, -3.5], cube({size: [4, 12, 16], radius: .6, segments: 10 * qty})), // bump remove
    translate([leftX - 1, bottomY + 40.8, sizeZ - 6.6], cube({size: [4, 8, 3], radius: .6, segments: 10 * qty})), // USB hole
    // translate([leftX - .1, bottomY + 39.4, sizeZ - 8.5], cube({size: [1, 10.8, 7], radius: [0, 1, 1], segments: 10 * qty})), // USB reccess // WTF
    translate([11, topY - 3, sizeZ - 8], cube({size: [3.4, 4, 2.6], radius: .2, segments: 10 * qty})) // antenna hole
  )
}

function box(delta, z1, z2) {
  const r = 3.4 - delta // corner radius
  return translate(
    [leftX + delta, bottomY + delta, z1],
    roundedRect(sizeX - 2 * delta, sizeY - 2 * delta, z2 - z1, r, 16))
}

function screws() {
  const screw = cylinderV1({radius: 1.6, start: [0, 0, -1], end: [0, 0, 6], segments: 30 * qty})
  const z = -2
  return union(
    translate([0, topY - 10, z], screw),
    translate([rightX - 16, bottomY + 10, z], screw)
  )
}

function topscrews() {
  const trihole = rotate([Math.PI / 2, 0, 0], subtract(
    extrudeLinear({height: 10}, polygon({points: [[0, 0], [7, 0], [0, 19]]})),
    cuboid({center: [5, 12, 5], size: [10, 20, 6]}),
    cylinderV1({radius: 1.6, start: [3, 0, 5], end: [3, 4, 5], segments: 20 * qty}),
    cylinderV1({r1: 0, r2: 2.95, start: [3, -1.4, 5], end: [3, 2, 5], segments: 20 * qty})
  ))
  return union(
    translate([rightX, bottomY + 38.8, 0], trihole),
    translate([leftX, bottomY + 28.8, 0], rotate([0, 0, Math.PI], trihole))
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
  const circuitboard = extrudeLinear({height: 2.5}, polygon({points: [
    [0, 8.2], [-1, 7.2], [-1, 1.8], [1, 0], [48, 0], [48, 4], [52.3, 8], [52.3, h - 8], [48, h - 4], [48, h], [1, h], [-1, h - 1.8], [-1, h - 7.2], [0, h - 8.2]
  ]}))
  return translate([leftX + 1.8, bottomY + 31.8, sizeZ - 16.4], union(
    colorize(colors.esp32, translate([0, 0.3, 9], circuitboard)),
    colorize([0, 0, 0, 0.4], translate([13.9, 2.8, 12], cube({size: [35, 20.4, 3.8]}))),
    colorize([0, 0, 0, 0.4], translate([24.5, 1, 12], cube({size: [14, 2, 3.8]}))),
    colorize(cssColors.black, translate([18.7, 8, 12], cube({size: [26.1, 13, 4.8]}))),
    colorize(cssColors.brown, cylinderV1({radius: 3.2, start: [11, 19.5, 10], end: [11, 19.5, 15.8], segments: 25 * qty}))
  ))
}

function bn220() {
  return colorize(colors.bn220, cuboid({center: [rightX - 12.1, bottomY + 46.3, sizeZ - 6.1], size: [20, 22, 4.2]}))
}

function driver() {
  // Pololu Dual MC33926 Motor Driver Carrier
  return colorize(colors.driver, cuboid({center: [0, bottomY + 16, sizeZ - 5], size: [46, 28, 6]}))
}

function bec() {
  // Pololu 3.3V, 500mA Step-Down Voltage Regulator D24V5F3
  return colorize(colors.bec, cuboid({center: [rightX - 9, bottomY + 23, sizeZ - 8], size: [12.8, 10.2, 3]}))
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
