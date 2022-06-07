# BASEline ParaDrone - Remote Control Software

ParaDrone supports remote control operation via LoRa radio. I use the Heltec ESP32 LoRa V2 as a kind of LoRa modem. The ParaDrone android app connects to the Remote Control via bluetooth, and the Remote Control sends commands to ParaDrone via LoRa. This is the code for the Remote Control.

PlatformIO is used to manage the software.

## Development

[Download](https://code.visualstudio.com/) and install Visual Studio Code.
Open VSCode Extension Manager.
Search for official PlatformIO IDE extension.
Install PlatformIO IDE.
Open the project in this directory.

## Running

Connect the ESP32 to the computer by USB.
Use the PlatformIO "Upload" function to program the device.
