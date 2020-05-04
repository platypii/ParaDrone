# BASEline ParaDrone

BASEline ParaDrone aims to automate the flight of any parachute or paraglider.

The system consists of:
 - Hardware to pull left and right toggles.
 - Electronics for remote control and autopilot control.
 - Autopilot software

## Hardware

Recommended configuration:
 - Build tools:
   - Phillips screwdriver
   - Super glue
   - Soldering kit

Many of the components can be substituted with no problem.
The battery and motors can be changed as long as there is enough power.
Most arduino variants should work.

## Toggle actuator (2x)

### Materials

 - 3D printed actuator top
 - 3D printed actuator bottom
 - 3D printed motor bracket
 - 12V DC 200RPM Gear Box Motor
 - 31cm pulley spool
 - Limit switch 20x6x10mm
 - 2x 20cm 20 gauge wire
 - 3A diode
 - 2x banana plugs
 - 6x wood screws #4 x 1/2"
 - 2x M3 8mm machine screws
 - QuicRun 1060 ESC
 - 140cm 100lbs braided fishing line

### Assembly

1. Pre-solder limit switch with 20cm wires on outer (NC) pins.

2. Glue limit switch to actuator top.

3. Drill mounting holes in board.

4. Screw actuator bottom and motor bracket to board.

5. Screw motor to actuator bottom.

6. Tie fishing line to spool.

7. Attach spool to motor shaft, tighen coupling screws. You may need to power the motor to rotate the screws into position.

8. Thread the limit switch wires through actuator bottom and motor bracket.

9. Thread fishing line through actuator top.

10. Snap actuator top into actuator bottom.

11. Girth hitch the fishing line to the snap shackle.

12. Solder wires, diode, and banana plugs.

## AutoPilot

Materials:

 - 11.1V lithium polymer (LiPo) battery
 - Heltec ESP32 LoRa
 - BN-220 GPS
 - Pololu dual MC33926 motor driver

## Pins

ESP32
 - Pin 17 -> Driver M1_IN2
 - Pin 2 -> Driver M1_IN1
 - Pin 23 -> Driver M2_IN2
 - Pin 22 -> Driver M2_IN1
 - Pin 12 -> GPS out
 - Pin 36 -> GPS in

Motor Driver
 - M2_FB -> ESP32 pin 33
 - M2_IN1 -> ESP32 pin 22
 - M2_IN2 -> ESP32 pin 23
 - M1_FB -> ESP32 pin 32
 - M1_IN1 -> ESP32 pin 2
 - M1_IN2 -> ESP32 pin 17
 - VDD -> ESP32 +3V
 - GND -> BEC GND
 - VIN -> BEC in

GPS
 - red -> ESP32 +3V
 - black -> ESP32 GND
 - green -> ESP32 pin 36
 - white -> ESP32 pin 12

## Software

Autopilot software runs on an arduino board such as the ESP32. These are super cheap but powerful microcontrollers. We use PlatformIO to manage the software.

To program the ESP32:

Install Visual Studio Code and PlatformIO.
Open the project in the `arduino` directory of this project.
Connect the ESP32 to the computer by USB.
Use the PlatformIO "Upload" function to program the device.
