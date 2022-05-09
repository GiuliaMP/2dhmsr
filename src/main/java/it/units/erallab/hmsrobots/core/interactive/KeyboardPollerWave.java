package it.units.erallab.hmsrobots.core.interactive;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardPollerWave implements DevicePollerProva, KeyListener {
  private final PropagationController controller;


  public KeyboardPollerWave(PropagationController controller) {
    this.controller = controller;
  }

  @Override
  public void start(PropagationController basicInteractiveController, CanvasManager canvasManager) {
    canvasManager.addKeyListener(this);
  }

  @Override
  public void keyTyped(KeyEvent e) {

  }


  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_SPACE:
        controller.triggerPropagation();
        break;
      default:
        System.out.println("key pressed: not the space key");
        break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {

  }
}
