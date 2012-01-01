/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JLabel;

/**
 *
 * @author Torridity
 */
public class MultiColorLabel extends JLabel {

    public MultiColorLabel() {
        super.setOpaque(true);
    }

    @Override
    public void setText(String text) {
        try {
            Integer.parseInt(text);
            super.setText(text);
        } catch (Exception e) {
            setText("0");
        }

    }

    @Override
    public final void setOpaque(boolean isOpaque) {
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int value = Integer.parseInt(getText());
        boolean fillRed = (value & 8) != 0;
        boolean fillGreen = (value & 4) != 0;
        boolean fillBlue = (value & 2) != 0;
        boolean fillMagenta = (value & 1) != 0;
        int segments = 0;
        if (fillRed) {
            segments++;
        }
        if (fillGreen) {
            segments++;
        }
        if (fillBlue) {
            segments++;
        }
        if (fillMagenta) {
            segments++;
        }

        int w = getWidth();
        int h = getHeight();
        g.setColor(Constants.DS_BACK_LIGHT);
        g.fillRect(0, 0, w, h);
        if (segments == 0) {
            return;
        }
        int segWidth = w / segments;
        int x = 0;
        int y = 0;
        if (fillRed) {
            g.setColor(Color.RED);
            g.fillRect(x, y, segWidth, h);
            x += segWidth;
        }
        if (fillGreen) {
            g.setColor(Color.GREEN);
            g.fillRect(x, y, segWidth, h);
            x += segWidth;
        }
        if (fillBlue) {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, segWidth, h);
            x += segWidth;
        }
        if (fillMagenta) {
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, segWidth, h);
            x += segWidth;
        }
    }
}
