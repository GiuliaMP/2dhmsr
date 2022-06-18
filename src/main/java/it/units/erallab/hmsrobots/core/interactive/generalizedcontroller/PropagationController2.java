package it.units.erallab.hmsrobots.core.interactive.generalizedcontroller;

import it.units.erallab.hmsrobots.core.interactive.DevicePoller;
import it.units.erallab.hmsrobots.core.objects.Voxel;
import it.units.erallab.hmsrobots.util.Grid;

import java.util.Map;
import java.util.function.Function;

public class PropagationController2 extends InteractiveController {

  private final double propagationTime;

  private double propagationStartTimeRight;
  private double propagationStartTimeLeft;

  public PropagationController2(double propagationTime, double propagationLag, String division, DevicePoller devicePoller) {
    super(division, devicePoller);
    this.propagationTime = propagationTime;
    Function<Double, Double> wave = x -> {
      if (x < 0 || x > 4 * (propagationTime - propagationLag)) {
        return 0d;
      } else {
        return Math.sin(Math.PI * x / (2 * (propagationTime - propagationLag)));
      }
    };
    reset();
  }

  @Override
  public Grid<Double> computeControlSignals(double t, Grid<Voxel> voxels) {
    Map<DevicePoller.RobotAreas, Boolean> keyPressed = devicePoller.getKeyPressed();
    setRobotAreasToContract(keyPressed.get(DevicePoller.RobotAreas.LEFT), 0);
    setRobotAreasToContract(keyPressed.get(DevicePoller.RobotAreas.RIGHT), 1);

    if (robotAreasToContract.get(1)) {
      propagationStartTimeRight = t;
      setRobotAreasToContract(false, 1);
    }

    if (robotAreasToContract.get(0)) {
      propagationStartTimeLeft = t;
      setRobotAreasToContract(false, 0);
    }

    Grid<Double> aGrid = Grid.create(voxels.getW(), voxels.getH(), (x, y) -> {
      double pRight = (t - propagationStartTimeRight) / propagationTime;
      double pLeft = (t - propagationStartTimeLeft) / propagationTime;
      double lag = (-voxels.getW()+.8);
      double waveRight = Math.sin(lag+(double) x / (double) voxels.getW() * Math.PI + pRight * Math.PI);
      double waveLeft = Math.sin(-(double) x / (double) voxels.getW() * Math.PI + pLeft * Math.PI);
      return waveRight * ((t - propagationStartTimeRight > propagationTime) ? -1 : 1)
          + waveLeft * ((t - propagationStartTimeLeft > propagationTime) ? -1 : 1);
    });
    return aGrid;
  }

  @Override
  public void reset() {
    propagationStartTimeRight = -10;
    propagationStartTimeLeft = -10;
    setRobotAreasToContract(false, 0);
    setRobotAreasToContract(false, 1);
  }

}
