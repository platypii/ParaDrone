# BASEline ParaDrone Companion App

Android app to interface with the BASEline ParaDrone hardware.

ParaDrone runs on arduino. This app connects to ParaDrone using bluetooth LE.

The app displays flight information on a map.

## Landing zone

You can use the app to set the target landing zone. Click the "LZ" button to set the landing zone. An arrow will appear.
Line up the map so that the arrow indicates the direction of landing, and the tip of the arrow is the target landing zone.

The app then uses the internet to get elevation data for that location, and sends the target to the ParaDrone device.

Important: Always verify that the landing zone was set successfully! The icon on the map should update, and the LZ should match on the phone and the device.

