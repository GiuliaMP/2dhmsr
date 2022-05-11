package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;

import java.util.Map;

public interface DevicePoller {

  Map<DevicePoller.RobotAreas, Boolean> getKeyPressed();

  void start(AbstractController basicInteractiveController, CanvasManager canvasManager);


  enum RobotAreas {
    LEFT,
    RIGHT,
    UP,
    DOWN,

    IMPULSE
  }
}
