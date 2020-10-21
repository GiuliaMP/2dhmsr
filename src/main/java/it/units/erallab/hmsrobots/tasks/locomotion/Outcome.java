/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as Eric Medvet <eric.medvet@gmail.com>)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.units.erallab.hmsrobots.tasks.locomotion;

import com.google.common.collect.Range;
import it.units.erallab.hmsrobots.util.Grid;
import it.units.erallab.hmsrobots.util.Point2;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Outcome {

  public static final double INITIAL_TRANSIENT_RATIO = 0.1d;
  public static final double FOOTPRINT_INTERVAL = 0.5d;
  public static final double GAIT_LONGEST_INTERVAL = 5d;
  public static final int NUM_OF_FREQS = 4;

  private final double distance;
  private final double time;
  private final double robotLargestDim;
  private final double controlPower;
  private final double areaRatioPower;
  private final SortedMap<Double, Point2> centerTrajectory;
  private final SortedMap<Double, Footprint> footprints;
  private final SortedMap<Double, Grid<Boolean>> postures;

  public static class Gait {
    private final List<Footprint> footprints;
    private final double modeInterval;
    private final double coverage;
    private final double duration;
    private final double purity;

    public Gait(List<Footprint> footprints, double modeInterval, double coverage, double duration, double purity) {
      this.footprints = footprints;
      this.modeInterval = modeInterval;
      this.coverage = coverage;
      this.duration = duration;
      this.purity = purity;
    }

    public List<Footprint> getFootprints() {
      return footprints;
    }

    public double getModeInterval() {
      return modeInterval;
    }

    public double getCoverage() {
      return coverage;
    }

    public double getDuration() {
      return duration;
    }

    public double getPurity() {
      return purity;
    }

    public int getNOfUniqueFootprints() {
      return Set.of(footprints).size();
    }

    public double getAvgTouchArea() {
      return footprints.stream()
          .mapToDouble(f -> IntStream.range(0, f.length())
              .mapToDouble(i -> f.getMask()[i] ? 1d : 0d)
              .sum() / (double) f.length())
          .average()
          .orElse(0d);
    }

    @Override
    public String toString() {
      return "Gait{" +
          "footprints=" + footprints +
          ", modeInterval=" + modeInterval +
          ", coverage=" + coverage +
          ", duration=" + duration +
          ", purity=" + purity +
          '}';
    }
  }

  public Outcome(double distance, double time, double robotLargestDim, double controlPower, double areaRatioPower, SortedMap<Double, Point2> centerTrajectory, SortedMap<Double, Footprint> footprints, SortedMap<Double, Grid<Boolean>> postures) {
    this.distance = distance;
    this.time = time;
    this.robotLargestDim = robotLargestDim;
    this.controlPower = controlPower;
    this.areaRatioPower = areaRatioPower;
    this.centerTrajectory = centerTrajectory;
    this.footprints = footprints;
    this.postures = postures;
  }

  public double getDistance() {
    return distance;
  }

  public double getTime() {
    return time;
  }

  public double getRobotLargestDim() {
    return robotLargestDim;
  }

  public double getControlPower() {
    return controlPower;
  }

  public double getAreaRatioPower() {
    return areaRatioPower;
  }

  public SortedMap<Double, Point2> getCenterTrajectory() {
    return centerTrajectory;
  }

  public SortedMap<Double, Footprint> getFootprints() {
    return footprints;
  }

  public SortedMap<Double, Grid<Boolean>> getPostures() {
    return postures;
  }

  @Override
  public String toString() {
    return "Outcome{" +
        "distance=" + distance +
        ", time=" + time +
        ", robotLargestDim=" + robotLargestDim +
        ", controlPower=" + controlPower +
        ", areaRatioPower=" + areaRatioPower +
        '}';
  }

  public double getVelocity() {
    return distance / time;
  }

  public double getRelativeVelocity() {
    return getVelocity() / getRobotLargestDim();
  }

  public double getCorrectedEfficiency() {
    return distance / (1d + controlPower * time);
  }

  public Grid<Boolean> getAveragePosture() {
    return getAveragePosture(time * INITIAL_TRANSIENT_RATIO, time);
  }

  public Grid<Boolean> getAveragePosture(double startingT, double endingT) {
    return Grid.create(
        postures.get(postures.firstKey()).getW(),
        postures.get(postures.firstKey()).getH(),
        (x, y) -> postures.subMap(startingT, endingT).values().stream()
            .mapToDouble(m -> m.get(x, y) ? 1d : 0d)
            .average()
            .orElse(0d) > 0.5d
    );
  }

  public SortedMap<Double, Footprint> getQuantizedFootprints(double startingT, double endingT, double interval) {
    SortedMap<Double, Footprint> quantized = new TreeMap<>();
    int n = footprints.get(footprints.firstKey()).length();
    for (double t = startingT; t <= endingT; t = t + interval) {
      SortedMap<Double, Footprint> local = footprints.subMap(t, t + interval);
      double[] counts = new double[n];
      double tot = local.size();
      for (int x = 0; x < n; x++) {
        final int finalX = x;
        counts[x] = local.values().stream().mapToDouble(f -> f.getMask()[finalX] ? 1d : 0d).sum();
      }
      boolean[] localFootprint = new boolean[n];
      for (int x = 0; x < n; x++) {
        if (counts[x] > tot / 2d) {
          localFootprint[x] = true;
        }
      }
      quantized.put(t, new Footprint(localFootprint));
    }
    return quantized;
  }

  public Gait getMainGait() {
    List<Gait> gaits = computeGaits(
        getQuantizedFootprints(time * INITIAL_TRANSIENT_RATIO, time, FOOTPRINT_INTERVAL),
        2,
        (int) Math.round(GAIT_LONGEST_INTERVAL / FOOTPRINT_INTERVAL),
        FOOTPRINT_INTERVAL
    );
    if (gaits.isEmpty()) {
      return null;
    }
    gaits.sort(Comparator.comparingDouble(Gait::getDuration).reversed());
    return gaits.get(0);
  }

  private static List<Gait> computeGaits(SortedMap<Double, Footprint> footprints, int minSequenceLength, int maxSequenceLength, double interval) {
    //compute subsequences
    Map<List<Footprint>, List<Range<Double>>> sequences = new HashMap<>();
    List<Footprint> footprintList = new ArrayList<>(footprints.values());
    List<Range<Double>> ranges = footprints.keySet().stream().map(d -> Range.closedOpen(d, d + interval)).collect(Collectors.toList());
    for (int l = minSequenceLength; l <= maxSequenceLength; l++) {
      for (int i = l; i <= footprintList.size(); i++) {
        List<Footprint> sequence = footprintList.subList(i - l, i);
        List<Range<Double>> localRanges = sequences.getOrDefault(sequence, new ArrayList<>());
        localRanges.add(ranges.get(i - l));
        sequences.put(sequence, localRanges);
      }
    }
    //compute median interval
    List<Double> allIntervals = sequences.values().stream()
        .map(l -> IntStream.range(0, l.size() - 1)
            .mapToObj(i -> l.get(i + 1).lowerEndpoint() - l.get(i).lowerEndpoint())
            .collect(Collectors.toList())
        )
        .reduce((l1, l2) -> Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList()))
        .orElse(List.of());
    if (allIntervals.isEmpty()) {
      return List.of();
    }
    double modeInterval = mode(allIntervals);
    //compute gaits
    return sequences.entrySet().stream()
        .filter(e -> e.getValue().size() > 1)
        .map(e -> {
          List<Double> intervals = IntStream.range(0, e.getValue().size() - 1)
              .mapToObj(i -> e.getValue().get(i + 1).lowerEndpoint() - e.getValue().get(i).lowerEndpoint())
              .collect(Collectors.toList());
          List<Double> coverages = IntStream.range(0, intervals.size())
              .mapToObj(i -> (e.getValue().get(i).upperEndpoint() - e.getValue().get(i).lowerEndpoint()) / intervals.get(i))
                  .collect(Collectors.toList());
              double localModeInterval = mode(intervals);
              return new Gait(
                  e.getKey(),
                  localModeInterval,
                  coverages.stream().mapToDouble(d -> d).average().orElse(0d),
                  e.getValue().stream().mapToDouble(r -> r.upperEndpoint() - r.lowerEndpoint()).sum(),
                  (double) intervals.stream().filter(d -> d == localModeInterval).count() / (double) e.getValue().size()
              );
            }
        )
        .filter(g -> g.getModeInterval() == modeInterval)
        .collect(Collectors.toList());
  }

  private static <K> boolean areTheSame(List<K> seq1, List<K> seq2) {
    if (seq1.size() != seq2.size()) {
      return false;
    }
    for (int r = 0; r < seq2.size(); r++) {
      boolean same = true;
      for (int i = 0; i < seq1.size(); i++) {
        if (!seq1.get(i).equals(seq2.get((i + r) % seq2.size()))) {
          same = false;
          break;
        }
      }
      if (same) {
        return true;
      }
    }
    return false;
  }

  private static <K> K mode(Collection<K> collection) {
    return collection.stream()
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .max(Map.Entry.comparingByValue())
        .orElseThrow()
        .getKey();
  }

  public List<Double> getMainFrequencies() {
    return getMainFrequencies(time * INITIAL_TRANSIENT_RATIO, time, NUM_OF_FREQS);
  }

  public List<Double> getMainFrequencies(double startingT, double endingT, int n) {
    List<Double> vx = new ArrayList<>();
    List<Double> vy = new ArrayList<>();
    List<Double> times = new ArrayList<>(centerTrajectory.subMap(startingT, endingT).keySet());
    List<Point2> points = new ArrayList<>(centerTrajectory.subMap(startingT, endingT).values());
    List<Double> intervals = new ArrayList<>();
    for (int i = 0; i < points.size() - 1; i++) {
      vx.add(Math.abs(points.get(i + 1).x - points.get(i).x));
      vy.add(Math.abs(points.get(i + 1).y - points.get(i).y));
      intervals.add(times.get(i + 1) - times.get(i));
    }
    double avgInterval = intervals.stream().mapToDouble(d -> d).average().orElseThrow();
    //pad
    int paddedSize = (int) Math.pow(2d, Math.ceil(Math.log(vx.size()) / Math.log(2d)));
    if (paddedSize != vx.size()) {
      vx.addAll(Collections.nCopies(paddedSize - vx.size(), 0d));
      vy.addAll(Collections.nCopies(paddedSize - vy.size(), 0d));
    }
    //compute fft
    FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
    List<Double> fx = List.of(fft.transform(vx.stream()
        .mapToDouble(d -> d)
        .toArray(), TransformType.FORWARD)).stream()
        .map(Complex::abs)
        .collect(Collectors.toList())
        .subList(0, paddedSize / 2 + 1);
    List<Double> fy = List.of(fft.transform(vy.stream()
        .mapToDouble(d -> d)
        .toArray(), TransformType.FORWARD)).stream()
        .map(Complex::abs)
        .collect(Collectors.toList())
        .subList(0, paddedSize / 2 + 1);
    List<Double> frequencies = IntStream.range(0, fx.size())
        .mapToObj(i -> 1d / avgInterval / 2d * (double) i / (double) fx.size())
        .collect(Collectors.toList());
    List<Integer> indexes = IntStream.range(0, fx.size())
        .boxed()
        .collect(Collectors.toList());
    indexes.sort(Comparator.comparingDouble(fx::get).reversed());
    //build overall list
    List<Double> values = new ArrayList<>();
    values.addAll( //freqs x
        indexes.stream()
            .map(frequencies::get)
            .limit(n)
            .collect(Collectors.toList())
    );
    values.addAll( //strenghts x
        indexes.stream()
            .map(fx::get)
            .limit(n)
            .collect(Collectors.toList())
    );
    indexes.sort(Comparator.comparingDouble(fy::get).reversed());
    values.addAll( //freqs y
        indexes.stream()
            .map(frequencies::get)
            .limit(n)
            .collect(Collectors.toList())
    );
    values.addAll( //strenghts y
        indexes.stream()
            .map(fy::get)
            .limit(n)
            .collect(Collectors.toList())
    );
    return values;
  }

}
