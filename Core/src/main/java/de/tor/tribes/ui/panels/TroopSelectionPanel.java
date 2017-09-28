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

import com.jidesoft.swing.LabeledTextField;
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmount;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.StandardAttack;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.attack.StandardAttackManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */

public abstract class TroopSelectionPanel<T extends TroopAmount> extends javax.swing.JPanel implements GenericManagerListener {
    
    private Hashtable<String, Point> unitCoordinates = new Hashtable<>();
    private LabeledTextField[][] unitFields = new LabeledTextField[20][20];
    
    @Override
    public void dataChangedEvent() {
        rebuildStandardAttackSelection();
    }
    
    @Override
    public void dataChangedEvent(String pGroup) {
        dataChangedEvent();
    }

    /**
     * Creates new form TroopSelectionPanel
     */
    public TroopSelectionPanel() {
        initComponents();
        setup(DataHolder.getSingleton().getUnits());
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jPanel2, BorderLayout.CENTER);
        StandardAttackManager.getSingleton().addManagerListener(TroopSelectionPanel.this);
        rebuildStandardAttackSelection();
    }
    
    public void hideSettings() {
        jSettingsButton.setVisible(false);
    }
    
    private void rebuildStandardAttackSelection() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (ManageableType t : StandardAttackManager.getSingleton().getAllElements()) {
            model.addElement(t);
        }
        jStandardAttackBox.setModel(model);
    }
    
    public final void setup(List<UnitHolder> pUnits) {
        setup(pUnits, true);
    }
    
    public final void setup(List<UnitHolder> pUnits, boolean pTypeSeparation) {
        jUnitContainer.removeAll();
        unitCoordinates.clear();
        unitFields = new LabeledTextField[20][20];
        int infantryX = 0;
        int cavallryX = 0;
        int otherX = 0;
        int unitCount = 0;
        for (UnitHolder unit : pUnits) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            if (unit.isInfantry()) {
                gridBagConstraints.gridx = (pTypeSeparation) ? infantryX : unitCount;
                gridBagConstraints.gridy = 0;
                unitCoordinates.put(unit.getPlainName(), new Point(gridBagConstraints.gridx, gridBagConstraints.gridy));
                infantryX++;
            } else if (unit.isCavalry()) {
                gridBagConstraints.gridx = (pTypeSeparation) ? cavallryX : unitCount;
                gridBagConstraints.gridy = (pTypeSeparation) ? 1 : 0;
                unitCoordinates.put(unit.getPlainName(), new Point(gridBagConstraints.gridx, gridBagConstraints.gridy));
                cavallryX++;
            } else if (unit.isOther()) {
                gridBagConstraints.gridx = (pTypeSeparation) ? otherX : unitCount;
                gridBagConstraints.gridy = (pTypeSeparation) ? 2 : 0;
                unitCoordinates.put(unit.getPlainName(), new Point(gridBagConstraints.gridx, gridBagConstraints.gridy));
                otherX++;
            }
            LabeledTextField unitField = new LabeledTextField();
            unitField.setIcon(ImageManager.getUnitIcon(unit));
            unitFields[gridBagConstraints.gridx][gridBagConstraints.gridy] = unitField;
            unitField.setMinimumSize(new Dimension(80, 24));
            unitField.setPreferredSize(new Dimension(80, 24));
            unitField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            unitField.setText("0");
            jUnitContainer.add(unitField, gridBagConstraints);
            unitCount++;
        }
        setEnabled(isEnabled());
    }
    
    public final void setupDefense(boolean pTypeSeparation) {
        List<UnitHolder> units = new LinkedList<>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (unit.isDefense()) {
                units.add(unit);
            }
        }
        setup(units, pTypeSeparation);
    }
    
    public final void setupOffense(boolean pTypeSeparation) {
        List<UnitHolder> units = new LinkedList<>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (unit.isOffense()) {
                units.add(unit);
            }
        }
        setup(units, pTypeSeparation);
    }
    
    public final void setupFarm(boolean pTypeSeparation) {
        List<UnitHolder> units = new LinkedList<>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (unit.isFarmUnit()) {
                units.add(unit);
            }
        }
        setup(units, pTypeSeparation);
    }
    
    public abstract T getAmounts();
    public abstract void setAmounts(T pAmounts);
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (unitFields[i][j] != null) {
                    unitFields[i][j].setEnabled(enabled);
                }
            }
        }
        jSettingsButton.setEnabled(enabled);
        if (!enabled) {
            jXCollapsiblePane1.setCollapsed(true);
        }        
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jStandardAttackBox = new javax.swing.JComboBox();
        jButton3 = new javax.swing.JButton();
        jUnitContainer = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jSettingsButton = new javax.swing.JButton();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Standardangriffe");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jLabel1, gridBagConstraints);

        jStandardAttackBox.setMinimumSize(new java.awt.Dimension(120, 18));
        jStandardAttackBox.setPreferredSize(new java.awt.Dimension(120, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jStandardAttackBox, gridBagConstraints);

        jButton3.setText("Verwenden");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireUseStandardAttackEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jButton3, gridBagConstraints);

        setLayout(new java.awt.BorderLayout());

        jUnitContainer.setMinimumSize(new java.awt.Dimension(73, 23));
        jUnitContainer.setPreferredSize(new java.awt.Dimension(73, 23));
        jUnitContainer.setLayout(new java.awt.GridBagLayout());
        add(jUnitContainer, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jSettingsButton.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jSettingsButton.setText("Einstellungen");
        jSettingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireClick(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jSettingsButton, gridBagConstraints);

        jXCollapsiblePane1.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jXCollapsiblePane1, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void fireClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireClick
        if (jSettingsButton.isEnabled()) {
            jXCollapsiblePane1.setCollapsed(!jXCollapsiblePane1.isCollapsed());
        }
    }//GEN-LAST:event_fireClick
    
    private void fireUseStandardAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUseStandardAttackEvent
        loadFromStandardAttack();
    }//GEN-LAST:event_fireUseStandardAttackEvent
    
    protected abstract void loadFromStandardAttack();
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jSettingsButton;
    private javax.swing.JComboBox jStandardAttackBox;
    private javax.swing.JPanel jUnitContainer;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de77");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de77")[0]);
        DataHolder.getSingleton().loadData(false);
        GlobalOptions.loadUserData();
        
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setSize(300, 300);
        TroopSelectionPanelFixed panel = new TroopSelectionPanelFixed();
        panel.setupDefense(false);
        f.getContentPane().add(panel);
        f.pack();
        f.setVisible(true);
    }

    protected LabeledTextField getFieldForUnit(UnitHolder pUnit) {
        Point location = unitCoordinates.get(pUnit.getPlainName());
        if (location != null) {
            return unitFields[location.x][location.y];
        }
        return null;
    }
    
    protected StandardAttack getSelectedAttack() {
        return (StandardAttack) jStandardAttackBox.getSelectedItem();
    }
}
