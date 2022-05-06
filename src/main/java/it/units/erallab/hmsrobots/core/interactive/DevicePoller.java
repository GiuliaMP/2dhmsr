package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;

public interface DevicePoller {
  void start(BasicInteractiveController basicInteractiveController, CanvasManager canvasManager);
}
