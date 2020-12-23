// title      : ParaDrone Actuator Pulley Spool
// author     : BASEline
// license    : MIT License
// revision   : 1.0
// tags       : Motor,actuator,linear,drone,paraglider
// file       : spool.jscad

const qty = 1

const pulleyRadius = 19 - 0.8
const axleRadius = 9
const couplingRadius = 6

const discThic = 2
const axleThic = 5
const threadRadius = 1.1

function main() {
  return onepiece()
  return twopiece()
}

function twopiece() {
  return union(
    translate([0, -20, 0],
      difference(
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
  return difference(
    solid(axleRadius),
    pin()
  )
}

function half2() {
  return difference(
    solid(couplingRadius),
    pin(),
    threadhole()
  )
}

function solid(axleRadius) {
  return union(
    cylinder({r: pulleyRadius, start: [0, 0, 0], end: [0, 0, discThic], fn: qty * 100}), // disc
    cylinder({r: axleRadius, start: [0, 0, discThic], end: [0, 0, discThic + axleThic], fn: qty * 100}) // axle
  )
}

function pin() {
  return difference(
    cylinder({r: 3.15, start: [0, 0, 0], end: [0, 0, 18], fn: qty * 80}),
    translate([-50, 2.65, 0], cube({size: [100, 100, 100]}))
  )
}

function threadhole() {
  return cylinder({r: threadRadius, start: [0, axleRadius + threadRadius, 0], end: [0, axleRadius + threadRadius, discThic], fn: qty * 120})
}

function gluechannel() {
  const top = discThic + axleThic
  const bottom = discThic + axleThic - 0.5
  return difference(
    cylinder({r: 6.0, start: [0, 0, bottom], end: [0, 0, top], fn: qty * 80}),
    cylinder({r: 5.2, start: [0, 0, bottom], end: [0, 0, top], fn: qty * 80})
  )
}
