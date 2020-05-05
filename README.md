# BASEline ParaDrone

BASEline ParaDrone aims to automate the flight of any parachute or paraglider.

The system consists of:
 - Hardware to pull left and right toggles.
 - Electronics for remote control and autopilot control.
 - Autopilot software

The flight operations manual, including instructions for building your own, is available at:
https://paradr.one/

This repository contains:
 - `arduino/` contains autopilot software
 - `hardware/` contains 3d printing cad files
 - `simulator/` contains a web-based path planning simulator

## Software

Autopilot software runs on an arduino board such as the ESP32. These are super cheap but powerful microcontrollers. We use PlatformIO to manage the software.

To program the ESP32:

Install Visual Studio Code and PlatformIO.
Open the project in the `arduino` directory of this project.
Connect the ESP32 to the computer by USB.
Use the PlatformIO "Upload" function to program the device.
