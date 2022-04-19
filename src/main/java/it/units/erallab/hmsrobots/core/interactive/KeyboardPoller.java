package it.units.erallab.hmsrobots.core.interactive;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardPoller implements DevicePoller, KeyListener {
  private final BasicInteractiveController controller;


  public KeyboardPoller(BasicInteractiveController controller) {
    this.controller = controller;
  }

  @Override
  public void start(BasicInteractiveController controller, CanvasManager canvasManager) {
    canvasManager.addKeyListener(this);
  }

  @Override
  public void keyTyped(KeyEvent e) {

  }


  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
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

  @Override
  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
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
