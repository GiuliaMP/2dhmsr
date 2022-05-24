package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;
import net.java.games.input.*;

import java.util.HashMap;
import java.util.Map;

public class JoystickPoller implements DevicePoller {

  private Map<RobotAreas, Boolean> keyPressed;


  public JoystickPoller() {
    //System.setProperty("net.java.games.input.useDefaultPlugin", "false");

    keyPressed = new HashMap<>();
    keyPressed.put(DevicePoller.RobotAreas.UP, false);
    keyPressed.put(DevicePoller.RobotAreas.DOWN, false);
    keyPressed.put(DevicePoller.RobotAreas.LEFT, false);
    keyPressed.put(DevicePoller.RobotAreas.RIGHT, false);
    keyPressed.put(DevicePoller.RobotAreas.IMPULSE, false);
  }

  @Override
  public Map<DevicePoller.RobotAreas, Boolean> getKeyPressed() {
    return keyPressed;
  }

  @Override
  public void start(AbstractController controller, CanvasManager canvasManager) {

    new Thread(new Runnable() {
      public void run() {

        while (true) {

          Controller[] controllers = ControllerEnvironment
              .getDefaultEnvironment().getControllers();

          for (int i = 0; i < controllers.length; i++) {
            /* Remember to poll each one */
            controllers[i].poll();

            /* Get the controllers event queue */
            EventQueue queue = controllers[i].getEventQueue();

            /* Create an event object for the underlying plugin to populate */
            Event event = new Event();

            /* For each object in the queue */
            while (queue.getNextEvent(event)) {

              Component component = event.getComponent();

              double value = event.getValue();
              if (!component.isAnalog()) { // Pulsantini
                switch (component.getIdentifier().toString()) {
                  //case "1": // Cerchio
                  case "5": //R2
                    keyPressed.replace(RobotAreas.RIGHT, (value == 1.0));
                    break;
                  //case "2":// X
                  case "7": //R1
                    keyPressed.replace(RobotAreas.DOWN, (value == 1.0));
                    break;
                  //case "3": // Quadrato
                  case "4": //L2
                    keyPressed.replace(RobotAreas.LEFT, (value == 1.0));
                    break;
                  //case "0": // Triangolo
                  case "6": //L1
                    keyPressed.replace(RobotAreas.UP, (value == 1.0));
                    break;
                  default:
                    break;
                }
              } /*else { // Levetta destra PS4
                System.out.println(component.toString());
                // input from analog-sticks and back triggers
                switch (component.getIdentifier().getName()) {
                  case "z":
                    if (value > 0.8) {
                      controller.setKeyPressed(true, 3);
                    } else if (value < -0.8) {
                      controller.setKeyPressed(true, 0);
                    } else {
                      if (!isKeyPressed.get(3)) {
                        controller.setKeyPressed(false, 3);
                      }
                      if (!isKeyPressed.get(0)) {
                        controller.setKeyPressed(false, 0);
                      }
                    }
                    break;
                  case "rz":
                    if (value > 0.8) {
                      controller.setKeyPressed(true, 1);
                    } else if (value < -0.8) {
                      controller.setKeyPressed(true, 2);
                    } else {
                      if (!isKeyPressed.get(1)) {
                        controller.setKeyPressed(false, 1);
                      }
                      if (!isKeyPressed.get(2)) {
                        controller.setKeyPressed(false, 2);
                      }
                    }
                    break;
                }
              } else { // Levetta destra
                // input from analog-sticks and back triggers
                System.out.println(component.getIdentifier().getName());
                System.out.println(value);
                switch (component.getIdentifier().getName()) {
                  case "unknown":
                    if (value > 0.8) {
                      //controller.setKeyPressed(true, 3);
                    } else if (value < -0.8) {
                      //controller.setKeyPressed(true, 0);
                    } else {
                      if (!isKeyPressed.get(3)) {
                        //controller.setKeyPressed(false, 3);
                      }
                      if (!isKeyPressed.get(0)) {
                        //controller.setKeyPressed(false, 0);
                      }
                    }
                    break;
                  case "rz":
                    if (value > 0.8) {
                      //controller.setKeyPressed(true, 1);
                    } else if (value < -0.8) {
                      //controller.setKeyPressed(true, 2);
                    } else {
                      if (!isKeyPressed.get(1)) {
                        //controller.setKeyPressed(false, 1);
                      }
                      if (!isKeyPressed.get(2)) {
                        //controller.setKeyPressed(false, 2);
                      }
                    }
                    break;
                }
              }*/
            }
          }

          try {
            Thread.sleep(20);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }


          if (controllers.length == 0) {
            System.out.println("Found no controllers.");
            System.exit(0);
          }
        }
      }
    }).start();
  }
}

