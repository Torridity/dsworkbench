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

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.jdesktop.swingx.JXLabel;

/**
 *
 * @author Torridity
 */
public class CollapseExpandTrigger extends JXLabel {

    BufferedImage back = null;

    public CollapseExpandTrigger() {
        try {
            back = ImageIO.read(CollapseExpandTrigger.class.getResource("/res/ui/knob.png"));
        } catch (IOException ignored) {
        }

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int pos = w / 2;

        Graphics2D g2d = (Graphics2D) g;
        for (int y = 0; y < getHeight(); y += 20) {
            g2d.drawImage(back, null, pos - 2, y);
        }
        g2d.dispose();

    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.getContentPane().setLayout(new BorderLayout());
        f.add(new CollapseExpandTrigger(), BorderLayout.CENTER);
        f.setSize(100, 100);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
