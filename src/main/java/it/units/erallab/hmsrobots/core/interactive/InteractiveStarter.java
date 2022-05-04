package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.interactive.*;

public class InteractiveStarter {

    public static void main(String[] args) {
        String name = args[0];
        String robotType = args[1];
        String device = args[2];
        String division = args[3];
        String writeToFile = args[4];
        String withoutTraining = args[5];

        new RunManager(name, robotType,device, division, writeToFile, withoutTraining);
    }
}

