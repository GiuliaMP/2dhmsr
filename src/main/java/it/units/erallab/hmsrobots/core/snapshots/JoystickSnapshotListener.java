package it.units.erallab.hmsrobots.core.snapshots;

import it.units.erallab.hmsrobots.core.controllers.BasicInteractiveController;
import it.units.erallab.hmsrobots.viewers.FramesImageBuilder;
import it.units.erallab.hmsrobots.viewers.drawers.Drawer;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import org.apache.commons.lang3.time.StopWatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class JoystickSnapshotListener extends JFrame implements SnapshotListener {
    private final Drawer drawer;
    private final double dT;
    private final BasicInteractiveController controller;

    private final Canvas canvas;
    private StopWatch stopWatch;
    private double lastDrawT;

    private List<Boolean> isKeyPressed;

    // Ottimizzazione: FrameT che indica ogni quanti frame vogliamo disegnare

    private final static int FRAME_RATE = 30;
    private static final Logger L = Logger.getLogger(FramesImageBuilder.class.getName());
    private final static int INIT_WIN_WIDTH = 400;
    private final static int INIT_WIN_HEIGHT = 300;


    public JoystickSnapshotListener(double dT, Drawer drawer, BasicInteractiveController controller) {
        this.dT = dT;
        this.drawer = drawer;
        this.controller = controller;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dimension = new Dimension(INIT_WIN_WIDTH, INIT_WIN_HEIGHT);
        canvas = new Canvas();
        canvas.setPreferredSize(dimension);
        canvas.setMinimumSize(dimension);
        canvas.setMaximumSize(dimension);
        getContentPane().add(canvas, BorderLayout.CENTER);
        //pack
        pack();
        setVisible(true);
        canvas.setIgnoreRepaint(true);
        canvas.createBufferStrategy(2);

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
    public void listen(double simT, Snapshot s) {

        if (stopWatch == null) {
            stopWatch = StopWatch.createStarted();
        }

        double realT = stopWatch.getTime(TimeUnit.MILLISECONDS) / 1000d;
        double frameDT = (1.0/FRAME_RATE);
        //System.out.println(simT + " " + (realT));

        if (lastDrawT == 0.0d || lastDrawT + frameDT <= realT){
            lastDrawT = realT;
            // Draw
            Graphics2D g = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
            g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());
            drawer.draw(simT, s, g);
            g.dispose();
            BufferStrategy strategy = canvas.getBufferStrategy();
            if (!strategy.contentsLost()) {
                strategy.show();
            }
            Toolkit.getDefaultToolkit().sync();
        }



        // Wait
        realT = stopWatch.getTime(TimeUnit.MILLISECONDS) / 1000d;
        long waitMillis = Math.max(Math.round((simT + dT - realT)*1000d), 0);
        if (waitMillis > 0) {
            synchronized (this) {
                //System.out.println(waitMillis);
                try {
                    wait(waitMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

