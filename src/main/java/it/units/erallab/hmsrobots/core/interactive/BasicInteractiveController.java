package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.behavior.PoseUtils;
import it.units.erallab.hmsrobots.core.controllers.AbstractController;
import it.units.erallab.hmsrobots.core.objects.Voxel;
import it.units.erallab.hmsrobots.util.Grid;

import java.util.*;

public class BasicInteractiveController extends AbstractController {

  private final List<Boolean> isKeyPressed;
  List<Set<Grid.Key>>  poses;
  private boolean enabledFlag;

  public BasicInteractiveController() {
    isKeyPressed = new ArrayList<>();
    for (int i = 0; i<4; i++) {
      isKeyPressed.add(false);
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
    Grid<Boolean> shape = getShape(voxels);
    poses = new ArrayList<>(PoseUtils.computeCardinalPoses(shape));
    Grid<Double> values = Grid.create(voxels, v -> 1d);
    for (int i = 0; i < isKeyPressed.size(); i++) {
      for (Grid.Key key : poses.get(i)) {
        values.set(key.x(), key.y(), isKeyPressed.get(i)?-1d:1d);
      }
    }
    return values;
  }

  @Override
  public void reset() {

  }

  public void setKeyPressed(boolean keyPressed, int index) {
    this.isKeyPressed.set(index, keyPressed);
  }

  public List<Boolean> getFlags() { return isKeyPressed; }
}
