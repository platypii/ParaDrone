// title      : ParaDrone Actuator Pulley Spool
// author     : BASEline
// tags       : Motor,actuator,linear,drone,paraglider

const jscad = require("@jscad/modeling")
const { subtract, union } = jscad.booleans
const { cuboid, cylinder } = jscad.primitives
const { rotateY, translate } = jscad.transforms

let qty = 1

//   ==   axle
// ====== disc
//   ==   axle
// ====== disc
const discRadius = 19 - 0.8
const axleRadius = 8
const shaftRadius = 4.2
const threadRadius = 1.1

// thicness
const discThic = 2
const axleThic = 5

function getParameterDefinitions() {
  return [
    {name: "tooling", type: "choice", values: ["FDM", "CNC"], caption: "Tooling"},
    {name: "print", type: "checkbox", checked: false, caption: "Print mode"}
  ]
}

function main(params) {
  if (params.print) {
    qty = 3
  }
  if (params.tooling === "CNC") {
    return onepiece()
  } else {
    return twopiece()
  }
}

function twopiece() {
  return [
    translate([0, -20, 0],
      subtract(
        solid(),
        gluechannel(),
        dShaft()
      )
    ),
    translate([0, 20, 0],
      subtract(
        solid(),
        threadhole(),
        dShaft()
      )
    )
  ]
}

function onepiece() {
  return [
    subtract(
      solid(),
      shaft()
    ),
    translate([0, 0, discThic + axleThic],
      subtract(
        solid(),
        shaft(),
        threadhole(),
        setScrew()
      )
    )
  ]
}

function solid() {
  return union(
    cylinder({radius: discRadius, height: discThic, center: [0, 0, discThic / 2], segments: qty * 120}), // disc
    cylinder({radius: axleRadius, height: axleThic, center: [0, 0, discThic + axleThic / 2], segments: qty * 100}) // axle
  )
}

/**
 * Motor shaft
 */
function shaft() {
  return cylinder({radius: shaftRadius, height: 18, center: [0, 0, 9], segments: qty * 80})
}

/**
 * D-shaft
 */
function dShaft() {
  return subtract(
    shaft(),
    cuboid({size: [100, 100, 100], center: [0, 53, 50]})
  )
}

/**
 * Set screw
 */
function setScrew() {
  const z = discThic + axleThic / 2
  return rotateY(Math.PI / 2,
    cylinder({radius: threadRadius, height: axleRadius, center: [-z, 0, axleRadius / 2], segments: qty * 24})
  )
}

/**
 * Thread hole to secure pulley string
 */
function threadhole() {
  return cylinder({radius: threadRadius, height: discThic, center: [0, axleRadius + threadRadius, discThic / 2], segments: qty * 24})
}

/**
 * Channel to hold glue for two piece version
 */
function gluechannel() {
  const depth = 0.5
  const midZ = discThic + axleThic - depth / 2
  return subtract(
    cylinder({radius: 6.4, height: depth, center: [0, 0, midZ], segments: qty * 60}),
    cylinder({radius: 5.6, height: depth, center: [0, 0, midZ], segments: qty * 60})
  )
}

module.exports = { getParameterDefinitions, main }
