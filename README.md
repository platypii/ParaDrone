# DubFlight

DubFlight is a C implementation of auto-pilot for gliders.

It is intended to run on a Raspberry Pi, linked with a GPS and a hardware interface to the aircraft controls.

The flight path planner uses Dubins Paths to efficiently compute paths for unpowered gliders.

[1] Dubins, L.E. (1957). "On Curves of Minimal Length with a Constraint on Average Curvature, and with Prescribed Initial and Terminal Positions and Tangents." American Journal of Mathematics.

## Build

Setup build environment:

`apt install libbluetooth-dev`

Build dubflight:

`make`

Run:

`./bin/dubflight`

## Hardware

Recommended configuration:

 - Raspberry Pi Zero W
 - BerryGPS module
 - Micro Maestro Servo controller
 - 2x Parallax Feedback 360 Servos
 - 50C 7.4V 5200mAh Lithium Polymer battery
 - UBEC power converter

Many of the components can be substituted with no problem.
Any Raspberry Pi series will work.
The software also supports bluetooth GPS such as the XGPS 160, however using BerryGPS means one less possible failure mode.
The battery and servos can be changed freely, the UBEC and Micro Maestro can handle 6-16V @ 3A.

## Raspberry Pi Setup

Install Raspbian Lite

`unzip -p 2019-09-26-raspbian-buster-lite.zip | sudo dd of=/dev/sdX bs=4M conv=fsync status=progress`

`raspi-config` > Enable wifi, ssh, etc.

### Systemd init

`cp dubflight.service /etc/systemd/system/`

`systemctl enable dubflight.service`

`systemctl start dubflight.service`

### BerryGPS

`raspi-config` > Interfacing options > disable serial shell.
