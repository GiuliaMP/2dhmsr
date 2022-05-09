/*
 * Copyright (C) 2022 Giorgia Nadizar <giorgia.nadizar@gmail.com> (as Giorgia Nadizar)
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

package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;
import it.units.erallab.hmsrobots.core.objects.Voxel;
import it.units.erallab.hmsrobots.util.Grid;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class PropagationController extends AbstractController {

  private final double propagationTime;
  // TODO might remove the field (if we don't need to get it)
  private final double propagationLag;
  private final Function<Double, Double> wave;

  private double propagationStartTime;
  private boolean active;

  public PropagationController(double propagationTime, double propagationLag) {
    this.propagationTime = propagationTime;
    this.propagationLag = propagationLag;
    // might move to a private utility method
    wave = x -> {
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
    if (active && t > propagationTime + propagationStartTime) {
      propagationStartTime = t;
      active = false;
    }

    double deltaT = t - propagationStartTime;
    double innerDeltaT = propagationTime / voxels.getW();
    List<Double> contractionLevels = IntStream.range(0, voxels.getW())
        .mapToObj(i -> wave.apply(deltaT - i * innerDeltaT)).toList();
    Grid<Double> aGrid = Grid.create(voxels.getW(), voxels.getH(), (x, y) -> {
      if (t - propagationStartTime > propagationTime) {
        return 0d;
      }
      double p = (t - propagationStartTime) / propagationTime;
      return Math.sin(-(double) x / (double) voxels.getW() * 2 * Math.PI + p * Math.PI);
    });
    //return Grid.create(voxels.getW(), voxels.getH(), (x, y) -> contractionLevels.get(x));
    return aGrid;
  }

  @Override
  public void reset() {
    // TODO startTime should be -progDelay or something like that
    propagationStartTime = -10;
    active = false;
  }

  public void triggerPropagation() {
    // TODO handle cases where you trigger it but there's an ongoing propagation
    // TODO List of times? Prevent it? Handle here or in computeSignals
    active = true;
    System.out.println("triggered");
  }

  public boolean getActive() {
    return active;
  }

}
