package it.units.erallab.hmsrobots.core.interactive.generalizedcontroller;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;
import it.units.erallab.hmsrobots.core.interactive.DevicePoller;
import it.units.erallab.hmsrobots.core.objects.Voxel;
import it.units.erallab.hmsrobots.util.Grid;

import java.util.ArrayList;
import java.util.List;

public abstract class InteractiveController extends AbstractController {

  protected final List<Boolean> robotAreasToContract;
  protected String division;
  protected DevicePoller devicePoller;

  public InteractiveController(String division, DevicePoller devicePoller) {
    this.division = division;
    this.devicePoller = devicePoller;
    int divisionInt = division.equals("4") ? 4 : 2;

    robotAreasToContract = new ArrayList<>();
    for (int i = 0; i < divisionInt; i++) {
      robotAreasToContract.add(false);
    }
  }

  @Override
  public abstract Grid<Double> computeControlSignals(double t, Grid<Voxel> voxels);

  @Override
  public abstract void reset();

  protected void setRobotAreasToContract(boolean keyPressed, int index) {
    this.robotAreasToContract.set(index, keyPressed);
  }
}
