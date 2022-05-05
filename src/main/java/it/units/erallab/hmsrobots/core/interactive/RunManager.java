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
  private final int division;
  private final boolean writeToFile;
  private final CanvasManager canvasManager;
  private final boolean withoutTraining;

  public RunManager(String name, String robotType, String device, String division, String writeToFile, String withoutTraining) {
    this.name = name;
    this.robotType = robotType;
    this.device = device;
    this.division = Integer.parseInt(division);
    this.writeToFile = Boolean.parseBoolean(writeToFile);
    this.withoutTraining = Boolean.parseBoolean(withoutTraining);
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
    // Training = 60s
    // Play = 30s
    String fileName = name + robotType;
    //int totalTime, boolean trainingFlag, String fileName
    if (!withoutTraining) {
      doSession(60 + 3, true, fileName, robotType, device, division, writeToFile);
    }
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
    doSession(30 + 3, false, fileName, robotType, device, division, writeToFile);
  }

  // Metti un po' di discesa
  private void doSession(int totalTime, boolean trainingFlag, String fileName, String robotType, String device,int division, boolean writeToFile) {
    double f = 1d;
    Grid<Boolean> body = RobotUtils.buildShape(robotType.equals("Multiped") ? "biped-4x3" : "worm-8x2");
    //Grid<Boolean> body = RobotUtils.buildShape("free-10000-10001-11111-11111-10001-10000");
    BasicInteractiveController basicInteractiveController = new BasicInteractiveController(division);
    Robot robot = new Robot(
        new SmoothedController(basicInteractiveController, 3),
        RobotUtils.buildSensorizingFunction("uniform-a-0.01").apply(body)
    );
    Locomotion locomotion = new Locomotion(totalTime,
        InteractiveTerrainManager.createTerrain("downhillHilly-0.5-10-0"),
        new Settings()
    );

    DevicePoller devicePoller = (device.equals("Keyboard")) ?
        new KeyboardPoller(basicInteractiveController, division) :
        new JoystickPoller(basicInteractiveController, division);
    canvasManager.rebuildDrawer();
    InteractiveSnapshotListener interactiveSnapshotListener = new InteractiveSnapshotListener(1d / 60d,
        canvasManager, devicePoller, basicInteractiveController, totalTime, trainingFlag);
    Outcome out = locomotion.apply(robot, interactiveSnapshotListener);
    if (!trainingFlag) {
      SortedMap<Double, Outcome.Observation> observationsHistory = out.getObservations();
      SortedMap<Double, List<Boolean>> flagsHistory = interactiveSnapshotListener.getFlagHistory();
      if (writeToFile) {
        File file = new File("Dati" + fileName + ".csv");
        WriteToFile.toFile(file, observationsHistory, flagsHistory);
      }
    }
  }
}
