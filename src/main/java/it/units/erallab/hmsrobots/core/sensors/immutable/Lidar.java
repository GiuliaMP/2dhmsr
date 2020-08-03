/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as luca)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.erallab.hmsrobots.core.sensors.immutable;

import it.units.erallab.hmsrobots.core.sensors.Sensor;

public class Lidar extends SensorReading {

    private final double angle;
    private final double rayLength;
    private final double[] rayDirections;

    public Lidar(double[] values,
                 Sensor.Domain[] domains,
                 int sensorIndex,
                 int nOfSensors,
                 double angle,
                 double rayLength,
                 double[] rayDirections) {
        super(values, domains, sensorIndex, nOfSensors);
        this.angle = angle;
        this.rayLength = rayLength;
        this.rayDirections = rayDirections;
    }

    public double getAngle()  {
        return angle;
    }

    public double getRayLength()  {
        return rayLength;
    }

    public double[] getRayDirections()   {
        return rayDirections;
    }
}
