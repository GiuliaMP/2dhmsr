package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.WriteToFile;
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
    private final CanvasManager canvasManager;

    public RunManager(String name, String robotType, String device) {
        this.name = name;
        this.robotType = robotType;
        this.device = device;
        this.canvasManager = new CanvasManager(
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
        doSession(5+3, true, name, robotType, device);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        doSession(10+3, false, name, robotType, device);
    }

    private void doSession(int totalTime, boolean provaFlag, String fileName, String robotType, String device) {
        double f = 1d;
        Grid<Boolean> body = RobotUtils.buildShape(robotType.equals("Multiped") ? "biped-8x4" : "worm-8x2");
        BasicInteractiveController basicInteractiveController = new BasicInteractiveController();
        Robot robot = new Robot(
                new SmoothedController(basicInteractiveController, 3),
                RobotUtils.buildSensorizingFunction("uniform-a-0.01").apply(body)
        );
        Locomotion locomotion = new Locomotion(totalTime,
                Locomotion.createTerrain("hilly-0.5-10-0"),
                new Settings()
        );

        DevicePoller devicePoller = (device.equals("Keyboard")) ?
                new KeyboardPoller(basicInteractiveController) :
                new JoystickPoller(basicInteractiveController);
        InteractiveSnapshotListener interactiveSnapshotListener = new InteractiveSnapshotListener(1d / 60d,
                canvasManager, devicePoller, basicInteractiveController, totalTime, provaFlag);
        Outcome out = locomotion.apply(robot, interactiveSnapshotListener);
        if (!provaFlag) {
            SortedMap<Double, Outcome.Observation> observationsHistory = out.getObservations();
            SortedMap<Double, List<Boolean>> flagsHistory = interactiveSnapshotListener.getFlagHistory();
            File file = new File("Dati" + fileName + ".csv");
            WriteToFile.toFile(file, observationsHistory, flagsHistory);
        }
    }

    private void plainWorm(int totalTime, boolean provaFlag, String fileName, String device) {
        double f = 1d;
        Grid<Boolean> body = RobotUtils.buildShape("worm-8x2");
        BasicInteractiveController basicInteractiveController = new BasicInteractiveController();
        Robot robot = new Robot(
                new SmoothedController(basicInteractiveController, 3),
                RobotUtils.buildSensorizingFunction("uniform-a-0.01").apply(body)
        );
        Locomotion locomotion = new Locomotion(5,
                Locomotion.createTerrain("hilly-0.5-10-0"),
                new Settings()
        );

        //DevicePoller devicePoller = new JoystickSnapshotListener(basicInteractiveController);
        DevicePoller devicePoller = new KeyboardPoller(basicInteractiveController);
        InteractiveSnapshotListener interactiveSnapshotListener = new InteractiveSnapshotListener(1d / 60d,
                canvasManager, devicePoller, basicInteractiveController, totalTime, provaFlag);
        Outcome out = locomotion.apply(robot, interactiveSnapshotListener);
        if (!provaFlag) {
            SortedMap<Double, Outcome.Observation> observationsHistory = out.getObservations();
            SortedMap<Double, List<Boolean>> flagsHistory = interactiveSnapshotListener.getFlagHistory();
            File file = new File("Dati" + fileName + ".csv");
            WriteToFile.toFile(file, observationsHistory, flagsHistory);
        }
    }


}
