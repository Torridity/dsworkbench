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

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.DataHolderListener;
import de.tor.tribes.io.TroopAmount;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.StandardAttack;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.attack.StandardAttackManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Torridity
 */

public abstract class TroopSelectionPanel<T extends TroopAmount> extends javax.swing.JPanel
        implements GenericManagerListener, DataHolderListener {
    private Logger logger = LogManager.getLogger("TroopSelectionPanel");
    private HashMap<String, Point> unitCoordinates = new HashMap<>();
    private JTextField[][] unitFields = new JTextField[20][20];
    private JLabel[][] unitIconFields = new JLabel[20][20];

    private enum panelType { STRING_ARRAY, OFFENSE, DEFENSE, FARM, ALL };
    public enum alignType { HORIZONTAL, VERTICAL, GROUPED};
    panelType pType = panelType.ALL; alignType aType = alignType.GROUPED;
    String unitNames[]; int maxGrouping;
    
    @Override
    public void dataChangedEvent() {
        rebuildStandardAttackSelection();
    }
    
    @Override
    public void dataChangedEvent(String pGroup) {
        dataChangedEvent();
    }
    
    @Override public void fireDataHolderEvent(String eventMessage) {};
    @Override
    public void fireDataLoadedEvent(boolean pSuccess) {
        if(pSuccess) {
            setupFromInternal();
        }
    }

    /**
     * Creates new form TroopSelectionPanel
     */
    public TroopSelectionPanel() {
        initComponents();
        setupFromInternal();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jPanel2, BorderLayout.CENTER);
        StandardAttackManager.getSingleton().addManagerListener(this);
        DataHolder.getSingleton().addDataHolderListener(this);
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
    
    private void setup(List<UnitHolder> pUnits) {
        jUnitContainer.removeAll();
        unitCoordinates.clear();
        unitFields = new JTextField[20][20];
        unitIconFields = new JLabel[20][20];
        int infantryX = 0;
        int cavallryX = 0;
        int otherX = 0;
        int unitCount = 0;
        for (UnitHolder unit : pUnits) {
            GridBagConstraints textGridConst = new GridBagConstraints();
            GridBagConstraints iconGridConst = new GridBagConstraints();
            textGridConst.insets = new java.awt.Insets(5, 5, 5, 5);
            textGridConst.weightx = 1.0;
            textGridConst.weighty = 1.0;
            textGridConst.fill = java.awt.GridBagConstraints.HORIZONTAL;
            iconGridConst.insets = new java.awt.Insets(5, 5, 5, 5);
            iconGridConst.weightx = 1.0;
            iconGridConst.weighty = 1.0;
            iconGridConst.fill = java.awt.GridBagConstraints.HORIZONTAL;
            int x = 0, y = 0;
            switch(aType) {
            case GROUPED:
                if (unit.isInfantry()) {
                    x = infantryX;
                    y = 0;
                    infantryX++;
                } else if (unit.isCavalry()) {
                    x = cavallryX;
                    y = 1;
                    cavallryX++;
                } else {
                    x = otherX;
                    y = 2;
                    otherX++;
                }
                break;
            case HORIZONTAL:
                if(maxGrouping > 0) {
                    x = unitCount % maxGrouping;
                    y = unitCount / maxGrouping;
                } else {
                    x = unitCount;
                    y = 0;
                }
                break;
            case VERTICAL:
                if(maxGrouping > 0) {
                    x = unitCount / maxGrouping;
                    y = unitCount % maxGrouping;
                } else {
                    x = 0;
                    y = unitCount;
                }
                break;
            }
            textGridConst.gridx = x * 2 + 1;
            textGridConst.gridy = y;
            iconGridConst.gridx = x * 2;
            iconGridConst.gridy = y;
            unitCoordinates.put(unit.getPlainName(), new Point(x, y));
            unitCoordinates.put(unit.getPlainName(), new Point(x, y));
            JTextField unitField = new JTextField();
            JLabel unitIconField = new JLabel("");
            unitIconField.setIcon(ImageManager.getUnitIcon(unit));
            unitFields[x][y] = unitField;
            unitIconFields[x][y] = unitIconField;
            unitField.setMinimumSize(new Dimension(80, 24));
            unitIconField.setMinimumSize(new Dimension(24, 24));
            unitField.setPreferredSize(new Dimension(80, 24));
            unitIconField.setPreferredSize(new Dimension(24, 24));
            unitField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            unitIconField.setMaximumSize(new Dimension(24, 24));
            unitField.setText("0");
            jUnitContainer.add(unitField, textGridConst);
            jUnitContainer.add(unitIconField, iconGridConst);
            unitCount++;
        }
        setEnabled(isEnabled());
    }
    
    public final void setupDefense(alignType pAlignType, int pMaxGrouping) {
        pType = panelType.DEFENSE;
        aType = pAlignType;
        maxGrouping = pMaxGrouping;
        setupFromInternal();
    }
    
    public final void setupOffense(alignType pAlignType, int pMaxGrouping) {
        pType = panelType.OFFENSE;
        aType = pAlignType;
        maxGrouping = pMaxGrouping;
        setupFromInternal();
    }
    
    public final void setupFarm(alignType pAlignType, int pMaxGrouping) {
        pType = panelType.FARM;
        aType = pAlignType;
        maxGrouping = pMaxGrouping;
        setupFromInternal();
    }
    
    public final void setup(String pUnitNames[], alignType pAlignType, int pMaxGrouping) {
        pType = panelType.STRING_ARRAY;
        unitNames = pUnitNames;
        aType = pAlignType;
        maxGrouping = pMaxGrouping;
        setupFromInternal();
    }
    
    private void setupFromInternal() {
        List<UnitHolder> units = new LinkedList<>();
        switch(pType) {
        case ALL:
            units = DataHolder.getSingleton().getSendableUnits();
            break;
        case DEFENSE:
            for (UnitHolder unit : DataHolder.getSingleton().getSendableUnits()) {
                if (unit.isDefense()) {
                    units.add(unit);
                }
            }
            break;
        case OFFENSE:
            for (UnitHolder unit : DataHolder.getSingleton().getSendableUnits()) {
                if (unit.isOffense()) {
                    units.add(unit);
                }
            }
            break;
        case FARM:
            for (UnitHolder unit : DataHolder.getSingleton().getSendableUnits()) {
                if (unit.isFarmUnit()) {
                    units.add(unit);
                }
            }
            break;
        case STRING_ARRAY:
            for(String unit: unitNames) {
                UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(unit);
                if(u != UnknownUnit.getSingleton()) {
                    units.add(u);
                }
            }
            break;
        }
        setup(units);
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

        setLayout(new java.awt.GridBagLayout());

        jUnitContainer.setMinimumSize(new java.awt.Dimension(73, 23));
        jUnitContainer.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jUnitContainer, gridBagConstraints);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fireClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireClick
        if (jSettingsButton.isEnabled()) {
            jXCollapsiblePane1.setCollapsed(!jXCollapsiblePane1.isCollapsed());
        }
    }//GEN-LAST:event_fireClick
    
    private void fireUseStandardAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUseStandardAttackEvent
        try {
            loadFromStandardAttack();
        } catch(Exception e) {
            logger.warn("Error during loading Standard Attack", e);
        }
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

    protected JTextField getFieldForUnit(UnitHolder pUnit) {
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
