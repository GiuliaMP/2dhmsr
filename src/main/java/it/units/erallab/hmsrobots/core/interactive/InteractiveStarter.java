package it.units.erallab.hmsrobots.core.interactive;

public class InteractiveStarter {

  public static void main(String[] args) {
    String name = args[0]; // NickName
    String robotType = args[1]; // Already the "correct" type
    String device = args[2]; // Keyboard or Joystick
    String division = args[3]; // 2 or 4
    String writeToFile = args[4]; // Take the data or not
    String withoutTraining = args[5]; // No training in case someone wants to do the session again

    new RunManager(name, robotType, device, division, writeToFile, withoutTraining);
  }
}

