package it.units.erallab.hmsrobots;

import it.units.erallab.hmsrobots.core.controllers.*;
import it.units.erallab.hmsrobots.core.interactive.BasicInteractiveController;
import it.units.erallab.hmsrobots.core.interactive.DevicePoller;
import it.units.erallab.hmsrobots.core.interactive.InteractiveSnapshotListener;
import it.units.erallab.hmsrobots.core.interactive.KeyboardPoller;
import it.units.erallab.hmsrobots.core.objects.Robot;
import it.units.erallab.hmsrobots.tasks.locomotion.Locomotion;
import it.units.erallab.hmsrobots.tasks.locomotion.Outcome;
import it.units.erallab.hmsrobots.util.Grid;
import it.units.erallab.hmsrobots.util.RobotUtils;
import it.units.erallab.hmsrobots.viewers.drawers.Drawers;
import org.dyn4j.dynamics.Settings;

import java.io.File;
import java.util.List;
import java.util.SortedMap;

public class InteractiveStarter {

    public static void main(String[] args) {
        multiped();
    }

    private static void multiped() {
        double f = 1d;
        Grid<Boolean> body = RobotUtils.buildShape("biped-8x4");
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
        InteractiveSnapshotListener interactiveSnapshotListener = new InteractiveSnapshotListener(1d / 60d, Drawers.basic(), devicePoller, basicInteractiveController);
        Outcome out = locomotion.apply(robot, interactiveSnapshotListener);
        SortedMap<Double, Outcome.Observation> observationsHistory = out.getObservations();
        SortedMap<Double, List<Boolean>> flagsHistory = interactiveSnapshotListener.getFlagHistory();
        System.out.println(flagsHistory);

        File file = new File("DatiUtili.csv");
        WriteToFile.toFile(file, observationsHistory, flagsHistory);
    }
}

