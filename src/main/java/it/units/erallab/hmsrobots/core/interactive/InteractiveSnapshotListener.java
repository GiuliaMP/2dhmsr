package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.WriteToFile;
import it.units.erallab.hmsrobots.core.snapshots.Snapshot;
import it.units.erallab.hmsrobots.core.snapshots.SnapshotListener;
import it.units.erallab.hmsrobots.viewers.FramesImageBuilder;
import it.units.erallab.hmsrobots.viewers.drawers.Drawer;
import org.apache.commons.lang3.time.StopWatch;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class InteractiveSnapshotListener extends JFrame implements SnapshotListener {
    private final Drawer drawer;
    private final double dT;
    private final BasicInteractiveController controller;

    private final Canvas canvas;
    private StopWatch stopWatch;
    private double lastDrawT;

    //private List<List<Boolean>> flagHistory;
    private SortedMap<Double, List<Boolean>> flagHistory;

    // Ottimizzazione: FrameT che indica ogni quanti frame vogliamo disegnare

    private final static int FRAME_RATE = 30;
    private static final Logger L = Logger.getLogger(FramesImageBuilder.class.getName());
    private final static int INIT_WIN_WIDTH = 400;
    private final static int INIT_WIN_HEIGHT = 300;


    public InteractiveSnapshotListener(double dT, Drawer drawer, DevicePoller devicePoller, BasicInteractiveController controller) {
        this.dT = dT;
        this.drawer = drawer;
        this.controller = controller;
        //this.devicePoller = devicePoller;

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

        this.flagHistory = new TreeMap<>();

        devicePoller.start(controller, this);
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

            flagHistory.put(realT, controller.getFlags());
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

    public SortedMap<Double, List<Boolean>> getFlagHistory() {
        return flagHistory;
    }
}

