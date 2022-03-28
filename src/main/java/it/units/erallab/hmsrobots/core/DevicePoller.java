package it.units.erallab.hmsrobots.core;

import it.units.erallab.hmsrobots.core.controllers.BasicInteractiveController;
import it.units.erallab.hmsrobots.core.snapshots.InteractiveSnapshotListener;

public interface DevicePoller {

    void start(BasicInteractiveController basicInteractiveController, InteractiveSnapshotListener interactiveSnapshotListener);
}
