# BASEline ParaDrone - AutoPilot Software

ParaDrone autopilot software runs on a small arduino microcontroller. I use the Heltec ESP32 LoRa V2. This module is nice because it has the ESP32, a screen, and a LoRa radio all bundled together.

PlatformIO is used to manage the software.

## Development

Install Visual Studio Code and PlatformIO.
Open the project in this directory.

## Running

Connect the ESP32 to the computer by USB.
Use the PlatformIO "Upload" function to program the device.

## Testing

```
pio test -e native
```
