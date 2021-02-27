# BASEline ParaDrone

BASEline ParaDrone aims to automate the flight of any parachute or paraglider.

The system consists of:
 - Hardware to pull left and right toggles.
 - Electronics for remote control and autopilot control.
 - Autopilot software

The flight operations manual, including instructions for building your own, is available at:
https://paradr.one/

In this repository:
 - `autopilot/` autopilot software for arduino
 - `hardware/` 3d printing cad files
 - `remotecontrol/` remote control software for arduino using LoRa
 - `simulator/` a web-based path planning simulator

## Software

Autopilot and remote control software runs on small ESP32 dev boards, using the arduino framework. These are super cheap but powerful microcontrollers. The Heltec ESP32 LoRa V2 is recommended as it also has a 128x64 display and a LoRa radio. PlatformIO is used to manage the software.

### To program the autopilot device:

Install Visual Studio Code and PlatformIO.
Open the project in the `autopilot` directory of this project.
Connect the ESP32 to the computer by USB.
Use the PlatformIO "Upload" function to program the device.

### To program the remote control device:

Same thing but in the `remotecontrol` project directory.
