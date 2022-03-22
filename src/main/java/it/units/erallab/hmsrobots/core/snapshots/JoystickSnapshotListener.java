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

        new Thread(new Runnable() {
            public void run() {

                List<Boolean> isKeyPressed = new ArrayList<>(Collections.nCopies(4, false));
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

                            Component comp = event.getComponent();

                            if (comp.isAnalog()) {
                                // input from analog-sticks and back triggers
                                double value = event.getValue();
                                if (value > 0.8) {
                                    // positive direction
                                    switch (comp.getIdentifier().getName()) {
                                        case "z":
                                            controller.setKeyPressed(!isKeyPressed.get(3), 3);
                                            isKeyPressed.set(3, !isKeyPressed.get(3));
                                            break;
                                        case "rz":
                                            controller.setKeyPressed(!isKeyPressed.get(1), 1);
                                            isKeyPressed.set(1, !isKeyPressed.get(1));
                                            break;
                                    }
                                } else if (value < -0.8) {
                                    // negative direction
                                    switch (comp.getIdentifier().getName()) {
                                        case "z":
                                            controller.setKeyPressed(!isKeyPressed.get(0), 0);
                                            isKeyPressed.set(0, !isKeyPressed.get(0));
                                            break;
                                        case "rz":
                                            controller.setKeyPressed(!isKeyPressed.get(2), 2);
                                            isKeyPressed.set(2, !isKeyPressed.get(2));
                                            break;
                                    }
                                }
                            } else {
                                switch (comp.getIdentifier().toString()) {
                                    case "1": //X
                                        controller.setKeyPressed(!isKeyPressed.get(1), 1);
                                        isKeyPressed.set(1, !isKeyPressed.get(1));
                                        break;
                                    case "2":// Cerchio
                                        controller.setKeyPressed(!isKeyPressed.get(3), 3);
                                        isKeyPressed.set(3, !isKeyPressed.get(3));
                                        break;
                                    case "3": //Triangolo
                                        controller.setKeyPressed(!isKeyPressed.get(2), 2);
                                        isKeyPressed.set(2, !isKeyPressed.get(2));
                                        break;
                                    case "0": // Quadrato
                                        controller.setKeyPressed(!isKeyPressed.get(0), 0);
                                        isKeyPressed.set(0, !isKeyPressed.get(0));
                                        break;
                                    default:
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

        /*// Read the joystick events
        // Si bugga sicuramente perché manca il "release"
        while (true) {
            /* Get the available controllers
            Controller[] controllers = ControllerEnvironment
                    .getDefaultEnvironment().getControllers();
            if (controllers.length == 0) {
                System.out.println("Found no controllers.");
                System.exit(0);
            }

            for (int i = 0; i < controllers.length; i++) {
                /* Remember to poll each one
                controllers[i].poll();

                /* Get the controllers event queue
                net.java.games.input.EventQueue queue = controllers[i].getEventQueue();

                /* Create an event object for the underlying plugin to populate
                net.java.games.input.Event event = new net.java.games.input.Event();

                /* For each object in the queue
                while (queue.getNextEvent(event)) {

                    Component comp = event.getComponent();
                    //System.out.println(comp.getIdentifier());
                    switch (comp.getIdentifier().toString()) {
                        case "1": //X
                            controller.setKeyPressed(true, 1);
                            break;
                        case "2":// Cerchio
                            controller.setKeyPressed(true, 3);
                            break;
                        case "3": //Triangolo
                            controller.setKeyPressed(true, 2);
                            break;
                        case "0": // Quadrato
                            controller.setKeyPressed(true, 0);
                            break;
                        default:
                            //System.out.println(comp.getName());
                            break;
                    }
                }
            }

            /*
             * Sleep for 20 milliseconds, in here only so the example doesn't
             * thrash the system.
             *
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }*/
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


    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
            case KeyEvent.VK_UP:
                controller.setKeyPressed(true, 2);
                break;
            case KeyEvent.VK_DOWN:
                controller.setKeyPressed(true, 1);
                break;
            case KeyEvent.VK_LEFT:
                controller.setKeyPressed(true, 0);
                break;
            case KeyEvent.VK_RIGHT:
                controller.setKeyPressed(true, 3);
                break;
            default:
                System.out.println("key pressed: not an arrow");
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()){
            case KeyEvent.VK_UP:
                controller.setKeyPressed(false, 2);
                break;
            case KeyEvent.VK_DOWN:
                controller.setKeyPressed(false, 1);
                break;
            case KeyEvent.VK_LEFT:
                controller.setKeyPressed(false, 0);
                break;
            case KeyEvent.VK_RIGHT:
                controller.setKeyPressed(false, 3);
                break;
            default:
                System.out.println("key released: not an arrow");
                break;
        }
    }
}

