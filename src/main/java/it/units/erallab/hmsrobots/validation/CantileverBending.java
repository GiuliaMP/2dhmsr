/*
 * Copyright (C) 2019 Eric Medvet <eric.medvet@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.erallab.hmsrobots.validation;

import com.google.common.base.Stopwatch;
import it.units.erallab.hmsrobots.objects.immutable.Snapshot;
import it.units.erallab.hmsrobots.objects.Ground;
import it.units.erallab.hmsrobots.objects.Voxel;
import it.units.erallab.hmsrobots.objects.VoxelCompound;
import it.units.erallab.hmsrobots.objects.WorldObject;
import it.units.erallab.hmsrobots.objects.immutable.Point2;
import it.units.erallab.hmsrobots.problems.AbstractEpisode;
import it.units.erallab.hmsrobots.problems.Episode;
import it.units.erallab.hmsrobots.util.CSVWriter;
import it.units.erallab.hmsrobots.util.Grid;
import it.units.erallab.hmsrobots.viewers.OnlineViewer;
import it.units.erallab.hmsrobots.viewers.SnapshotListener;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dyn4j.dynamics.Settings;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.joint.WeldJoint;
import org.dyn4j.geometry.Vector2;

/**
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class CantileverBending extends AbstractEpisode<Grid<Voxel.Builder>, CantileverBending.Result> {

  public static class Result {

    private final double realTime;
    private final double dampingRealTime;
    private final double dampingSimTime;
    private final long steps;
    private final long dampingSteps;
    private final double minVoxelStepPerSecond;
    private final double minStepPerSecond;
    private final double overallVoxelStepPerSecond;
    private final double overallStepPerSecond;
    private final double yDisplacement;
    private final Map<String, List<Double>> timeEvolution;
    private final List<Point2> finalTopPositions;

    public Result(double realTime, double dampingRealTime, double dampingSimTime, long steps, long dampingSteps, double minVoxelStepPerSecond, double minStepPerSecond, double overallVoxelStepPerSecond, double overallStepPerSecond, double yDisplacement, Map<String, List<Double>> timeEvolution, List<Point2> finalTopPositions) {
      this.realTime = realTime;
      this.dampingRealTime = dampingRealTime;
      this.dampingSimTime = dampingSimTime;
      this.steps = steps;
      this.dampingSteps = dampingSteps;
      this.minVoxelStepPerSecond = minVoxelStepPerSecond;
      this.minStepPerSecond = minStepPerSecond;
      this.overallVoxelStepPerSecond = overallVoxelStepPerSecond;
      this.overallStepPerSecond = overallStepPerSecond;
      this.yDisplacement = yDisplacement;
      this.timeEvolution = timeEvolution;
      this.finalTopPositions = finalTopPositions;
    }

    public double getRealTime() {
      return realTime;
    }

    public double getDampingRealTime() {
      return dampingRealTime;
    }

    public double getDampingSimTime() {
      return dampingSimTime;
    }

    public long getSteps() {
      return steps;
    }

    public long getDampingSteps() {
      return dampingSteps;
    }

    public double getMinVoxelStepPerSecond() {
      return minVoxelStepPerSecond;
    }

    public double getMinStepPerSecond() {
      return minStepPerSecond;
    }

    public double getOverallVoxelStepPerSecond() {
      return overallVoxelStepPerSecond;
    }

    public double getOverallStepPerSecond() {
      return overallStepPerSecond;
    }

    public double getYDisplacement() {
      return yDisplacement;
    }

    public Map<String, List<Double>> getTimeEvolution() {
      return timeEvolution;
    }

    public List<Point2> getFinalTopPositions() {
      return finalTopPositions;
    }

  }

  private final static double WALL_MARGIN = 10d;

  private final double force;
  private final double forceDuration;
  private final double maxT;
  private final double epsilon;

  public CantileverBending(double force, double forceDuration, double maxT, double epsilon, Settings settings, SnapshotListener listener) {
    super(listener, settings);
    this.force = force;
    this.forceDuration = forceDuration;
    this.maxT = maxT;
    this.epsilon = epsilon;
  }

  @Override
  public Result apply(Grid<Voxel.Builder> builderGrid) {
    List<WorldObject> worldObjects = new ArrayList<>();
    //build voxel compound
    VoxelCompound vc = new VoxelCompound(0, 0, new VoxelCompound.Description(
            Grid.create(builderGrid.getW(), builderGrid.getH(), true), null, builderGrid
    ));
    Point2[] boundingBox = vc.boundingBox();
    worldObjects.add(vc);
    //build ground
    Ground ground = new Ground(new double[]{0, 1}, new double[]{0, boundingBox[1].y - boundingBox[0].y + 2d * WALL_MARGIN});
    worldObjects.add(ground);
    //build world w/o gravity
    World world = new World();
    world.setSettings(settings);
    world.setGravity(new Vector2(0d, 0d));
    for (WorldObject worldObject : worldObjects) {
      worldObject.addTo(world);
    }
    //attach vc to ground
    vc.translate(new Vector2(-boundingBox[0].x + 1d, (boundingBox[1].y - boundingBox[0].y + 2d * WALL_MARGIN) / 2d - 1d));
    for (int y = 0; y < vc.getVoxels().getH(); y++) {
      for (int i : new int[]{0, 3}) {
        WeldJoint joint = new WeldJoint(
                ground.getBodies().get(0),
                vc.getVoxels().get(0, y).getVertexBodies()[i],
                vc.getVoxels().get(0, y).getVertexBodies()[i].getWorldCenter()
        );
        world.addJoint(joint);
      }
    }
    //prepare data
    List<Double> ys = new ArrayList<>((int) Math.round(maxT / settings.getStepFrequency()));
    List<Double> realTs = new ArrayList<>((int) Math.round(maxT / settings.getStepFrequency()));
    List<Double> simTs = new ArrayList<>((int) Math.round(maxT / settings.getStepFrequency()));
    double y0 = vc.getVoxels().get(vc.getVoxels().getW() - 1, vc.getVoxels().getH() / 2).getCenter().y;
    //simulate
    Stopwatch stopwatch = Stopwatch.createStarted();
    double t = 0d;
    while (t < maxT) {
      //add force
      if (t <= forceDuration) {
        for (int y = 0; y < vc.getVoxels().getH(); y++) {
          for (int i : new int[]{1, 2}) {
            vc.getVoxels().get(vc.getVoxels().getW() - 1, y).getVertexBodies()[i].applyForce(new Vector2(0d, -force / 2d));
          }
        }
      }
      //do step
      t = t + settings.getStepFrequency();
      world.step(1);
      if (listener != null) {
        Snapshot snapshot = new Snapshot(t, worldObjects.stream().map(WorldObject::getSnapshot).collect(Collectors.toList()));
        listener.listen(snapshot);
      }
      //get position
      double y = vc.getVoxels().get(vc.getVoxels().getW() - 1, vc.getVoxels().getH() / 2).getCenter().y;
      ys.add(y - y0);
      realTs.add((double) stopwatch.elapsed(TimeUnit.MICROSECONDS) / 1000000d);
      simTs.add(t);
    }
    stopwatch.stop();
    //compute things
    int dampingIndex = ys.size() - 2;
    while (dampingIndex > 0) {
      if (Math.abs(ys.get(dampingIndex) - ys.get(dampingIndex + 1)) > epsilon) {
        break;
      }
      dampingIndex--;
    }
    double dampingTime = Double.POSITIVE_INFINITY;
    if (dampingIndex < ys.size() - 1) {
      dampingTime = simTs.get(dampingIndex);
    }
    double elapsedSeconds = (double) stopwatch.elapsed(TimeUnit.MICROSECONDS) / 1000000d;
    double voxelCalcsPerSecond = (double) vc.getVoxels().count(v -> v != null) * (double) dampingIndex / realTs.get(dampingIndex);
    //fill
    Map<String, List<Double>> timeEvolution = new LinkedHashMap<>();
    timeEvolution.put("st", simTs);
    timeEvolution.put("rt", realTs);
    timeEvolution.put("y", ys);
    List<Point2> finalTopPositions = new ArrayList<>();
    for (int x = 0; x < vc.getVoxels().getW(); x++) {
      Vector2 center = vc.getVoxels().get(x, 0).getCenter();
      finalTopPositions.add(new Point2(center.x, center.y - y0));
    }
    return new Result(
            elapsedSeconds,
            realTs.get(dampingIndex),
            simTs.get(dampingIndex),
            realTs.size(),
            dampingIndex,
            (double) vc.getVoxels().count(v -> v != null) * (double) dampingIndex / realTs.get(dampingIndex),
            (double) dampingIndex / realTs.get(dampingIndex),
            (double) vc.getVoxels().count(v -> v != null) * (double) realTs.size() / elapsedSeconds,
            (double) realTs.size() / elapsedSeconds,
            finalTopPositions.get(finalTopPositions.size() - 1).y,
            timeEvolution,
            finalTopPositions
    );
  }

  public static void main(String[] args) {
    ExecutorService executor = Executors.newFixedThreadPool(3);
    double[] stepFrequencies = new double[]{0.005, 0.01}; //0.015, 0.02, 0.025};
    List<Future<Map<String, Object>>> futures = new ArrayList<>();
    for (double stepFrequency : stepFrequencies) {
      final Map<String, Object> staticKeys = new LinkedHashMap<>();
      staticKeys.put("stepFrequency", stepFrequency);
      futures.add(executor.submit(() -> {
        System.out.printf("Started\t%s%n", staticKeys);
        Settings settings = new Settings();
        settings.setStepFrequency(stepFrequency);
        CantileverBending cb = new CantileverBending(250d, 0d, 10d, 0.01d, settings, null);
        Result result = cb.apply(Grid.create(10, 4, Voxel.Builder.create()));
        System.out.printf("Ended\t%s%n", staticKeys);
        Map<String, Object> row = new LinkedHashMap<>();
        row.putAll(staticKeys);
        row.put("realTime", result.getRealTime());
        row.put("steps", result.getSteps());
        row.put("dampingSteps", result.getDampingSteps());
        row.put("dampingSimTime", result.getDampingSimTime());
        row.put("dampingRealTime", result.getDampingRealTime());
        row.put("minStepPerSecond", result.getMinStepPerSecond());
        row.put("minVoxelStepPerSecond", result.getMinVoxelStepPerSecond());
        row.put("overallStepPerSecond", result.getOverallStepPerSecond());
        row.put("overallVoxelStepPerSecond", result.getOverallVoxelStepPerSecond());
        row.put("yDisplacement", result.getYDisplacement());
        return row;
      }));
    }
    List<Map<String, Object>> rows = futures.stream().map(f -> {
      try {
        return f.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
      return null;
    }).collect(Collectors.toList());
    CSVWriter.write(CSVWriter.Table.create(rows), System.out);
    executor.shutdown();
  }

}
