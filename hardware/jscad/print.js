// title      : ParaDrone
// author     : BASEline
// tags       : Microcontroller,arduino,autopilot,drone,paraglider

const { mirrorX, rotateZ, translate } = require("@jscad/modeling").transforms
const actuator = require("./actuator").main(true)
const autopilot = require("./autopilot").main(true)
const battery = require("./battery").main(true)

function main() {
  return [
    translate([-75, 20, 0], mirrorX(actuator)), // left actuator
    translate([-75, 80, 0], actuator), // right actuator
    translate([-5, 60, 0], rotateZ(Math.PI / 2, battery)), // left battery case
    translate([5, 60, 0], rotateZ(-Math.PI / 2, battery)), // right battery case
    autopilot
  ]
}

module.exports = { main }
