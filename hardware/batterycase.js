// title      : ParaDrone battery case
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Battery,motor,drone,paraglider
// file       : battery.jscad

const qty = 1

const battery = {len: 115, width: 31.5, height: 17.4}

const thic = 2 // wall thickness
const h = battery.len / 2 + thic // height (1/2 battery length)
const xSize = battery.width
const ySize = battery.height
const halfX = xSize / 2
const r = 8 // radius

function main() {
  return difference(
    union(
      translate([-halfX - 11, 0, 0], cube({size: [xSize + 22, thic, h]})), // mount1
      translate([-halfX - thic, -10, 0], roundedRect(xSize + 2 * thic, ySize + thic + 10, h, r + thic, 30)) // outer
    ),
    translate([-halfX, -10, thic], roundedRect(xSize, ySize + 10, h, r, 30)), // inner
    translate([-100, -400, -1], cube(400)), // bottom half
    translate([-1.5, -1, -1], roundedRect(3, 5, 10, 1, 15)), // wire hole
    screws()
  )
}

function screws() {
  const screw = cylinder({r: 1.6, start: [0, 0, 0], end: [0, 4, 0], fn: 25 * qty})
  const x = 6.5
  return union(
    translate([-halfX - x, -2, h / 4], screw),
    translate([halfX + x, -2, h / 4], screw),
    translate([-halfX - x, -2, h * 3 / 4], screw),
    translate([halfX + x, -2, h * 3 / 4], screw)
  )
}

function roundedRect(x, y, z, r, q) {
  return intersection(
    translate([0, 0, -2 * r], cube({size: [x, y, z + 4 * r], radius: r, fn: qty * q})),
    cube({size: [x, y, z]})
  )
}
