package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.behavior.PoseUtils;
import it.units.erallab.hmsrobots.core.controllers.AbstractController;
import it.units.erallab.hmsrobots.core.objects.Voxel;
import it.units.erallab.hmsrobots.util.Grid;

import java.util.*;

public class BasicInteractiveController extends AbstractController {

  private final List<Boolean> robotAreasToContract;
  List<Set<Grid.Key>> poses;
  String division;
  private DevicePoller devicePoller;

  public BasicInteractiveController(String division, DevicePoller devicePoller) {
    this.division = division;
    this.devicePoller = devicePoller;
    int divisionInt = division.equals("4") ? 4 : 2;

    robotAreasToContract = new ArrayList<>();
    for (int i = 0; i < divisionInt; i++) {
      robotAreasToContract.add(false);
    }
  }

  private Grid<Boolean> getShape(Grid<Voxel> voxels) {
    Grid<Boolean> shape = Grid.create(voxels, Objects::isNull);
    for (var val : voxels) {
      shape.set(val.key().x(), val.key().y(), voxels.get(val.key().x(), val.key().y()) != null);
    }
    return shape;
  }

  @Override
  public Grid<Double> computeControlSignals(double t, Grid<Voxel> voxels) {
    Map<DevicePoller.RobotAreas, Boolean> keyPressed = new HashMap<>(devicePoller.getKeyPressed());
    Grid<Boolean> shape = getShape(voxels);
    if (division.equals("2ud")) {
      poses = new ArrayList<>(DivisionUtils.computeTwoPosesUpDown(shape));
      setRobotAreasToContract(keyPressed.get(DevicePoller.RobotAreas.UP), 1);
      setRobotAreasToContract(keyPressed.get(DevicePoller.RobotAreas.DOWN), 0);
    } else if (division.equals("4")) {
      poses = new ArrayList<>(PoseUtils.computeCardinalPoses(shape));
      setRobotAreasToContract(keyPressed.get(DevicePoller.RobotAreas.UP), 2);
      setRobotAreasToContract(keyPressed.get(DevicePoller.RobotAreas.DOWN), 1);
      setRobotAreasToContract(keyPressed.get(DevicePoller.RobotAreas.LEFT), 0);
      setRobotAreasToContract(keyPressed.get(DevicePoller.RobotAreas.RIGHT), 3);
    } else {
      poses = new ArrayList<>((DivisionUtils.computeTwoPosesLeftRight(shape)));
      setRobotAreasToContract(keyPressed.get(DevicePoller.RobotAreas.LEFT), 0);
      setRobotAreasToContract(keyPressed.get(DevicePoller.RobotAreas.RIGHT), 1);
    }
    Grid<Double> values = Grid.create(voxels, v -> 1d);
    for (int i = 0; i < robotAreasToContract.size(); i++) {
      for (Grid.Key key : poses.get(i)) {
        values.set(key.x(), key.y(), robotAreasToContract.get(i) ? -1d : 1d);
      }
    }
    return values;
  }


  @Override
  public void reset() {

  }

  private void setRobotAreasToContract(boolean keyPressed, int index) {
    this.robotAreasToContract.set(index, keyPressed);
  }

}

