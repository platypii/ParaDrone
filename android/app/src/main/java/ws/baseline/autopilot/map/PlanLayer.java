package ws.baseline.autopilot.map;

import ws.baseline.autopilot.Services;

class PlanLayer extends PathLayer {

    @Override
    public void update() {
        setPath(Services.flightComputer.plan, Services.flightComputer.lz);
        super.update();
    }
}
