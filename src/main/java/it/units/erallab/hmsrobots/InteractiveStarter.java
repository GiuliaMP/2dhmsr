package it.units.erallab.hmsrobots;

import it.units.erallab.hmsrobots.core.interactive.*;

public class InteractiveStarter {

    public static void main(String[] args) {
        String name = args[0];
        String robotType = args[1];
        String device = args[2];

        RunManager manager = new RunManager(name, robotType,device);
    }
}

