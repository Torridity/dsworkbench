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
package de.tor.tribes.ui.panels;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Marker;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 *
 * @author  Charon
 */
public class ScreenshotPanel extends javax.swing.JPanel {

    private BufferedImage mBuffer = null;
    private int iScaling = 1;

    /** Creates new form ScreenshotPanel */
    public ScreenshotPanel() {
        initComponents();
    }

    public void setBuffer(BufferedImage pBuffer) {
        if (pBuffer == null) {
            return;
        }
        mBuffer = pBuffer;
        setScaling(1);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                updateUI();
            }
        });
    }

    public BufferedImage getResult(int pTransparency) {
        int width = mBuffer.getWidth() * iScaling;
        int height = mBuffer.getHeight() * iScaling;
        BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        b.getGraphics().drawImage(mBuffer.getScaledInstance(width, height, BufferedImage.SCALE_DEFAULT), 0, 0, null);
        if (pTransparency < 10) {
            Graphics2D g2d = (Graphics2D) b.getGraphics();
            float transFac = 1.0f - ((float) pTransparency / 10.0f);
            Composite a = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transFac);

            Font t = new Font("Serif", Font.BOLD, 16);
            g2d.setComposite(a);
            //draw legend
            List<ManageableType> allElements = MarkerManager.getSingleton().getAllElementsFromAllGroups();
            List<ManageableType> toRemove = new LinkedList<>();
            for (ManageableType elem : allElements) {
                Marker m = (Marker) elem;
                if (!m.isShownOnMap()) {
                    toRemove.add(elem);
                }
            }

            for (ManageableType elem : toRemove) {
                allElements.remove(elem);
            }

            int legendW = 0;
            //get legend height plus 2 times spacing
            int legendH = allElements.size() * b.getGraphics().getFontMetrics().getHeight() + 10;
            int heightF = b.getGraphics().getFontMetrics().getHeight();
            for (ManageableType elem : allElements) {

                Marker m = (Marker) elem;
                String value;
                switch (m.getMarkerType()) {
                    case TRIBE:
                        value = DataHolder.getSingleton().getTribes().get(m.getMarkerID()).getName();
                        break;
                    case ALLY:
                        value = DataHolder.getSingleton().getAllies().get(m.getMarkerID()).getName();
                        break;
                    default:
                        value = "";
                }

                Rectangle2D bounds = b.getGraphics().getFontMetrics().getStringBounds(value, b.getGraphics());
                if (bounds.getWidth() > legendW) {
                    legendW = (int) Math.rint(bounds.getWidth());
                }
            }
            //add left and right spacing
            //left/right border of 5px each, spacing of 5px between font and color and color field width
            legendW += 10 + 5 + heightF;
            //add line spacing of 2px
            //legendH += count * 2;
            g2d.setColor(Constants.DS_BACK);
            g2d.fillRect(width - legendW - 5, height - legendH - 5, legendW, legendH);
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < allElements.size(); i++) {
                g2d.setColor(Color.BLACK);
                Marker m = (Marker) allElements.get(i);
                String value;
                switch (m.getMarkerType()) {
                    case TRIBE:
                        value = DataHolder.getSingleton().getTribes().get(m.getMarkerID()).getName();
                        break;
                    case ALLY:
                        value = DataHolder.getSingleton().getAllies().get(m.getMarkerID()).getName();
                        break;
                    default:
                        value = "";
                }

                Color c = m.getMarkerColor();
                g2d.drawString(value, width - legendW, (height - legendH + 5 + (heightF / 2) + heightF * i));
                g2d.setColor(c);
                g2d.fillRect(width - legendW + (legendW - 10 - heightF), height - legendH + i * heightF, heightF, heightF);
            }
            g2d.dispose();
        }


        return b;
    }

    public void setScaling(int pValue) {
        iScaling = pValue;
        Dimension dim = new Dimension(mBuffer.getWidth() * iScaling, mBuffer.getHeight() * iScaling);
        setSize(dim);
        setPreferredSize(dim);
        setMaximumSize(dim);
        setMinimumSize(dim);
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(mBuffer.getScaledInstance(mBuffer.getWidth() * iScaling, mBuffer.getHeight() * iScaling, BufferedImage.SCALE_DEFAULT), 0, 0, null);
        g2d.dispose();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
