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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.jdesktop.swingx.JXList;

/**
 *
 * @author Torridity
 */
public class IconizedList extends JXList {
    
    private Image iconImage = null;
    
    public IconizedList(String pResourcePath) {
        try {
            BufferedImage b = ImageIO.read(IconizedList.class.getResource(pResourcePath));
            iconImage = b.getScaledInstance(80, 80, BufferedImage.SCALE_SMOOTH);
        } catch (Exception ignored) {
        }
    }
    
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (iconImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            Composite c = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f));
            g2d.drawImage(iconImage, getWidth() - 80, 0, null);
            g2d.setComposite(c);
        }
        
        if (!isEnabled()) {
            Graphics2D g2d = (Graphics2D) g;
            Composite c = g2d.getComposite();
            g2d.setColor(Color.DARK_GRAY);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setComposite(c);
        }
    }
}
