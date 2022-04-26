package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.geometry.Point2;
import it.units.erallab.hmsrobots.core.objects.Robot;
import it.units.erallab.hmsrobots.core.objects.Voxel;
import it.units.erallab.hmsrobots.core.snapshots.Snapshot;
import it.units.erallab.hmsrobots.core.snapshots.SnapshotListener;
import it.units.erallab.hmsrobots.core.snapshots.VoxelPoly;
import it.units.erallab.hmsrobots.viewers.DrawingUtils;
import it.units.erallab.hmsrobots.viewers.FramesImageBuilder;
import it.units.erallab.hmsrobots.viewers.drawers.Drawer;
import it.units.erallab.hmsrobots.viewers.drawers.SubtreeDrawer;
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
  private final SortedMap<Double, List<Boolean>> flagHistory;
  private final int totalTime;
  private final boolean trainingFlag;
  private StopWatch stopWatch;
  // Ottimizzazione: FrameT che indica ogni quanti frame vogliamo disegnare
  private double lastDrawT;

  private double prevT;
  private Double firstX;
  private double maxDistanceFromStart;


  public InteractiveSnapshotListener(double dT, CanvasManager canvasManager,
                                     DevicePoller devicePoller,
                                     BasicInteractiveController controller,
                                     int totalTime,
                                     boolean trainingFlag) {
    this.dT = dT;
    this.canvasManager = canvasManager;
    this.controller = controller;
    this.devicePoller = devicePoller;

    this.totalTime = totalTime;
    this.trainingFlag = trainingFlag;

    this.flagHistory = new TreeMap<>();

    prevT = 0;
    firstX = null;
    maxDistanceFromStart = 0.0;

    devicePoller.start(controller, canvasManager);
  }

  private double extractDistanceFromStart(Snapshot snapshot) {
    List<Point2> currentCenterPositions = SubtreeDrawer.Extractor.matches(null, Robot.class, null)
        .extract(snapshot)
        .stream()
        .map(s -> Point2.average(
            s.getChildren().stream()
                .filter(c -> Voxel.class.isAssignableFrom(c.getSnapshottableClass()))
                .map(c -> ((VoxelPoly) c.getContent()).center())
                .toArray(Point2[]::new))
        )
        .toList();
    double currentX = currentCenterPositions.get(0).x();
    if (firstX == null) {
      firstX = currentX;
    }
    return currentX - firstX;
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

        // Useful informations for the user
        // Timer
        /*g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        String timerString = "" + (3 - (int) simT);
        g.drawString(timerString,
            g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(timerString) / 2,
            g.getClipBounds().y + g.getClipBounds().height / 2 - g.getFontMetrics().stringWidth(timerString) / 2);*/
        String timerString = "" + (3 - (int) simT);
        drawStringOnCanvas(g,timerString,40,Position.CENTER);
        // Title
        /*g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        String titleString = "Ready to start in:";
        g.drawString(titleString,
            g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(titleString) / 2,
            g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent());*/
        String titleString = "Ready to start in:";
        drawStringOnCanvas(g,titleString,30,Position.TOPCENTER);
      } else if (realT < totalTime) {
        // Draw
        // Useful informations for the user
        /*g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        String timerString = "" + (totalTime - (int) simT);
        g.drawString(timerString,
            g.getClipBounds().x + g.getClipBounds().width - 1 - g.getFontMetrics().stringWidth(timerString) - 30,
            g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent());*/
        String timerString = "" + (totalTime - (int) simT);
        drawStringOnCanvas(g,timerString,40,Position.RIGHT);

        /*g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        String trainingString = trainingFlag ? "Training" : "Do your best now";
        g.drawString(trainingString,
            g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(trainingString) / 2,
            g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent());*/
        String trainingString = trainingFlag ? "Training" : "Do your best now";
        drawStringOnCanvas(g,trainingString,30,Position.TOPCENTER);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        double distanceFromStart = extractDistanceFromStart(s);
        if (distanceFromStart > maxDistanceFromStart) {
          maxDistanceFromStart = distanceFromStart;
        }
        String distanceFromStartString = String.format("Distance = %.1f", distanceFromStart);
        /*g.drawString(distanceFromStartString,
            g.getClipBounds().x + 1,
            g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent() + 30);*/
        drawStringOnCanvas(g,distanceFromStartString,20,Position.LEFT);
        this.prevT = simT;
      } else if (!trainingFlag) {

        lastDrawT = realT;

        String endString = "End";
        /*g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 50));
        g.drawString(endString,
            g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(endString) / 2,
            g.getClipBounds().y + g.getClipBounds().height / 2 - g.getFontMetrics().stringWidth(endString) / 2);*/
        drawStringOnCanvas(g,endString,50,Position.CENTER);

        // draw maximum travelled distance from strart
        String maxDistanceFromStartString = String.format("Your maximum distance was = %.1f", maxDistanceFromStart);
        /*g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        g.drawString(maxDistanceFromStartString,
            g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(maxDistanceFromStartString) / 2,
            g.getClipBounds().y + g.getClipBounds().height / 2 - g.getFontMetrics().stringWidth(maxDistanceFromStartString) / 2 +100);*/
        drawStringOnCanvas(g,endString,20,Position.CENTEREND);
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

  public enum Position{TOPCENTER, LEFT,RIGHT,CENTER,CENTEREND}

  private void drawStringOnCanvas(Graphics2D g, String string, int size, Position position){
    g.setFont(new Font(Font.MONOSPACED, Font.BOLD, size));
    int posX;
    int posY;
    switch (position) {
      case TOPCENTER:
        posX = g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(string) / 2;
        posY = g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent();
        break;
      case LEFT:
        posX = g.getClipBounds().x + 1;
        posY = g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent() + 30;
        break;
      case RIGHT:
        posX = g.getClipBounds().x + g.getClipBounds().width - 1 - g.getFontMetrics().stringWidth(string) - 30;
        posY = g.getClipBounds().y + 1 + g.getFontMetrics().getMaxAscent();
        break;
      case CENTER:
        posX = g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(string) / 2;
        posY = g.getClipBounds().y + g.getClipBounds().height / 2 - g.getFontMetrics().stringWidth(string) / 2;
        break;
      case CENTEREND:
        posX = g.getClipBounds().x + g.getClipBounds().width / 2 - g.getFontMetrics().stringWidth(string) / 2;
        posY = g.getClipBounds().y + g.getClipBounds().height / 2 - g.getFontMetrics().stringWidth(string) / 2 +10;
        break;
      default:
        posX = 0;
        posY = 0;
        break;
    }
    g.drawString(string,
        posX,
        posY);
  }

  public SortedMap<Double, List<Boolean>> getFlagHistory() {
    return flagHistory;
  }
}

