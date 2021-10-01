// title      : ParaDrone
// author     : BASEline
// license    : MIT License
// tags       : Microcontroller,arduino,autopilot,drone,paraglider
// file       : all.jscad

const jscad = require('@jscad/modeling')
const { subtract } = jscad.booleans
const { colorize, cssColors } = jscad.colors
const { cylinder, roundedCuboid } = jscad.primitives
const { rotate, rotateZ, translate } = jscad.transforms
const actuator = require("./actuator").main(true)
const autopilot = require("./autopilot").main(true)
const battery = require("./battery").main(true)

const boardX = 380
const boardY = 80
const boardZ = 10

function main() {
  return [
    translate([20 - boardX / 2, 0, 0], rotateZ(Math.PI, actuator)), // left
    translate([boardX / 2 - 20, 0, 0], actuator), // right
    autopilot,
    batteries(),
    board()
  ]
}

function batteries() {
  return colorize(cssColors.purple, [
    translate([-60, 0, -boardZ], rotate([Math.PI / 2, Math.PI, Math.PI / 2], battery)), // left
    translate([60, 0, -boardZ], rotate([-Math.PI / 2, 0, Math.PI / 2], battery)) // right
  ])
}

function board() {
  const margin = 6
  const hole = cylinder({height: 20, radius: 3, segments: 15})
  const holes = [
    translate([margin - boardX / 2, margin - boardY / 2, 0], hole),
    translate([margin - boardX / 2, boardY / 2 - margin, 0], hole),
    translate([boardX / 2 - margin, margin - boardY / 2, 0], hole),
    translate([boardX / 2 - margin, boardY / 2 - margin, 0], hole)
  ]
  return colorize(cssColors.saddlebrown,
    subtract(
      roundedCuboid({center: [0, 0, -boardZ / 2], size: [boardX, boardY, boardZ], roundRadius: 1.5, segments: 30}),
      holes
    )
  )
}

module.exports = { main }
