package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.core.controllers.AbstractController;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class KeyboardPoller implements DevicePoller, KeyListener {

  private Map<DevicePoller.RobotAreas, Boolean> keyPressed;

  public KeyboardPoller() {

    keyPressed = new HashMap<>();
    keyPressed.put(DevicePoller.RobotAreas.UP, false);
    keyPressed.put(DevicePoller.RobotAreas.DOWN, false);
    keyPressed.put(DevicePoller.RobotAreas.LEFT, false);
    keyPressed.put(DevicePoller.RobotAreas.RIGHT, false);
    keyPressed.put(DevicePoller.RobotAreas.IMPULSE, false);
  }


  @Override
  public Map<DevicePoller.RobotAreas, Boolean> getKeyPressed() {
    return keyPressed;
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
    switch (e.getKeyCode()) {
      case KeyEvent.VK_W:
        keyPressed.replace(RobotAreas.UP, true);
        break;
      case KeyEvent.VK_S:
        keyPressed.replace(RobotAreas.DOWN, true);
        break;
      case KeyEvent.VK_A:
        keyPressed.replace(RobotAreas.LEFT, true);
        break;
      case KeyEvent.VK_D:
        keyPressed.replace(RobotAreas.RIGHT, true);
        break;
      case KeyEvent.VK_SPACE:
        keyPressed.replace(RobotAreas.IMPULSE, true);
        break;
      default:
        System.out.println("key pressed: not an arrow");
        break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_W:
        keyPressed.replace(RobotAreas.UP, false);
        break;
      case KeyEvent.VK_S:
        keyPressed.replace(RobotAreas.DOWN, false);
        break;
      case KeyEvent.VK_A:
        keyPressed.replace(RobotAreas.LEFT, false);
        break;
      case KeyEvent.VK_D:
        keyPressed.replace(RobotAreas.RIGHT, false);
        break;
      case KeyEvent.VK_SPACE:
        keyPressed.replace(RobotAreas.IMPULSE, false);
        break;
      default:
        System.out.println("key released: not an arrow");
        break;
    }
  }
}

