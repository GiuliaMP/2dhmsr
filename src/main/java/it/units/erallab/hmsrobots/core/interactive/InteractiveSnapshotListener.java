package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.snapshots.Snapshot;
import it.units.erallab.hmsrobots.core.snapshots.SnapshotListener;
import it.units.erallab.hmsrobots.viewers.DrawingUtils;
import it.units.erallab.hmsrobots.viewers.FramesImageBuilder;
import it.units.erallab.hmsrobots.viewers.drawers.Drawer;
import org.apache.commons.lang3.time.StopWatch;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class InteractiveSnapshotListener implements SnapshotListener {
  private final static int FRAME_RATE = 30;
  private static final Logger L = Logger.getLogger(FramesImageBuilder.class.getName());
  private final double dT;
  private final BasicInteractiveController controller;
  private final CanvasManager canvasManager;
  private final DevicePoller devicePoller;
  private StopWatch stopWatch;

  // Ottimizzazione: FrameT che indica ogni quanti frame vogliamo disegnare
  private double lastDrawT;
  //private List<List<Boolean>> flagHistory;
  private final SortedMap<Double, List<Boolean>> flagHistory;
  private final int totalTime;
  private final boolean provaFlag;


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
    double frameDT = (1.0 / FRAME_RATE);
    //System.out.println(simT + " " + (realT));

    if (lastDrawT == 0.0d || lastDrawT + frameDT <= realT) {
      Drawer drawer = canvasManager.getDrawer();
      Canvas canvas = canvasManager.getCanvas();
      Graphics2D g = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
      g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());
      drawer.draw(simT, s, g);


      g.setColor(DrawingUtils.alphaed(Color.BLACK, 0.9f));
      if (realT < 3) {
        devicePoller.setEnabledFlag(true);
        // Useful informations for the user
        // Timer
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        String timerString = "" + (3 - (int) simT);
        g.drawString(timerString,
            g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(timerString) / 2,
            g.getClipBounds().y + g.getClipBounds().height / 2 - g.getFontMetrics().stringWidth(timerString) / 2);
        // Title
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        String titleString = "Ready to start in:";
        g.drawString(titleString,
            g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(titleString) / 2,
            g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent());
      } else {

        devicePoller.setEnabledFlag(false);

        lastDrawT = realT;
        // Draw
        // Useful informations for the user
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));

        String timerString = "" + (totalTime - (int) simT);
        g.drawString(timerString,
            g.getClipBounds().x + g.getClipBounds().width - 1 - g.getFontMetrics().stringWidth(timerString) - 30,
            g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent());

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        String provaString = provaFlag ? "Training" : "Do your best now";
        g.drawString(provaString,
            g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(provaString) / 2,
            g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent());
      }

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
    long waitMillis = Math.max(Math.round((simT + dT - realT) * 1000d), 0);
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

