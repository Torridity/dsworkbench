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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DSWorkbenchAttackInfoPanel extends javax.swing.JPanel {

    private Hashtable<UnitHolder, List<Village>> offSources = null;
    private List<Village> offTargets = null;
    private Hashtable<UnitHolder, List<Village>> fakeSources = null;
    private List<Village> fakeTargets = null;

    /** Creates new form DSWorkbenchAttackInfoPanel */
    public DSWorkbenchAttackInfoPanel() {
        initComponents();
    }

    public void setVillages(Hashtable<UnitHolder, List<Village>> pOffSources, List<Village> pOffTargets, Hashtable<UnitHolder, List<Village>> pFakeSources, List<Village> pFakeTargets) {
        offSources = pOffSources;
        offTargets = pOffTargets;
        fakeSources = pFakeSources;
        fakeTargets = pFakeTargets;
    }

    public void refresh() {
        List<Village> allVillages = new LinkedList<Village>();
        //add sources by unit
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            //add off sources
            List<Village> offsByUnit = offSources.get(unit);
            if (offsByUnit != null) {
                for (Village v : offsByUnit) {
                    if (!allVillages.contains(v)) {
                        allVillages.add(v);
                    }
                }
            }
            //add fake sources
            List<Village> fakesByUnit = fakeSources.get(unit);
            if (fakesByUnit != null) {
                for (Village v : fakesByUnit) {
                    if (!allVillages.contains(v)) {
                        allVillages.add(v);
                    }
                }
            }
        }
        //add off targets
        for (Village v : offTargets) {
            if (!allVillages.contains(v)) {
                allVillages.add(v);
            }
        }
        //add fake targets
        for (Village v : fakeTargets) {
            if (!allVillages.contains(v)) {
                allVillages.add(v);
            }
        }
        /* int orderBefore = Village.getOrderType();
        Village.setOrderType(Village.ORDER_BY_COORDINATES);
        Collections.sort(allVillages);
        Village.setOrderType(orderBefore);
         */
        int xMin = 1000;
        int yMin = 1000;
        int xMax = 0;
        int yMax = 0;

        for (Village v : allVillages) {
            if (v.getX() < xMin) {
                xMin = v.getX();
            }
            if (v.getY() < yMin) {
                yMin = v.getY();
            }
            if (v.getX() > xMax) {
                xMax = v.getX();
            }
            if (v.getY() > yMax) {
                yMax = v.getY();
            }
        }
        int w = xMax - xMin;
        int h = yMax - yMin;
        int villageWidth = (int) Math.floor(getWidth() / w);
        int villageHeight = (int) Math.floor(getHeight() / h);
        setSize(w * villageWidth, h * villageHeight);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Constants.DS_DEFAULT_MARKER);
        g.fillRect(0, 0, getWidth(), getHeight());
        List<Village> allVillages = new LinkedList<Village>();
        //add sources by unit
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            //add off sources
            List<Village> offsByUnit = offSources.get(unit);
            if (offsByUnit != null) {
                for (Village v : offsByUnit) {
                    if (!allVillages.contains(v)) {
                        allVillages.add(v);
                    }
                }
            }
            //add fake sources
            List<Village> fakesByUnit = fakeSources.get(unit);
            if (fakesByUnit != null) {
                for (Village v : fakesByUnit) {
                    if (!allVillages.contains(v)) {
                        allVillages.add(v);
                    }
                }
            }
        }
        //add off targets
        for (Village v : offTargets) {
            if (!allVillages.contains(v)) {
                allVillages.add(v);
            }
        }
        //add fake targets
        for (Village v : fakeTargets) {
            if (!allVillages.contains(v)) {
                allVillages.add(v);
            }
        }
        /* int orderBefore = Village.getOrderType();
        Village.setOrderType(Village.ORDER_BY_COORDINATES);
        Collections.sort(allVillages);
        Village.setOrderType(orderBefore);*/
        int xMin = 1000;
        int yMin = 1000;
        int xMax = 0;
        int yMax = 0;

        for (Village v : allVillages) {
            if (v.getX() < xMin) {
                xMin = v.getX();
            }
            if (v.getY() < yMin) {
                yMin = v.getY();
            }
            if (v.getX() > xMax) {
                xMax = v.getX();
            }
            if (v.getY() > yMax) {
                yMax = v.getY();
            }
        }


        int w = xMax - xMin;
        int h = yMax - yMin;
        int villageWidth = (int) Math.floor(getWidth() / w);
        int villageHeight = (int) Math.floor(getHeight() / h);

        GeneralPath p = new GeneralPath();
        p.moveTo(0, 0);
        p.lineTo(5, 5);
        p.moveTo(5, 0);
        p.lineTo(0, 5);
        Graphics2D g2d = (Graphics2D) g;
        for (Village v : allVillages) {
            int x = v.getX() - xMin;
            int y = v.getY() - yMin;
            if (offTargets.contains(v)) {
                g2d.setColor(Color.red);
            } else if (fakeTargets.contains(v)) {
                g2d.setColor(Color.black);
            } else {
                g2d.setColor(Color.green);
            }
            g2d.setTransform(AffineTransform.getTranslateInstance(x * villageWidth - 3, y * villageHeight - 3));
            g2d.draw(p);
        }
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
