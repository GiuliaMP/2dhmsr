package it.units.erallab.hmsrobots;

import it.units.erallab.hmsrobots.core.controllers.SmoothedController;
import it.units.erallab.hmsrobots.core.controllers.TimeFunctions;
import it.units.erallab.hmsrobots.core.interactive.*;
import it.units.erallab.hmsrobots.core.objects.Robot;
import it.units.erallab.hmsrobots.core.objects.Voxel;
import it.units.erallab.hmsrobots.tasks.locomotion.Locomotion;
import it.units.erallab.hmsrobots.tasks.locomotion.Outcome;
import it.units.erallab.hmsrobots.util.Grid;
import it.units.erallab.hmsrobots.util.RobotUtils;
import it.units.erallab.hmsrobots.util.SerializationUtils;
import it.units.erallab.hmsrobots.viewers.GridOnlineViewer;
import it.units.erallab.hmsrobots.viewers.NamedValue;
import it.units.erallab.hmsrobots.viewers.drawers.Drawers;
import org.dyn4j.dynamics.Settings;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;

public class InteractiveStarter {

    // Flag start sui poller che quando premo un tasto fa partire tutto
    // InteractiveSnapshotListener ascolta la flag start e disegna/fa partire le cose giuste al momento giusto

    //Suo costruttore che init tutto e extend JFrame e un run, il vero main la crea e basta
    // g con canale alpha
    public static void main(String[] args) { // Nome, robot, device
        String name = args[0];
        String robotType = args[1];
        String device = args[2];

        if (robotType.equals("Multiped")) {
            multiped(5, true, name, device);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            multiped(10, false, name, device);
        } else {
            plainWorm(5, true, name, device);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            plainWorm(10, false, name, device);
        }
    }

    private static void multiped(int totalTime, boolean provaFlag, String fileName, String device) {
        double f = 1d;
        Grid<Boolean> body = RobotUtils.buildShape("biped-8x4");
        BasicInteractiveController basicInteractiveController = new BasicInteractiveController();
        Robot robot = new Robot(
                new SmoothedController(basicInteractiveController, 3),
                RobotUtils.buildSensorizingFunction("uniform-a-0.01").apply(body)
        );
        Locomotion locomotion = new Locomotion(totalTime,
                Locomotion.createTerrain("hilly-0.5-10-0"),
                new Settings()
        );

        DevicePoller devicePoller = (device.equals("Keyboard"))?
                new KeyboardPoller(basicInteractiveController):
                new JoystickPoller(basicInteractiveController);
        InteractiveSnapshotListener interactiveSnapshotListener = new InteractiveSnapshotListener(1d / 60d,
                Drawers.basic(), devicePoller, basicInteractiveController, totalTime, provaFlag);
        Outcome out = locomotion.apply(robot, interactiveSnapshotListener);
        if (!provaFlag) {
            SortedMap<Double, Outcome.Observation> observationsHistory = out.getObservations();
            SortedMap<Double, List<Boolean>> flagsHistory = interactiveSnapshotListener.getFlagHistory();
            File file = new File("Dati"+fileName+".csv");
            WriteToFile.toFile(file, observationsHistory, flagsHistory);
        }
    }

    private static void plainWorm(int totalTime, boolean provaFlag, String fileName, String device) {
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
                Drawers.basic(), devicePoller, basicInteractiveController, totalTime, provaFlag);
        Outcome out = locomotion.apply(robot, interactiveSnapshotListener);
        if (!provaFlag) {
            SortedMap<Double, Outcome.Observation> observationsHistory = out.getObservations();
            SortedMap<Double, List<Boolean>> flagsHistory = interactiveSnapshotListener.getFlagHistory();
            File file = new File("Dati"+fileName+".csv");
            WriteToFile.toFile(file, observationsHistory, flagsHistory);
        }
    }
}

