package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardPoller implements DevicePoller, KeyListener {
  private final BasicInteractiveController controller;
  private final String division;


  public KeyboardPoller(BasicInteractiveController controller, String division) {
    this.controller = controller;
    this.division = division;
  }

  @Override
  public void start(AbstractController controller, CanvasManager canvasManager) {
    canvasManager.addKeyListener(this);
  }

  @Override
  public void keyTyped(KeyEvent e) {

  }


  @Override
  public void keyPressed(KeyEvent e) {
    if (division.equals("4")) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_W:
          controller.setKeyPressed(true, 2);
          break;
        case KeyEvent.VK_S:
          controller.setKeyPressed(true, 1);
          break;
        case KeyEvent.VK_A:
          controller.setKeyPressed(true, 0);
          break;
        case KeyEvent.VK_D:
          controller.setKeyPressed(true, 3);
          break;
        default:
          System.out.println("key pressed: not an arrow");
          break;
      }
    } else if (division.equals("2ud")) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_A:
          controller.setKeyPressed(true, 0);
          break;
        case KeyEvent.VK_D:
          controller.setKeyPressed(true, 1);
          break;
        default:
          System.out.println("key pressed: not an arrow");
          break;
      }
    } else {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_W:
          controller.setKeyPressed(true, 0);
          break;
        case KeyEvent.VK_S:
          controller.setKeyPressed(true, 1);
          break;
        default:
          System.out.println("key pressed: not an arrow");
          break;
      }
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    if (division.equals("4")) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_W:
          controller.setKeyPressed(false, 2);
          break;
        case KeyEvent.VK_S:
          controller.setKeyPressed(false, 1);
          break;
        case KeyEvent.VK_A:
          controller.setKeyPressed(false, 0);
          break;
        case KeyEvent.VK_D:
          controller.setKeyPressed(false, 3);
          break;
        default:
          System.out.println("key released: not an arrow");
          break;
      }
    } else if (division.equals("2ud")){
      switch (e.getKeyCode()) {
        case KeyEvent.VK_A:
          controller.setKeyPressed(false, 0);
          break;
        case KeyEvent.VK_D:
          controller.setKeyPressed(false, 1);
          break;
        default:
          System.out.println("key released: not an arrow");
          break;
      }
    } else {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_W:
          controller.setKeyPressed(false, 0);
          break;
        case KeyEvent.VK_S:
          controller.setKeyPressed(false, 1);
          break;
        default:
          System.out.println("key released: not an arrow");
          break;
      }
    }
  }
}
