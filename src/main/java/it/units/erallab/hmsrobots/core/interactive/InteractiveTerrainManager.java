package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

public class InteractiveTerrainManager {

  public final static double INITIAL_PLACEMENT_X_GAP = 1d;
  public final static double INITIAL_PLACEMENT_Y_GAP = 1d;
  public final static double TERRAIN_BORDER_HEIGHT = 100d;
  public static final int TERRAIN_LENGTH = 2000;
  public static final double TERRAIN_BORDER_WIDTH = 10d;

  public static double[][] createTerrain(String name) {
    String hilly = "hilly-(?<h>[0-9]+(\\.[0-9]+)?)-(?<w>[0-9]+(\\.[0-9]+)?)-(?<seed>[0-9]+)";
    String downhill = "downhill-(?<angle>[0-9]+(\\.[0-9]+)?)";
    String downhillHilly = "downhillHilly-(?<h>[0-9]+(\\.[0-9]+)?)-(?<w>[0-9]+(\\.[0-9]+)?)-(?<seed>[0-9]+)";
    Map<String, String> params;
    //noinspection UnusedAssignment
    if ((params = Utils.params(downhillHilly, name)) != null) {
      double h = Double.parseDouble(params.get("h"));
      double w = Double.parseDouble(params.get("w"));

      double xTot = (TERRAIN_LENGTH - 2 * TERRAIN_BORDER_WIDTH);
      double yTot = xTot * Math.sin(5.0 / 180 * Math.PI);

      RandomGenerator random = new Random(Integer.parseInt(params.get("seed")));
      List<Double> xs = new ArrayList<>(List.of(0d, TERRAIN_BORDER_WIDTH));
      List<Double> ys = new ArrayList<>(List.of(TERRAIN_BORDER_HEIGHT + yTot, yTot));
      double previousYs = ys.get(ys.size() - 1);
      while (xs.get(xs.size() - 1) < TERRAIN_LENGTH - TERRAIN_BORDER_WIDTH) {
        double currentX = xs.get(xs.size() - 1) + Math.max(1d, (random.nextGaussian() * 0.25 + 1) * w);
        double deltaH = yTot * currentX / xTot;
        double currentY = previousYs + random.nextGaussian() * h;
        xs.add(currentX);
        ys.add(currentY - deltaH);
        previousYs = currentY;
      }


      xs.addAll(List.of(xs.get(xs.size() - 1) + TERRAIN_BORDER_WIDTH));
      ys.addAll(List.of(TERRAIN_BORDER_HEIGHT));
      return new double[][]{
          xs.stream().mapToDouble(d -> d).toArray(),
          ys.stream().mapToDouble(d -> d).toArray()
      };
    }
    if ((params = Utils.params(hilly, name)) != null) {
      double h = Double.parseDouble(params.get("h"));
      double w = Double.parseDouble(params.get("w"));
      RandomGenerator random = new Random(Integer.parseInt(params.get("seed")));
      List<Double> xs = new ArrayList<>(List.of(0d, TERRAIN_BORDER_WIDTH));
      List<Double> ys = new ArrayList<>(List.of(TERRAIN_BORDER_HEIGHT, 0d));
      while (xs.get(xs.size() - 1) < TERRAIN_LENGTH - TERRAIN_BORDER_WIDTH) {
        xs.add(xs.get(xs.size() - 1) + Math.max(1d, (random.nextGaussian() * 0.25 + 1) * w));
        ys.add(ys.get(ys.size() - 1) + random.nextGaussian() * h);
      }
      xs.addAll(List.of(xs.get(xs.size() - 1) + TERRAIN_BORDER_WIDTH));
      ys.addAll(List.of(TERRAIN_BORDER_HEIGHT));
      return new double[][]{
          xs.stream().mapToDouble(d -> d).toArray(),
          ys.stream().mapToDouble(d -> d).toArray()
      };
    }
    if ((params = Utils.params(downhill, name)) != null) {
      double angle = Double.parseDouble(params.get("angle"));
      double dY = (TERRAIN_LENGTH - 2 * TERRAIN_BORDER_WIDTH) * Math.sin(angle / 180 * Math.PI);
      return new double[][]{
          new double[]{0, TERRAIN_BORDER_WIDTH, TERRAIN_LENGTH - TERRAIN_BORDER_WIDTH, TERRAIN_LENGTH},
          new double[]{TERRAIN_BORDER_HEIGHT + dY, 5 + dY, 5, TERRAIN_BORDER_HEIGHT}
      };
    }
    throw new IllegalArgumentException(String.format("Unknown terrain name: %s", name));
  }
}
