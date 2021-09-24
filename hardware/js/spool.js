// title      : ParaDrone Actuator Pulley Spool
// author     : BASEline
// license    : MIT License
// tags       : Motor,actuator,linear,drone,paraglider
// file       : spool.jscad

const { cuboid, cylinder } = require('@jscad/modeling').primitives
const { subtract, union } = require('@jscad/modeling').booleans
const { translate } = require('@jscad/modeling').transforms

const qty = 1

// radii
const pulleyRadius = 19 - 0.8
const innerRadius = 8
const outerRadius = 8
const threadRadius = 1.1
const pinRadius = 4.2

// thicness
const discThic = 2
const axleThic = 5

function main() {
  // return onepiece()
  return twopiece()
}

function twopiece() {
  return union(
    translate([0, -20, 0],
      subtract(
        half1(),
        gluechannel()
      )
    ),
    translate([0, 20, 0], half2())
  )
}

function onepiece() {
  return union(
    translate([0, 0, 0], half1()),
    translate([0, 0, discThic + axleThic], half2())
  )
}

function half1() {
  return subtract(
    solid(innerRadius),
    pin()
  )
}

function half2() {
  return subtract(
    solid(outerRadius),
    pin(),
    threadhole()
  )
}

function solid(radius) {
  return union(
    cylinder({radius: pulleyRadius, height: discThic, center: [0, 0, discThic / 2], segments: qty * 120}), // disc
    cylinder({radius: radius, height: axleThic, center: [0, 0, discThic + axleThic / 2], segments: qty * 100}) // axle
  )
}

function pin() {
  return subtract(
    cylinder({radius: pinRadius, height: 18, center: [0, 0, 9], segments: qty * 80}),
    cuboid({size: [100, 100, 100], center: [0, 53, 50]})
  )
}

function threadhole() {
  return cylinder({radius: threadRadius, height: discThic, center: [0, innerRadius + threadRadius, discThic / 2], segments: qty * 24})
}

function gluechannel() {
  const depth = 0.5
  const midZ = discThic + axleThic - depth / 2
  return subtract(
    cylinder({radius: 6.4, height: depth, center: [0, 0, midZ], segments: qty * 60}),
    cylinder({radius: 5.6, height: depth, center: [0, 0, midZ], segments: qty * 60})
  )
}

module.exports = { main }
