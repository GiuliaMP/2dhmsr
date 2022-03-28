package it.units.erallab.hmsrobots.core.interactive;

public interface DevicePoller {

    void start(BasicInteractiveController basicInteractiveController, InteractiveSnapshotListener interactiveSnapshotListener);
}
