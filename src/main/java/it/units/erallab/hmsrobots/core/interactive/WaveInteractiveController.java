package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;
import it.units.erallab.hmsrobots.core.objects.Voxel;
import it.units.erallab.hmsrobots.util.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WaveInteractiveController extends AbstractController {
  private boolean isKeyPressed;
  List<Set<Grid.Key>> poses;

  public WaveInteractiveController() {
    isKeyPressed = false;
  }

  private Grid<Boolean> getShape(Grid<Voxel> voxels) {
    Grid<Boolean> shape = Grid.create(voxels, Objects::isNull);
    for (var val : voxels) {
      shape.set(val.key().x(), val.key().y(), voxels.get(val.key().x(), val.key().y()) != null);
    }
    return shape;
  }

  // Parte da sistemare
  @Override
  public Grid<Double> computeControlSignals(double t, Grid<Voxel> voxels) {
    Grid<Boolean> shape = getShape(voxels);
    poses = new ArrayList<>(DivisionUtils.computeTwoPosesLeftRight(shape));
    Grid<Double> values = Grid.create(voxels, v -> 1d);
    int i = 0;
    for (Grid.Key key : poses.get(i)) {
      values.set(key.x(), key.y(), isKeyPressed ? -1d : 1d);
    }
    return values;
  }

  @Override
  public void reset() {

  }

  public void setKeyPressed (boolean flag) {
    isKeyPressed = flag;
  }

}
