// title      : ParaDrone Autopilot Case
// author     : BASEline
// tags       : Microcontroller,case,arduino,autopilot,drone,paraglider

const jscad = require("@jscad/modeling")
const { intersect, subtract } = jscad.booleans
const { colorize, cssColors } = jscad.colors
const { extrudeLinear } = jscad.extrusions
const { cuboid, cylinder, cylinderElliptic, polygon, roundedCuboid, roundedRectangle } = jscad.primitives
const { rotate, rotateX, rotateY, translate } = jscad.transforms

let qty = 1

const topY = 28.9
const bottomY = -31.8

const sizeX = 79.5
const sizeY = topY - bottomY
const sizeZ = 20.0

const leftX = -sizeX / 2
const rightX = sizeX / 2

const sh = 2 // shell thicness

function getParameterDefinitions() {
  return [{name: "print", type: "checkbox", checked: false, caption: "Print mode"}]
}

function main(params) {
  if (params.print) {
    qty = 3
    return translate([0, 0, sizeZ], rotateY(Math.PI, topcase()))
  } else {
    return [
      colorize(cssColors.purple, topcase()),
      circuit()
    ]
  }
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

function topscrews() {
  const trihole = subtract(
    rotateX(Math.PI / 2, extrudeLinear({height: 10}, polygon({points: [[0, 0], [7, 0], [0, 19]]}))),
    cuboid({center: [5, -5, 12], size: [10, 6, 20]}),
    cylinder({center: [3, -5, 1.5], radius: 1.6, height: 3, segments: 20 * qty}),
    cylinderElliptic({center: [3, -5, 0.5], startRadius: [0, 0], endRadius: [3, 3], height: 3.5, segments: 20 * qty})
  )
  return [
    translate([rightX, bottomY + 38.8, 0], trihole),
    translate([leftX, bottomY + 28.8, 0], rotate([0, 0, Math.PI], trihole)),
  ]
}

function circuit() {
  return [
    pcb(),
    esp32(),
    gps(),
    driver(),
    bec(),
    plugs()
  ]
}

function esp32() {
  const h = 25.4
  const circuitboard = extrudeLinear({height: 2.5}, polygon({points: [
    [0, 8.2], [-1, 7.2], [-1, 1.8], [1, 0], [48, 0], [48, 4], [52.3, 8], [52.3, h - 8], [48, h - 4], [48, h], [1, h], [-1, h - 1.8], [-1, h - 7.2], [0, h - 8.2]
  ]}))
  return translate([leftX + 1.8, bottomY + 31.8, sizeZ - 16.4], [
    colorize(cssColors.white, translate([0, 0.3, 9], circuitboard)),
    colorize([0, 0, 0, 0.4], cuboid({center: [31.5, 13, 13.9], size: [35, 20.4, 3.8]})), // recess
    colorize([0, 0, 0, 0.4], cuboid({center: [31.5, 2, 13.9], size: [14, 2, 3.8]})), // ribbon
    colorize([0.1, 0.1, 0.1, 0.99], cuboid({center: [31.75, 14.5, 14.4], size: [26.1, 13, 4.8]})), // screen
    colorize(cssColors.brown, cylinder({radius: 3.2, center: [11, 19.5, 12.9], height: 5.8, segments: 25 * qty}))
  ])
}

function gps() {
  // u-blox SAM-M8Q
  return colorize(cssColors.tan, cuboid({center: [rightX - 12.1, topY - 12, 6], size: [16, 16, 4.2]}))
}

function driver() {
  // Pololu Dual MC33926 Motor Driver Carrier
  return colorize(cssColors.darkgreen, cuboid({center: [0, bottomY + 16, 8], size: [46, 28, 6]}))
}

function bec() {
  // Pololu 3.3V, 500mA Step-Down Voltage Regulator D24V5F3
  return colorize(cssColors.darkgreen, cuboid({center: [rightX - 9, bottomY + 23, sizeZ - 8], size: [12.8, 10.2, 3]}))
}

function pcb() {
  return colorize(cssColors.green, box(sh + 0.2, 0, sh))
}

function plugs() {
  return colorize([0.1, 0.1, 0.1, 1], [
    cuboid({center: [rightX - 5, bottomY + 10, 5.5], size: [9.5, 13, 6]}),
    cuboid({center: [leftX + 5, bottomY + 10, 5.5], size: [9.5, 13, 6]}),
    cuboid({center: [leftX + 5, bottomY + 22.5, 5.5], size: [9.5, 10, 6]})
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
