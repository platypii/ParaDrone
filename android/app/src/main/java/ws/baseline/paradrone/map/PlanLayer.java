package ws.baseline.paradrone.map;

import ws.baseline.paradrone.Services;
import ws.baseline.paradrone.bluetooth.ApLandingZone;

class PlanLayer extends PathLayer {

    @Override
    public void update() {
        if (ApLandingZone.lastLz != null) {
            setPath(Services.flightComputer.plan, ApLandingZone.lastLz.lz);
        }
        super.update();
    }
}
