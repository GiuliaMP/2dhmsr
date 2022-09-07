package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;
import it.units.erallab.hmsrobots.core.controllers.CentralizedSensing;
import it.units.erallab.hmsrobots.core.controllers.DiscreteActionsController;
import it.units.erallab.hmsrobots.core.controllers.TimedRealFunction;
import it.units.erallab.hmsrobots.core.interactive.*;
import it.units.erallab.hmsrobots.core.objects.Ground;
import it.units.erallab.hmsrobots.core.objects.Robot;
import it.units.erallab.hmsrobots.core.objects.Voxel;
import it.units.erallab.hmsrobots.tasks.locomotion.Locomotion;
import it.units.erallab.hmsrobots.tasks.locomotion.Outcome;
import it.units.erallab.hmsrobots.util.Grid;
import it.units.erallab.hmsrobots.util.RobotUtils;
import it.units.erallab.hmsrobots.viewers.AllRobotFollower;
import it.units.erallab.hmsrobots.viewers.GridOnlineViewer;
import it.units.erallab.hmsrobots.viewers.drawers.Drawer;
import it.units.erallab.hmsrobots.viewers.drawers.PolyDrawer;
import it.units.erallab.hmsrobots.viewers.drawers.SubtreeDrawer;
import it.units.erallab.hmsrobots.viewers.drawers.VoxelDrawer;
import org.dyn4j.dynamics.Settings;

import java.util.*;
import java.util.stream.Collectors;

public class DescreteControllerManager {

  //private final List<Boolean> robotAreasToContract;
  String division;
  private DevicePoller devicePoller;

  public DescreteControllerManager(String division, DevicePoller devicePoller) {
    this.division = division;
    this.devicePoller = devicePoller;
    int divisionInt = division.equals("4") ? 4 : 2;
  }

  private Grid<Boolean> getShape(Grid<Voxel> voxels) {
    Grid<Boolean> shape = Grid.create(voxels, Objects::isNull);
    for (var val : voxels) {
      shape.set(val.key().x(), val.key().y(), voxels.get(val.key().x(), val.key().y()) != null);
    }
    return shape;
  }


  enum RobotArea {
    LEFT,
    RIGHT,
    UP,
    DOWN
  }


  public static DiscreteActionsController.Action buildPoseAction(Grid<Boolean> shape, double deltaT, String division, RobotArea area) {
    return new DiscreteActionsController.Action() {
      @Override
      public Double apply(Double t, Grid.Key key) {
        //if (t<deltaT) {
        Set<Grid.Key> center = shape.stream()
            .filter(Grid.Entry::value)
            .map(Grid.Entry::key)
            .collect(Collectors.toSet());
        double midCenterY = center.stream().mapToDouble(Grid.Key::y).average().orElse(0d);
        if (area.equals(RobotArea.UP) && key.y() <= midCenterY) {
          return 1d;
        } else if (area.equals(RobotArea.DOWN) && key.y() > midCenterY){
          return 1d;
        } else { return -1d;}
        //} else {
        //return -1d;
        //}
      }
    };
  }

  public static double[] computeArrayBifunction(String division, DevicePoller devicePoller) {
    double[] out;
    Map<DevicePoller.RobotAreas, Boolean> keyPressed = new HashMap<DevicePoller.RobotAreas, Boolean>(devicePoller.getKeyPressed());
    if (division.equals("2ud")) {
      out = new double[3];
      out[0] = keyPressed.get(DevicePoller.RobotAreas.UP) ? 1d : -1d;
      out[1] = keyPressed.get(DevicePoller.RobotAreas.DOWN) ? 1d : -1d;
      out[2] = 0d;
    } else if (division.equals("4")) {
      out = new double[5];
      out[0] = keyPressed.get(DevicePoller.RobotAreas.LEFT) ? 1d : -1d;
      out[1] = keyPressed.get(DevicePoller.RobotAreas.DOWN) ? 1d : -1d;
      out[2] = keyPressed.get(DevicePoller.RobotAreas.UP) ? 1d : -1d;
      out[3] = keyPressed.get(DevicePoller.RobotAreas.RIGHT) ? 1d : -1d;
      out[4] = 0d;
    } else {
      out = new double[3];
      out[0] = keyPressed.get(DevicePoller.RobotAreas.LEFT) ? 1d : -1d;
      out[1] = keyPressed.get(DevicePoller.RobotAreas.RIGHT) ? 1d : -1d;
      out[2] = 0d;
    }
    return out;
  }

