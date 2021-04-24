// title      : ParaDrone battery case
// author     : BASEline
// license    : MIT License
// tags       : GPS,data,arduino,flysight
// file       : battery.js

const { cube, cuboid, cylinder, cylinderElliptic, rectangle, roundedRectangle } = require('@jscad/modeling').primitives
const { subtract, union } = require('@jscad/modeling').booleans
const { rotateX, translate } = require('@jscad/modeling').transforms
const { extrudeLinear } = require('@jscad/modeling').extrusions

const qty = 6

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
      translate([-halfX - 10, 0, 0], cuber(xSize + 20, thic, h, 0)), // mount1
      translate([-halfX - thic, -10, 0], cuber(xSize + 2 * thic, ySize + thic + 10, h, r + thic)) // outer
    ),
    translate([-100, -400, 0], cuber(400, 400, 400)), // bottom half
    translate([-halfX, -10, thic], cuber(xSize, ySize + 10, h, r)), // inner
    translate([-1.5, -1, -1], cuber(3, 5, 10, 1)), // wire hole
    screws()
  )
}

function screws() {
  const screw = rotateX(Math.PI / 2, cylinder({radius: 1.6, height: 4, fn: 25 * qty}))
  const x = 6.5
  return union(
    translate([-halfX - x, 2, h / 4], screw),
    translate([halfX + x, 2, h / 4], screw),
    translate([-halfX - x, 2, h * 3 / 4], screw),
    translate([halfX + x, 2, h * 3 / 4], screw)
  )
}

function cuber(x, y, z, r) {
  if (r) {
    return translate([x / 2, y / 2, 0], extrudeLinear({height: z}, roundedRectangle({size: [x, y], roundRadius: r, segments: 15 * qty})))
  } else {
    return cuboid({size: [x, y, z], center: [x / 2, y / 2, z / 2]})
  }
}

module.exports = { main }
