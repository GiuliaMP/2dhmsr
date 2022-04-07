package it.units.erallab.hmsrobots.core.interactive;

public interface DevicePoller {

    void start(BasicInteractiveController basicInteractiveController, CanvasManager canvasManager);
    void setEnabledFlag(boolean flag);
}