  public static void main(String[] args) {
    String division = "2";
    Grid<Boolean> shape = RobotUtils.buildShape("worm-4x2");

    Set<Grid.Key> center = shape.stream()
        .filter(Grid.Entry::value)
        .map(Grid.Entry::key)
        .collect(Collectors.toSet());
    double midCenterY = center.stream().mapToDouble(Grid.Key::y).average().orElse(0d);

    Grid<Voxel> body = RobotUtils.buildSensorizingFunction("spinedTouch-f-f-0").apply(shape);
    DevicePoller devicePoller = new KeyboardPoller();
    CanvasManager canvasManager = new CanvasManager(() ->
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
    devicePoller.start(null, canvasManager);
    List<DiscreteActionsController.Action> actionList;
    if (division.equals("2ud")) {
      DiscreteActionsController.Action aDown = (t, k) -> t > 0 ? 0 : (k.y() <= midCenterY ? 1d : -1d);
      DiscreteActionsController.Action aUp = (t, k) -> t > 0 ? 0 : (k.y() > midCenterY ? 1d : -1d);
      actionList = List.of(aUp, aDown);
    } else if (division.equals("4")) {
      //TODO: implement the actions of the robot divided in 4 parts
      DiscreteActionsController.Action aDown = (t, k) -> t > 0 ? 0 : (k.y() <= midCenterY ? 1d : -1d);
      DiscreteActionsController.Action aUp = (t, k) -> t > 0 ? 0 : (k.y() > midCenterY ? 1d : -1d);
      DiscreteActionsController.Action aLeft = (t, k) -> t > 0 ? 0 : (k.y() <= midCenterY ? 1d : -1d);
      DiscreteActionsController.Action aRight = (t, k) -> t > 0 ? 0 : (k.y() > midCenterY ? 1d : -1d);
      actionList = List.of(aUp, aDown, aLeft, aRight);
    } else {
      //TODO: implement left right actions
      DiscreteActionsController.Action aLeft = (t, k) -> t > 0 ? 0 : (k.y() <= midCenterY ? 1d : -1d);
      DiscreteActionsController.Action aRight = (t, k) -> t > 0 ? 0 : (k.y() > midCenterY ? 1d : -1d);
      actionList = List.of(aLeft, aRight);
    }
    TimedRealFunction f = TimedRealFunction.from(
        (t, in) -> computeArrayBifunction(division, devicePoller),//new double[]{1d, 1d},
        CentralizedSensing.nOfInputs(body),
        3
    );
    AbstractController controller = new DiscreteActionsController(
        RobotUtils.buildSensorizingFunction("spinedTouch-f-f-0").apply(RobotUtils.buildShape("worm-4x2")),
        actionList, // 2 o 4 azioni diverse
        f, // timedRealFunction, decide l'indice dell'azione da compiere sulla base al device poller ignorando gli input
        1,
        1d
    );
    Robot robot = new Robot(controller, body);
    Locomotion locomotion = new Locomotion(30, Locomotion.createTerrain("hilly-1-10-0"), new Settings());
    InteractiveSnapshotListener interactiveSnapshotListener = new InteractiveSnapshotListener(1d / 60d,
        canvasManager, devicePoller, controller, division, 30, true);
    //GridOnlineViewer.run(locomotion, robot);
    Outcome out = locomotion.apply(robot, interactiveSnapshotListener);
  }

}

