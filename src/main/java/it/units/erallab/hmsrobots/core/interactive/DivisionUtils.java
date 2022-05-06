package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.util.Grid;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DivisionUtils {

  public static Set<Set<Grid.Key>> computeTwoPosesLeftRight(Grid<Boolean> shape) {
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
    return new LinkedHashSet<>(List.of(top, bottom));
  }

  public static Set<Set<Grid.Key>> computeTwoPosesUpDown(Grid<Boolean> shape) {
    Set<Grid.Key> center = shape.stream()
        .filter(Grid.Entry::value)
        .map(Grid.Entry::key)
        .collect(Collectors.toSet());
    double midCenterY = center.stream().mapToDouble(Grid.Key::y).average().orElse(0d);
    Set<Grid.Key> top = center.stream().filter(k -> k.y() <= midCenterY).collect(Collectors.toSet());
    Set<Grid.Key> bottom = center.stream().filter(k -> k.y() > midCenterY).collect(Collectors.toSet());
    return new LinkedHashSet<>(List.of(top, bottom));
  }
}
