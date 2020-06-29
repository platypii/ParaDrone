// title      : ParaDrone battery case
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : GPS,data,arduino,flysight
// file       : battery.jscad

const qty = 1

const battery = {len: 140, width: 37, height: 23}

const thic = 2 // wall thickness
const h = battery.len / 2 + thic // height (1/2 battery length)
const width = battery.height
const len = battery.width
const half_len = len / 2
const r = 8 // radius

function main() {
  return difference(
    union(
      translate([-half_len - 10, 0, 0], cube({size: [len + 20, thic, h]})), // mount1
      translate([-half_len - thic, -10, 0], cuber(len + 2 * thic, width + thic + 10, h, r + thic)) // outer
    ),
    translate([-half_len, -10, thic], cuber(len, width + 10, h, r)), // inner
    translate([-100, -400, -1], cube(400)), // bottom half
    translate([-half_len - 6, -2, h / 4], screw()),
    translate([half_len + 6, -2, h / 4], screw()),
    translate([-half_len - 6, -2, h * 3 / 4], screw()),
    translate([half_len + 6, -2, h * 3 / 4], screw())
  )
}

function screw(shaftRadius) {
  const r = shaftRadius || 1.6
  return union(
    cylinder({r: r, start: [0, 0, 0], end: [0, 4, 0], fn: 30 * qty}),
    cylinder({r1: 0, r2: 2.7, start: [0, 1.5, 0], end: [0, 4, 0], fn: 30 * qty})
  )
}

function cuber(x, y, z, r) {
  if (r) {
    return cube({size: [x, y, z], radius: [r, r, 0], fn: 30 * qty})
    // return linear_extrude({height: z}, square({size: [x, y], radius: r, segments: 15 * qty}))
  } else {
    return cube({size: [x, y, z], center: [x / 2, y / 2, z / 2]})
  }
}
