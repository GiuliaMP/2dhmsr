package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.SmoothedController;
import it.units.erallab.hmsrobots.core.objects.Ground;
import it.units.erallab.hmsrobots.core.objects.Robot;
import it.units.erallab.hmsrobots.tasks.locomotion.Locomotion;
import it.units.erallab.hmsrobots.tasks.locomotion.Outcome;
import it.units.erallab.hmsrobots.util.Grid;
import it.units.erallab.hmsrobots.util.RobotUtils;
import it.units.erallab.hmsrobots.viewers.AllRobotFollower;
import it.units.erallab.hmsrobots.viewers.drawers.Drawer;
import it.units.erallab.hmsrobots.viewers.drawers.PolyDrawer;
import it.units.erallab.hmsrobots.viewers.drawers.SubtreeDrawer;
import it.units.erallab.hmsrobots.viewers.drawers.VoxelDrawer;
import org.dyn4j.dynamics.Settings;

import java.io.File;
import java.util.List;
import java.util.SortedMap;

public class RunManager {

  private final String name;
  private final String robotType;
  private final String device;
  private final String division;
  private final boolean writeToFile;
  private final CanvasManager canvasManager;
  private final boolean withoutTraining;
  private final int iteration;

  public RunManager(String name, String robotType, String device, String division, String writeToFile, String withoutTraining, String iteration) {
    this.name = name;
    this.robotType = robotType;
    this.device = device;
    this.division = division;
    this.writeToFile = Boolean.parseBoolean(writeToFile);
    this.withoutTraining = Boolean.parseBoolean(withoutTraining);
    this.iteration = Integer.parseInt(iteration);
    this.canvasManager = new CanvasManager(() ->
        Drawer.of(
            Drawer.clear(),
            Drawer.transform(
                new AllRobotFollower(1.5d, 3),
                Drawer.of(
                    new PolyDrawer(PolyDrawer.TEXTURE_PAINT, SubtreeDrawer.Extractor.matches(null, Ground.class, null)),
                    new VoxelDrawer()
                )
            )
        ));

    run();
  }

  private void run() {
    String fileName = name + "_" + robotType+ "_" + division + "_" + device;
    Grid<Boolean> body = RobotUtils.buildShape(robotType);
    DevicePoller devicePoller = (device.equals("Keyboard")) ?
        new KeyboardPoller() :
        new JoystickPoller();
    if (!withoutTraining) {
      if (division.equals("Wave")) {
        waveSession(30 + 3, true, fileName, body, devicePoller, writeToFile);
      } else {
        if (iteration == 2) {
          doSession(30 + 3, true, fileName, body, devicePoller, division, writeToFile);
        } else {
          doSession(60 + 3, true, fileName, body, devicePoller, division, writeToFile);
        }
      }
    }
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
    if (division.equals("Wave")) {
      waveSession(30 + 3, false, fileName, body, devicePoller, writeToFile);
    } else {
      doSession(30 + 3, false, fileName, body, devicePoller, division, writeToFile);
    }
  }

  private void doSession(int totalTime, boolean trainingFlag, String fileName, Grid<Boolean> body, DevicePoller devicePoller, String division, boolean writeToFile) {
    BasicInteractiveController basicInteractiveController = new BasicInteractiveController(division, devicePoller);
    Robot robot = new Robot(
        new SmoothedController(basicInteractiveController, 5),
        RobotUtils.buildSensorizingFunction("uniform-a-0.01").apply(body)
    );
    Locomotion locomotion = new Locomotion(totalTime,
        //InteractiveTerrainManager.createTerrain("downhillHilly-0.5-10-0"),
        Locomotion.createTerrain("downhill-10"),
        new Settings()
    );

    canvasManager.rebuildDrawer();
    InteractiveSnapshotListener interactiveSnapshotListener = new InteractiveSnapshotListener(1d / 60d,
        canvasManager, devicePoller, basicInteractiveController, division, totalTime, trainingFlag);
    Outcome out = locomotion.apply(robot, interactiveSnapshotListener);

    if (!trainingFlag) {
      SortedMap<Double, Outcome.Observation> observationsHistory = out.getObservations();
      SortedMap<Double, List<Boolean>> flagsHistory = interactiveSnapshotListener.getFlagHistory();
      if (writeToFile) {
        File file = new File(fileName + ".csv");
        WriteToFile.toFile(file, observationsHistory, flagsHistory, division);
      }
    }
  }

  private void waveSession(int totalTime, boolean trainingFlag, String fileName, Grid<Boolean> body, DevicePoller devicePoller, boolean writeToFile) {
    //Grid<Boolean> body = RobotUtils.buildShape("free-10000-10001-11111-11111-10001-10000");
    PropagationController basicInteractiveController = new PropagationController(1d, .5d, devicePoller);
    Robot robot = new Robot(
        basicInteractiveController,
        RobotUtils.buildSensorizingFunction("uniform-a-0.01").apply(body)
    );
    Locomotion locomotion = new Locomotion(totalTime,
        //InteractiveTerrainManager.createTerrain("downhillHilly-0.5-10-0"),
        Locomotion.createTerrain("downhill-10"),
        new Settings()
    );

    canvasManager.rebuildDrawer();
    InteractiveSnapshotListener interactiveSnapshotListener = new InteractiveSnapshotListener(1d / 60d,
        canvasManager, devicePoller, basicInteractiveController, division, totalTime, trainingFlag);
    Outcome out = locomotion.apply(robot, interactiveSnapshotListener);
    if (!trainingFlag) {
      SortedMap<Double, Outcome.Observation> observationsHistory = out.getObservations();
      SortedMap<Double, List<Boolean>> flagsHistory = interactiveSnapshotListener.getFlagHistory();
      if (writeToFile) {
        File file = new File(fileName + ".csv");
        WriteToFile.toFile(file, observationsHistory, flagsHistory, division);
      }
    }
  }
}
