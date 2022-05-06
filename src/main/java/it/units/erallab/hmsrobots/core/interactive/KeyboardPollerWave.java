package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardPollerWave implements DevicePoller, KeyListener {
  private final WaveInteractiveController controller;


  public KeyboardPollerWave(WaveInteractiveController controller) {
    this.controller = controller;
  }

  @Override
  public void start(BasicInteractiveController basicInteractiveController, CanvasManager canvasManager) {
    canvasManager.addKeyListener(this);
  }

  @Override
  public void keyTyped(KeyEvent e) {

  }


  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_SPACE:
        controller.setKeyPressed(true);
        break;
      default:
        System.out.println("key pressed: not the space key");
        break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_W:
        controller.setKeyPressed(false);
        break;
      default:
        System.out.println("key released: not the space key");
        break;
    }
  }
}
