package it.units.erallab.hmsrobots.core.interactive;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

import java.util.ArrayList;
import java.util.List;

public class JoystickPoller implements DevicePoller {
    private final BasicInteractiveController controller;

    private List<Boolean> isKeyPressed;
    private boolean enabledFlag;


    public JoystickPoller(BasicInteractiveController controller) {
        this.controller = controller;
    }

    @Override
    public void start(BasicInteractiveController controller, CanvasManager canvasManager) {
        isKeyPressed = new ArrayList<>();
        for (int i = 0; i<4; i++) {
            isKeyPressed.add(false);
        }

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
                                    case "1": //X
                                        isKeyPressed.set(1, value == 1.0);
                                        controller.setKeyPressed(value == 1.0, 1);
                                        break;
                                    case "2":// Cerchio
                                        isKeyPressed.set(3, value == 1.0);
                                        controller.setKeyPressed(value == 1.0, 3);
                                        break;
                                    case "3": //Triangolo
                                        isKeyPressed.set(2, value == 1.0);
                                        controller.setKeyPressed(value == 1.0, 2);
                                        break;
                                    case "0": // Quadrato
                                        isKeyPressed.set(0, value == 1.0);
                                        controller.setKeyPressed(value == 1.0, 0);
                                        break;
                                    default:
                                        break;
                                }
                            } else { // Levetta destra
                                // input from analog-sticks and back triggers
                                switch (component.getIdentifier().getName()) {
                                    case "z":
                                        if (value > 0.8){
                                            controller.setKeyPressed(true, 3);
                                        } else if (value < -0.8){
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
                                        if (value > 0.8){
                                            controller.setKeyPressed(true, 1);
                                        } else if (value < -0.8){
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
                            }
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

    @Override
    public void setEnabledFlag(boolean flag) {
        enabledFlag = flag;
    }
}

