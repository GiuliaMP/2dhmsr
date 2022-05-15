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

import java.util.Map;
import java.util.function.Function;

public class PropagationController extends AbstractController {

  private final double propagationTime;

  private double propagationStartTimeRight;
  private double propagationStartTimeLeft;
  private boolean activeRight;
  private boolean activeLeft;

  private DevicePoller devicePoller;

  public PropagationController(double propagationTime, double propagationLag, DevicePoller devicePoller) {
    this.devicePoller = devicePoller;
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
    activeRight = keyPressed.get(DevicePoller.RobotAreas.RIGHT);
    activeLeft = keyPressed.get(DevicePoller.RobotAreas.LEFT);

    if (activeRight) {
      propagationStartTimeRight = t;
      activeRight = false;
    }

    if (activeLeft) {
      propagationStartTimeLeft = t;
      activeLeft = false;
    }

    Grid<Double> aGrid = Grid.create(voxels.getW(), voxels.getH(), (x, y) -> {
      double pRight = (t - propagationStartTimeRight) / propagationTime;
      double pLeft = (t - propagationStartTimeLeft) / propagationTime;
      double lag = (-voxels.getW()+.8);
      double waveRight = Math.sin(lag+(double) x / (double) voxels.getW() * Math.PI + pRight * Math.PI);
      double waveLeft = Math.sin(-(double) x / (double) voxels.getW() * Math.PI + pLeft * Math.PI);
      return waveRight * ((t - propagationStartTimeRight > propagationTime) ? 0 : 1)
          + waveLeft * ((t - propagationStartTimeLeft > propagationTime) ? 0 : 1);
    });
    return aGrid;
  }

  @Override
  public void reset() {
    propagationStartTimeRight = -10;
    propagationStartTimeLeft = -10;
    activeRight = false;
    activeLeft = false;
  }

}
