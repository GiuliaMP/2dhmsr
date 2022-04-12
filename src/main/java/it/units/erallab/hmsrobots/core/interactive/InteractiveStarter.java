package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.interactive.*;

public class InteractiveStarter {

    public static void main(String[] args) {
        String name = args[0];
        String robotType = args[1];
        String device = args[2];
        String writeToFile = args[3];
        String withoutTraining = args[4];

        new RunManager(name, robotType,device, writeToFile, withoutTraining);
    }
}

