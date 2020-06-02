package ws.baseline.autopilot.map;

import ws.baseline.autopilot.Services;
import ws.baseline.autopilot.bluetooth.APLandingZone;

class PlanLayer extends PathLayer {

    @Override
    public void update() {
        if (APLandingZone.lastLz != null) {
            setPath(Services.flightComputer.plan, APLandingZone.lastLz.lz);
        }
        super.update();
    }
}
