/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.components;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JLabel;

/**
 *
 * @author Torridity
 */
public class CrossedLabel extends JLabel {

    private static BufferedImage CROSS = null;
    private boolean crossed = false;

    static {
        try {
            CROSS = ImageIO.read(CrossedLabel.class.getResource("/res/ui/red_x.png"));
        } catch (Exception e) {
            CROSS = null;
        }
    }

    public CrossedLabel() {
        uncross();
    }

    public void cross() {
        crossed = true;
        repaint();
    }

    public void uncross() {
        crossed = false;
        repaint();
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        if (CROSS != null && crossed) {
            int w = getWidth();
            int h = getHeight();
            if (h > w) {
                w = h;
            }
            Composite c = g2d.getComposite();
            Rectangle bounds = getBounds();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2d.drawImage(CROSS.getScaledInstance(h, h, BufferedImage.SCALE_DEFAULT), (int) Math.rint(bounds.getWidth() / 2.0 - h / 2.0), 0, null);
            g2d.setComposite(c);
        }

    }
}
