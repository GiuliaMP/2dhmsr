package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;
import it.units.erallab.hmsrobots.core.objects.Voxel;
import it.units.erallab.hmsrobots.util.Grid;

import java.util.*;
import java.util.stream.Collectors;

public class BasicInteractiveController extends AbstractController {

  private final List<Boolean> isKeyPressed;
  List<Set<Grid.Key>>  poses;
  int division;

  public BasicInteractiveController(int division) {
    this.division = division;
    isKeyPressed = new ArrayList<>();
    for (int i = 0; i<division; i++) {
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

  public static Set<Set<Grid.Key>> computeTwoPoses(Grid<Boolean> shape) {
    Set<Grid.Key> top = shape.stream()
        .filter(e -> e.key().x() < shape.getW() / 2d)
        .filter(Grid.Entry::value)
        .map(Grid.Entry::key)
        .collect(Collectors.toSet());
    Set<Grid.Key> bottom = shape.stream()
        .filter(e -> e.key().x() >= shape.getW() * 2d / 4d)
        .filter(Grid.Entry::value)
        .map(Grid.Entry::key)
        .collect(Collectors.toSet());
    /*Set<Grid.Key> center = shape.stream()
        .filter(Grid.Entry::value)
        .map(Grid.Entry::key)
        .collect(Collectors.toSet());
    double midCenterY = center.stream().mapToDouble(Grid.Key::y).average().orElse(0d);
    Set<Grid.Key> top = center.stream().filter(k -> k.y() <= midCenterY).collect(Collectors.toSet());
    Set<Grid.Key> bottom = center.stream().filter(k -> k.y() > midCenterY).collect(Collectors.toSet());*/
    return new LinkedHashSet<>(List.of(top, bottom));
  }

  @Override
  public Grid<Double> computeControlSignals(double t, Grid<Voxel> voxels) {
    Grid<Boolean> shape = getShape(voxels);
    //poses = new ArrayList<>(PoseUtils.computeCardinalPoses(shape));
    poses = new ArrayList<>(computeTwoPoses(shape));
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
