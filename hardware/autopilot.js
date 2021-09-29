// title      : ParaDrone Autopilot Case
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Microcontroller,case,arduino,autopilot,drone,paraglider
// file       : autopilot.jscad

const jscad = require('@jscad/modeling')
const { intersect, subtract, union } = jscad.booleans
const { colorize, cssColors } = jscad.colors
const { extrudeLinear } = jscad.extrusions
const { cuboid, cylinder, cylinderElliptic, polygon, roundedCuboid, roundedRectangle } = jscad.primitives
const { rotate, rotateX, rotateY, translate } = jscad.transforms

const qty = 1

const topY = 28.9
const bottomY = -31.8

const sizeX = 79.5
const sizeY = topY - bottomY
const sizeZ = 20.0

const leftX = -sizeX / 2
const rightX = sizeX / 2

const sh = 2 // shell thicness

const colors = {
  bn220: cssColors.blue,
  driver: cssColors.green,
  topcase: cssColors.magenta,
  bottomcase: cssColors.purple,
  bec: cssColors.yellow
}

function main() {
  return [
    // mechanism(),
    colorize(colors.topcase, topcase()),
    // colorize(colors.bottomcase, bottomcase())
  ]
}

function topcase() {
  return [
    subtract(
      // gentle bevel
      intersect(
        roundedCuboid({center: [0, bottomY + sizeY / 2, sizeZ / 2 - 4.2], size: [sizeX, sizeY, sizeZ + 10], roundRadius: 3, segments: 22 * qty}),
        cuboid({center: [0, bottomY + sizeY / 2, sizeZ / 2], size: [sizeX, sizeY, sizeZ]})
      ),
      box(sh, -1, sizeZ - sh), // inner
      connectors(),
      esp32()
    ),
    topscrews()
  ]
}

function bottomcase() {
  const support = cylinder({radius: 3.2, center: [0, 0, 1.8], height: 1.6, segments: 30 * qty})
  return subtract(
    union(
      box(sh + 0.2, 0, sh),
      cuboid({center: [leftX + 1.25, bottomY + 16, sh / 2], size: [2.5, 24, sh]}), // left connector base
      cuboid({center: [rightX - 1.25, bottomY + 15, sh / 2], size: [2.5, 22, sh]}), // right connector base
      translate([0, topY - 10, 0], support),
      translate([rightX - 16, bottomY + 10, 0], support)
    ),
    translate([-25, bottomY + 14, -1], roundedRect(50, 30, 4, 2, 16 * qty)), // Save bottom weight
    cylinder({radius: 2.2, center: [leftX + 4.8, bottomY + 21.1, 2], height: 2.4}), // molex recess
    cylinder({radius: 2.2, center: [leftX + 4.8, bottomY + 11.5, 2], height: 2.4}), // molex recess
    cylinder({radius: 2.2, center: [rightX - 4.8, bottomY + 11.5, 2], height: 2.4}), // molex recess
    screws()
  )
}

function connectors() {
  const smallhole = rotateY(-Math.PI / 2, roundedRect(16, 6.6, 4, 0.6, 10 * qty))
  const bighole = rotateY(-Math.PI / 2, roundedRect(13.5, 14.4, 4, 0.6, 10 * qty))
  return [
    roundedCuboid({center: [12.7, topY - 1, sizeZ - 6.7], size: [3.4, 4, 2.6], roundRadius: .2, segments: 10 * qty}), // antenna hole
    cuboid({center: [0, bottomY + 4, 1.5], size: [sizeX, 4, 3]}), // pcb clearance
    // right side
    translate([rightX + 1, bottomY + 3, -1], bighole), // motor hole
    translate([rightX + 1, bottomY + 6.9, -1], smallhole), // motor hole
    translate([rightX + 1, bottomY + 12.3, -5.5], bighole), // switch hole
    // left side
    translate([leftX + 3, bottomY + 6.9, -1], smallhole), // motor latch
    translate([leftX + 3, bottomY + 3, -1], bighole), // motor hole
    translate([leftX + 3, bottomY + 10.5, -1], bighole), // power latch
    translate([leftX + 3, bottomY + 14.4, -3.5], bighole), // power hole
    // USB
    roundedCuboid({center: [leftX + 1, bottomY + 44.8, sizeZ - 5.1], size: [4, 8, 3], roundRadius: .6, segments: 10 * qty}), // USB hole
    // roundedCuboid({center: [leftX + .4, bottomY + 44.9, sizeZ - 5], size: [1, 10.8, 7], roundRadius: .4, segments: 10 * qty}), // USB recess // TODO
  ]
}

function box(delta, z1, z2) {
  const r = 3.4 - delta // corner radius
  return translate(
    [leftX + delta, bottomY + delta, z1],
    roundedRect(sizeX - 2 * delta, sizeY - 2 * delta, z2 - z1, r, 16 * qty))
}

function screws() {
  const screw = cylinder({radius: 1.6, center: [0, 0, 2.5], height: 7, segments: 30 * qty})
  const z = -2
  return [
    translate([0, topY - 10, z], screw),
    translate([rightX - 16, bottomY + 10, z], screw)
  ]
}

function topscrews() {
  const trihole = subtract(
    rotateX(Math.PI / 2, extrudeLinear({height: 10}, polygon({points: [[0, 0], [7, 0], [0, 19]]}))),
    cuboid({center: [5, -5, 12], size: [10, 6, 20]}),
    cylinder({center: [3, -5, 1.5], radius: 1.6, height: 3, segments: 30 * qty}),
    cylinderElliptic({center: [3, -5, 0.5], startRadius: [0.01, 0.01], endRadius: [3, 3], height: 3.5, segments: 30 * qty})
  )
  return [
    translate([rightX, bottomY + 38.8, 0], trihole),
    translate([leftX, bottomY + 28.8, 0], rotate([0, 0, Math.PI], trihole)),
  ]
}

// Mechanism
function mechanism() {
  return [
    esp32(),
    bn220(),
    driver(),
    bec()
  ]
}

function esp32() {
  const h = 25.4
  const circuitboard = extrudeLinear({height: 2.5}, polygon({points: [
    [0, 8.2], [-1, 7.2], [-1, 1.8], [1, 0], [48, 0], [48, 4], [52.3, 8], [52.3, h - 8], [48, h - 4], [48, h], [1, h], [-1, h - 1.8], [-1, h - 7.2], [0, h - 8.2]
  ]}))
  return translate([leftX + 1.8, bottomY + 31.8, sizeZ - 16.4], union(
    colorize(cssColors.white, translate([0, 0.3, 9], circuitboard)),
    colorize([0, 0, 0, 0.4], cuboid({center: [31.5, 13, 13.9], size: [35, 20.4, 3.8]})), // recess
    colorize([0, 0, 0, 0.4], cuboid({center: [31.5, 2, 13.9], size: [14, 2, 3.8]})), // ribbon
    colorize(cssColors.black, cuboid({center: [31.75, 14.5, 14.4], size: [26.1, 13, 4.8]})), // screen
    colorize(cssColors.brown, cylinder({radius: 3.2, center: [11, 19.5, 12.9], height: 5.8, segments: 25 * qty}))
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
 * @param roundRadius corner radius
 * @param segments number of corner segments
 */
function roundedRect(x, y, z, roundRadius, segments) {
  return extrudeLinear({height: z},
    roundedRectangle({center: [x / 2, y / 2], size: [x, y], roundRadius, segments})
  )
}

module.exports = { main }
