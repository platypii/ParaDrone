
# ParaDrone simulator

Runs a simulation of the ParaDrone path planner.
Allows testing and development of the path planning algorithms from the comfort of your home.

The simulator is available online at:
https://paradr.one/simulator/index.html

![ParaDrone simulator](/website/html/img/sim.jpg)


## Usage

Click on the map to set the paraglider starting location.
The blue line is the current flight plan.
The purple line is the simulated actual flight path.

Wind is simulated but wind information is NOT available to the glider, just like real life.
Use the wind rose to change the wind conditions.

The "test" button starts the paraglider at a grid of points and determines the accuracy of the landings.


## Running locally

In a terminal run:

```
npm i
npm run build
npm run serve
```

Then go to:

http://locahost:8080/
