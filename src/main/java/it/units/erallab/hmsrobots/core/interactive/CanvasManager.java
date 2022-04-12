package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.viewers.drawers.Drawer;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class CanvasManager extends JFrame {

    private final Supplier<Drawer> drawerSupplier;
    private final Canvas canvas;
    private Drawer drawer;

    private final static int INIT_WIN_WIDTH = 600;
    private final static int INIT_WIN_HEIGHT = 500;

    public CanvasManager(Supplier<Drawer> drawerSupplier) {
        this.drawerSupplier = drawerSupplier;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dimension = new Dimension(INIT_WIN_WIDTH, INIT_WIN_HEIGHT);
        canvas = new Canvas();
        canvas.setPreferredSize(dimension);
        canvas.setMinimumSize(dimension);
        canvas.setMaximumSize(dimension);
        getContentPane().add(canvas, BorderLayout.CENTER);

        //pack
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        canvas.setIgnoreRepaint(true);
        canvas.createBufferStrategy(2);

        rebuildDrawer();
    }

    public Drawer getDrawer() {
        return drawer;
    }

    public void rebuildDrawer() {
        drawer = drawerSupplier.get();
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
