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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.ProfileManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class VillageOverviewMapPanel extends javax.swing.JPanel {

    private Color[][] colors = new Color[1000][1000];
    private Hashtable<Shape, Color> additionalShapes = new Hashtable<>();
    private int upperLeftContinent = -1;
    private int continentsInXAndY = -1;
    private int minCol = -1;
    private int maxCol = -1;
    private int minRow = -1;
    private int maxRow = -1;

    /**
     * Creates new form VillageOverviewMapPanel
     */
    public VillageOverviewMapPanel() {
        initComponents();
    }

    public void addShape(Shape pShape, Color pColor) {
        additionalShapes.put(pShape, pColor);
    }

    public void clearShapes() {
        additionalShapes.clear();
    }

    public void addVillage(Village pVillage, Color pColor) {
        addVillage(new Point(pVillage.getX(), pVillage.getY()), pColor);
    }

    public Color getColor(Village pVillage) {
        return colors[pVillage.getX()][pVillage.getY()];
    }

    public void addVillage(Point pPoint, Color pColor) {
        colors[pPoint.x][pPoint.y] = pColor;
        int cont = DSCalculator.getContinent(pPoint.x, pPoint.y);

        int col = cont % 10;
        int row = cont / 10;

        minCol = (minCol == -1) ? col : Math.min(col, minCol);
        maxCol = (maxCol == -1) ? col : Math.max(col, maxCol);
        minRow = (minRow == -1) ? row : Math.min(row, minRow);
        maxRow = (maxRow == -1) ? row : Math.max(row, maxRow);

        upperLeftContinent = 10 * minRow + minCol;

        int continentRows = Math.max(1, maxRow - minRow + 1);
        int continentColumns = Math.max(1, maxCol - minCol + 1);
        continentsInXAndY = Math.max(continentsInXAndY, Math.max(continentRows, continentColumns));
    }

    public void setOptimalSize(int pScale) {
        Dimension dim = new Dimension(getSquareContinents() * pScale * 100, getSquareContinents() * pScale * 100);
        setSize(dim);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
    }

    public void setOptimalSize() {
        setOptimalSize(1);
    }

    public int getSquareContinents() {
        return Math.max(1, continentsInXAndY);
    }

    public void removeVillage(Village pVillage) {
        removeVillage(new Point(pVillage.getX(), pVillage.getY()));
    }

    public void removeVillage(Point pPoint) {
        colors[pPoint.x][pPoint.y] = null;
    }

    public void addVillages(Hashtable<Village, Color> pVillages) {
        Enumeration<Village> keys = pVillages.keys();
        while (keys.hasMoreElements()) {
            Village v = keys.nextElement();
            colors[v.getX()][v.getY()] = pVillages.get(v);
        }
        repaint();
    }

    public void reset() {
        colors = new Color[1000][1000];
        upperLeftContinent = -1;
        continentsInXAndY = -1;
        minCol = -1;
        maxCol = -1;
        minRow = -1;
        maxRow = -1;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (continentsInXAndY < 0 || upperLeftContinent < 0) {
            g.setColor(Constants.DS_DEFAULT_BACKGROUND);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }
        int horizontalVillages = continentsInXAndY * 100;
        int verticalVillages = continentsInXAndY * 100;
        int startContinentColumn = upperLeftContinent % 10;
        int startContinentRow = upperLeftContinent / 10;

        BufferedImage im = ImageUtils.createCompatibleBufferedImage(horizontalVillages, verticalVillages, BufferedImage.OPAQUE);
        int deltaContinentColumn = startContinentColumn * 100;
        int deltaContinentRow = startContinentRow * 100;
        Graphics2D g2d = (Graphics2D) im.getGraphics();
        g2d.setColor(Constants.DS_DEFAULT_BACKGROUND);
        g2d.fillRect(0, 0, im.getWidth(), im.getHeight());

        for (int i = deltaContinentColumn; i < deltaContinentColumn + continentsInXAndY * 100; i++) {
            for (int j = deltaContinentRow; j < deltaContinentRow + continentsInXAndY * 100; j++) {
                if (i < 1000 && j < 1000 && colors[i][j] != null) {
                    g2d.setColor(colors[i][j]);
                    g2d.fillRect(i - deltaContinentColumn, j - deltaContinentRow, 1, 1);
                }
            }
        }
        g2d.setColor(Color.black);
        for (int i = minCol + 1; i <= maxCol + 1; i++) {
            g2d.drawLine((i - minCol) * 100, 0, (i - minCol) * 100, im.getHeight());
        }

        for (int i = minRow + 1; i <= maxRow + 1; i++) {
            g2d.drawLine(0, (i - minRow) * 100, im.getWidth(), (i - minRow) * 100);
        }

        for (int i = minRow + 1; i <= maxRow + 1; i++) {
            for (int j = minCol; j <= maxCol + 1; j++) {
                int cont = (i - 1) * 10 + j;
                g2d.drawString("K" + ((cont < 10) ? "0" + cont : cont), (j - minCol) * 100 + 2, (i - minRow) * 100 - 2);
                cont++;
            }
        }

        AffineTransform tb = g2d.getTransform();
        g2d.setTransform(AffineTransform.getTranslateInstance(-deltaContinentColumn, -deltaContinentRow));
        Enumeration<Shape> keys = additionalShapes.keys();
        while (keys.hasMoreElements()) {
            Shape s = keys.nextElement();

            g2d.setColor(additionalShapes.get(s));
            g2d.draw(s);
        }
        g2d.setTransform(tb);
        g2d.dispose();
        g.drawImage(im.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH), 0, 0, this);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setSize(300, 300);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        VillageOverviewMapPanel p = new VillageOverviewMapPanel();
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        DataHolder.getSingleton().loadData(false);

        Point po = DSCalculator.calculateCenterOfMass(Arrays.asList(ProfileManager.getSingleton().getProfiles("de43")[0].getTribe().getVillageList()));

        for (Village v : ProfileManager.getSingleton().getProfiles("de43")[0].getTribe().getVillageList()) {
            if (v.getContinent() == 96 || v.getContinent() == 95) {
                p.addVillage(new Point(v.getX(), v.getY()), Color.RED);
            }
        }
        p.addShape(new Ellipse2D.Double(po.x, po.y, 30, 30), Color.yellow);


        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
    }
}
