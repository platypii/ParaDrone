package ws.baseline.paradrone.map;

import ws.baseline.paradrone.Services;
import ws.baseline.paradrone.bluetooth.APLandingZone;

class PlanLayer extends PathLayer {

    @Override
    public void update() {
        if (APLandingZone.lastLz != null) {
            setPath(Services.flightComputer.plan, APLandingZone.lastLz.lz);
        }
        super.update();
    }
}
