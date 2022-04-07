package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.WriteToFile;
import it.units.erallab.hmsrobots.core.snapshots.Snapshot;
import it.units.erallab.hmsrobots.core.snapshots.SnapshotListener;
import it.units.erallab.hmsrobots.viewers.DrawingUtils;
import it.units.erallab.hmsrobots.viewers.FramesImageBuilder;
import it.units.erallab.hmsrobots.viewers.drawers.Drawer;
import org.apache.commons.lang3.time.StopWatch;
import org.dyn4j.geometry.Circle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class InteractiveSnapshotListener implements SnapshotListener {
    private final double dT;
    private final BasicInteractiveController controller;
    private DevicePoller devicePoller;
    private final CanvasManager canvasManager;

    private StopWatch stopWatch;
    private double lastDrawT;

    //private List<List<Boolean>> flagHistory;
    private SortedMap<Double, List<Boolean>> flagHistory;

    private int totalTime;
    private boolean provaFlag;

    // Ottimizzazione: FrameT che indica ogni quanti frame vogliamo disegnare

    private final static int FRAME_RATE = 30;
    private static final Logger L = Logger.getLogger(FramesImageBuilder.class.getName());
    private final static int INIT_WIN_WIDTH = 600;
    private final static int INIT_WIN_HEIGHT = 500;


    public InteractiveSnapshotListener(double dT, CanvasManager canvasManager,
                                       DevicePoller devicePoller,
                                       BasicInteractiveController controller,
                                       int totalTime,
                                       boolean provaFlag) {
        this.dT = dT;
        this.canvasManager = canvasManager;
        this.controller = controller;
        this.devicePoller = devicePoller;

        this.totalTime = totalTime;
        this.provaFlag = provaFlag;

        this.flagHistory = new TreeMap<>();

        devicePoller.start(controller, canvasManager);
    }

    @Override
    public void listen(double simT, Snapshot s) {

        if (stopWatch == null) {
            stopWatch = StopWatch.createStarted();
        }

        double realT = stopWatch.getTime(TimeUnit.MILLISECONDS) / 1000d;
        double frameDT = (1.0/FRAME_RATE);
        //System.out.println(simT + " " + (realT));

        Drawer drawer = canvasManager.getDrawer();
        Canvas canvas = canvasManager.getCanvas();

        if (lastDrawT == 0.0d || lastDrawT + frameDT <= realT){
            devicePoller.setEnabledFlag(realT < 3);
            lastDrawT = realT;
            // Draw
            //Drawer drawer = canvasManager.getDrawer();
            //Canvas canvas = canvasManager.getCanvas();
            Graphics2D g = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
            g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());
            drawer.draw(simT, s, g);

            // Useful informations for the user
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));

            g.setColor(DrawingUtils.alphaed(Color.MAGENTA,0.9f));
            String timerString = ""+(totalTime-(int)simT);
            g.drawString(timerString,
                    g.getClipBounds().x + g.getClipBounds().width - 1 - g.getFontMetrics().stringWidth(timerString)-30,
                    g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent());

            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
            String provaString = provaFlag? "Training":"Do your best now";
            g.drawString(provaString,
                    g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(provaString) / 2,
                    g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent());

            /*int w = canvas.getSize().width;
            int h = canvas.getSize().height;
            Ellipse2D e = new Ellipse2D.Float(w/2.0f,h/2.0f,
                    w/1.0f,h/1.0f);
            g.draw(e);*/

            g.dispose();
            BufferStrategy strategy = canvas.getBufferStrategy();
            if (!strategy.contentsLost()) {
                strategy.show();
            }
            Toolkit.getDefaultToolkit().sync();

            List<Boolean> flags = new ArrayList<>(controller.getFlags());
            flagHistory.putIfAbsent(simT, flags);
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

