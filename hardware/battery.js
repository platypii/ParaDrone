// title      : ParaDrone battery case
// author     : BASEline
// license    : MIT License
// tags       : GPS,data,arduino,flysight
// file       : battery.js

const jscad = require('@jscad/modeling')
const { subtract, union } = jscad.booleans
const { extrudeLinear } = jscad.extrusions
const { cuboid, cylinder, roundedRectangle } = jscad.primitives
const { rotateX, translate } = jscad.transforms

const qty = 1

const battery = {len: 115, width: 31.5, height: 17.4}

const thic = 2 // wall thickness
const h = battery.len / 2 + thic // height (1/2 battery length)
const xSize = battery.width
const ySize = battery.height
const halfX = xSize / 2
const r = 8 // radius

function main() {
  return subtract(
    union(
      cuboid({center: [0, thic / 2, h / 2], size: [xSize + 20, thic, h]}), // mount1
      translate([-halfX - thic, -10, 0], roundedRect(xSize + 2 * thic, ySize + thic + 10, h, r + thic, 30 * qty)) // outer
    ),
    cuboid({center: [100, -200, 200], size: [400, 400, 400]}), // bottom half
    translate([-halfX, -10, thic], roundedRect(xSize, ySize + 10, h, r, 30 * qty)), // inner
    translate([-1.5, -1, -1], roundedRect(3, 5, 10, 1, 15 * qty)), // wire hole
    screws()
  )
}

function screws() {
  const screw = rotateX(Math.PI / 2, cylinder({radius: 1.6, height: 4, segments: 15 * qty}))
  const x = 6.5
  return [
    translate([-halfX - x, 2, h / 4], screw),
    translate([halfX + x, 2, h / 4], screw),
    translate([-halfX - x, 2, h * 3 / 4], screw),
    translate([halfX + x, 2, h * 3 / 4], screw)
  ]
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
