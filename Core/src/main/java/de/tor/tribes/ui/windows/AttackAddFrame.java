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
package de.tor.tribes.ui.windows;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.attack.AttackManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 *
 * @author Torridity
 */
public class AttackAddFrame extends javax.swing.JFrame {

    private Village mSource;
    private boolean isMultiAttack = false;
    private Village[] mSources = null;
    private Village mTarget;
    private final NumberFormat nf = NumberFormat.getInstance();
    private boolean skipValidation = false;

    /**
     * Creates new form AttackAddFrame
     */
    public AttackAddFrame() {
        initComponents();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
    }

    private boolean validateDistance() {
        if (isMultiAttack) {
            for (Village source : mSources) {
                if (getSelectedUnit().getPlainName().equals("snob")) {
                    if (DSCalculator.calculateDistance(source, mTarget) > ServerSettings.getSingleton().getSnobRange()) {
                        //return false if distance is larger than max snob dist
                        return false;
                    }
                }
            }
        } else {
            if (getSelectedUnit().getPlainName().equals("snob")) {
                if (DSCalculator.calculateDistance(mSource, mTarget) > ServerSettings.getSingleton().getSnobRange()) {
                    //return false if distance is larger than max snob dist
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if the currently selected unit can arrive at the target village at the selected time. Returns result depending on the time mode
     * (arrive or send)
     */
    private boolean validateTime() {

        if (isMultiAttack) {
            for (Village source : mSources) {
                if (getSelectedUnit().getPlainName().equals("snob")) {
                    if (DSCalculator.calculateDistance(source, mTarget) > ServerSettings.getSingleton().getSnobRange()) {
                        //return false if distance is larger than max snob dist
                        return false;
                    }
                }
                long sendMillis = dateTimeField1.getSelectedDate().getTime();
                //check time depending selected unit
                double speed = ((UnitHolder) jUnitBox.getSelectedItem()).getSpeed();
                double minTime = DSCalculator.calculateMoveTimeInMinutes(source, mTarget, speed);
                long moveTime = (long) minTime * 60000;
                if (!(sendMillis > System.currentTimeMillis() + moveTime)) {
                    return false;
                }
            }
            return true;
        } else {
            if (getSelectedUnit().getPlainName().equals("snob")) {
                if (DSCalculator.calculateDistance(mSource, mTarget) > ServerSettings.getSingleton().getSnobRange()) {
                    //return false if distance is larger than max snob dist
                    return false;
                }
            }
            long sendMillis = dateTimeField1.getSelectedDate().getTime();
            //check time depending selected unit
            double speed = ((UnitHolder) jUnitBox.getSelectedItem()).getSpeed();
            double minTime = DSCalculator.calculateMoveTimeInMinutes(mSource, mTarget, speed);
            long moveTime = (long) minTime * 60000;
            return (sendMillis > System.currentTimeMillis() + moveTime);
        }
    }

    protected void buildUnitBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            model.addElement(unit);
        }
        jUnitBox.setModel(model);
    }

    public Date getTime() {
        return dateTimeField1.getSelectedDate();
    }

    public UnitHolder getSelectedUnit() {
        return (UnitHolder) jUnitBox.getSelectedItem();
    }

    public void setupAttack(List<Village> pSources, Village pTarget, int pInitialUnit, Date pInititalTime) {
        if (pSources == null || pSources.isEmpty()) {
            return;
        }
        if (pSources.size() == 1) {
            setupAttack(pSources.get(0), pTarget, pInitialUnit, pInititalTime);
            return;
        }
        isMultiAttack = true;
        mSources = pSources.toArray(new Village[]{});
        if ((pTarget == null)) {
            return;
        }
        for (Village source : mSources) {
            if (source == null) {
                pSources.remove(source);
            } else if (source.equals(pTarget)) {
                pSources.remove(source);
            } else if (source.getTribe() == Barbarians.getSingleton()) {
                pSources.remove(source);
            }
        }
        //rebuild sources list
        mSources = pSources.toArray(new Village[]{});
        skipValidation = true;
        int initialUnit = (pInitialUnit >= 0) ? pInitialUnit : 0;
        if (initialUnit > jUnitBox.getItemCount() - 1) {
            initialUnit = -1;
        }
        jUnitBox.setSelectedIndex(initialUnit);

        if (pInititalTime != null) {
            dateTimeField1.setDate(pInititalTime);
        } else {
            double maxDur = 0;
            for (Village source : mSources) {
                double dur = DSCalculator.calculateMoveTimeInMinutes(source, pTarget, ((UnitHolder) jUnitBox.getSelectedItem()).getSpeed());
                dur = dur * 60000;
                if (dur > maxDur) {
                    maxDur = dur;
                }
            }
            dateTimeField1.setDate(new Date(System.currentTimeMillis() + (long) maxDur + 60000));
        }

        mTarget = pTarget;
        jSourceVillage.setText(mSources.length + " gewählte Dörfer");
        if (pTarget.getTribe() != Barbarians.getSingleton()) {
            jTargetVillage.setText(pTarget.getTribe() + " (" + pTarget + ")");
        } else {
            jTargetVillage.setText("Barbarendorf" + " (" + pTarget.getX() + "|" + pTarget.getY() + ")");
        }

        jDistance.setText("Unterschiedliche Entfernungen");

        Iterator<String> plans = AttackManager.getSingleton().getGroupIterator();
        Object lastSelection = jAttackPlanBox.getSelectedItem();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        while (plans.hasNext()) {
            model.addElement(plans.next());
        }

        jAttackPlanBox.setModel(model);
        if (lastSelection != null) {
            jAttackPlanBox.setSelectedItem(lastSelection);
        } else {
            jAttackPlanBox.setSelectedIndex(0);
        }
        Rectangle bounds = getBounds();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int delta = dim.width - (bounds.x + bounds.width);
        if (delta < 0) {
            setLocation(bounds.x + delta, bounds.y);
        }
        setVisible(true);
        skipValidation = false;
    }

    public void setupAttack(Village pSource, Village pTarget, int pInitialUnit, Date pInititalTime) {
        isMultiAttack = false;
        if ((pSource == null) || (pTarget == null)) {
            return;
        }
        if (pSource.equals(pTarget)) {
            return;
        }
        if (pSource.getTribe() == Barbarians.getSingleton()) {
            //empty villages cannot attack
            return;
        }
        skipValidation = true;
        int initialUnit = (pInitialUnit >= 0) ? pInitialUnit : 0;
        if (initialUnit > jUnitBox.getItemCount() - 1) {
            initialUnit = -1;
        }
        jUnitBox.setSelectedIndex(initialUnit);

        if (pInititalTime != null) {
            dateTimeField1.setDate(pInititalTime);
        } else {
            double dur = DSCalculator.calculateMoveTimeInMinutes(pSource, pTarget, ((UnitHolder) jUnitBox.getSelectedItem()).getSpeed());
            dur = dur * 60000;
            dateTimeField1.setDate(new Date(System.currentTimeMillis() + (long) dur + 60000));
        }
        mSource = pSource;
        mTarget = pTarget;
        jSourceVillage.setText(pSource.getTribe() + " (" + pSource + ")");
        if (pTarget.getTribe() != Barbarians.getSingleton()) {
            jTargetVillage.setText(pTarget.getTribe() + " (" + pTarget + ")");
        } else {
            jTargetVillage.setText("Barbarendorf" + " (" + pTarget.getX() + "|" + pTarget.getY() + ")");
        }
        double d = DSCalculator.calculateDistance(mSource, mTarget);

        jDistance.setText(nf.format(d));

        Iterator<String> plans = AttackManager.getSingleton().getGroupIterator();
        Object lastSelection = jAttackPlanBox.getSelectedItem();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        while (plans.hasNext()) {
            model.addElement(plans.next());
        }

        jAttackPlanBox.setModel(model);
        if (lastSelection != null) {
            jAttackPlanBox.setSelectedItem(lastSelection);
        } else {
            jAttackPlanBox.setSelectedIndex(0);
        }
        Rectangle bounds = getBounds();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int delta = dim.width - (bounds.x + bounds.width);
        if (delta < 0) {
            setLocation(bounds.x + delta, bounds.y);
        }
        setVisible(true);
        skipValidation = false;
    }

    public void setupAttack(Village pSource, Village pTarget, int pInitialUnit) {
        setupAttack(pSource, pTarget, pInitialUnit, null);
    }

    public void setupAttack(Village pSource, Village pTarget) {
        setupAttack(pSource, pTarget, -1);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jOKButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jDistance = new javax.swing.JLabel();
        jSourceVillage = new javax.swing.JLabel();
        jSourceLabel = new javax.swing.JLabel();
        jTargetLabel = new javax.swing.JLabel();
        jUnitLabel = new javax.swing.JLabel();
        jUnitBox = new javax.swing.JComboBox();
        jDistanceLabel = new javax.swing.JLabel();
        jTargetVillage = new javax.swing.JLabel();
        jArriveTimeLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jAttackPlanBox = new javax.swing.JComboBox();
        dateTimeField1 = new de.tor.tribes.ui.components.DateTimeField();

        setTitle("Angriff hinzufügen");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jOKButton.setBackground(new java.awt.Color(239, 235, 223));
        jOKButton.setText("Angriff erstellen");
        jOKButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAttackEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jOKButton, gridBagConstraints);

        jCancelButton.setBackground(new java.awt.Color(239, 235, 223));
        jCancelButton.setText("Abbrechen");
        jCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCancelAddAttackEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jCancelButton, gridBagConstraints);

        jDistance.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jDistance.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jDistance.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        jDistance.setMinimumSize(new java.awt.Dimension(39, 20));
        jDistance.setOpaque(true);
        jDistance.setPreferredSize(new java.awt.Dimension(39, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jDistance, gridBagConstraints);

        jSourceVillage.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jSourceVillage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jSourceVillage.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        jSourceVillage.setMinimumSize(new java.awt.Dimension(39, 20));
        jSourceVillage.setOpaque(true);
        jSourceVillage.setPreferredSize(new java.awt.Dimension(39, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jSourceVillage, gridBagConstraints);

        jSourceLabel.setText("Herkunft");
        jSourceLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jSourceLabel, gridBagConstraints);

        jTargetLabel.setText("Ziel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jTargetLabel, gridBagConstraints);

        jUnitLabel.setText("Langsamste Einheit");
        jUnitLabel.setMaximumSize(new java.awt.Dimension(120, 14));
        jUnitLabel.setMinimumSize(new java.awt.Dimension(120, 14));
        jUnitLabel.setPreferredSize(new java.awt.Dimension(120, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jUnitLabel, gridBagConstraints);

        jUnitBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Speerträger (18)", "Adelsgeschlecht (35)", "Berittener Bogenschütze (11 Felder/min)" }));
        jUnitBox.setSelectedIndex(2);
        jUnitBox.setMinimumSize(new java.awt.Dimension(300, 25));
        jUnitBox.setPreferredSize(new java.awt.Dimension(300, 25));
        jUnitBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireUnitChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jUnitBox, gridBagConstraints);

        jDistanceLabel.setText("Entfernung");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jDistanceLabel, gridBagConstraints);

        jTargetVillage.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jTargetVillage.setAutoscrolls(true);
        jTargetVillage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTargetVillage.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        jTargetVillage.setMinimumSize(new java.awt.Dimension(39, 20));
        jTargetVillage.setOpaque(true);
        jTargetVillage.setPreferredSize(new java.awt.Dimension(39, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jTargetVillage, gridBagConstraints);

        jArriveTimeLabel.setText("Ankunftzeit");
        jArriveTimeLabel.setMaximumSize(new java.awt.Dimension(120, 14));
        jArriveTimeLabel.setMinimumSize(new java.awt.Dimension(120, 14));
        jArriveTimeLabel.setPreferredSize(new java.awt.Dimension(120, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jArriveTimeLabel, gridBagConstraints);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setText("Letzter Wert");
        jButton1.setToolTipText("Setzt den zuletzt verwendeten Wert als Ankunftzeit");
        jButton1.setMaximumSize(new java.awt.Dimension(93, 25));
        jButton1.setMinimumSize(new java.awt.Dimension(93, 25));
        jButton1.setPreferredSize(new java.awt.Dimension(93, 25));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSetLastArrivalEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jButton1, gridBagConstraints);

        jLabel1.setText("Angriffsplan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAttackPlanBox, gridBagConstraints);

        dateTimeField1.setMaximumSize(new java.awt.Dimension(32767, 25));
        dateTimeField1.setMinimumSize(new java.awt.Dimension(250, 25));
        dateTimeField1.setPreferredSize(new java.awt.Dimension(250, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(dateTimeField1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireUnitChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireUnitChangedEvent
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        UnitHolder u = (UnitHolder) evt.getItem();

        double maxDur = 0;
        if (mSources == null) {
            double dur = DSCalculator.calculateMoveTimeInMinutes(mSource, mTarget, u.getSpeed());
            dur *= 60000;
            maxDur = dur;
            if (dateTimeField1.getSelectedDate().getTime() < (System.currentTimeMillis() + maxDur)) {
                dateTimeField1.setDate(new Date(Math.round((System.currentTimeMillis() + maxDur))));
            }
        } else {
            for (Village source : mSources) {
                double dur = DSCalculator.calculateMoveTimeInMinutes(source, mTarget, u.getSpeed());
                dur *= 60000;
                if (dur > maxDur) {
                    maxDur = dur;
                }
            }
            if (dateTimeField1.getSelectedDate().getTime() < (System.currentTimeMillis() + maxDur)) {
                dateTimeField1.setDate(new Date(Math.round((System.currentTimeMillis() + maxDur))));
            }
        }
    }
}//GEN-LAST:event_fireUnitChangedEvent

private void fireCancelAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCancelAddAttackEvent
    setVisible(false);
}//GEN-LAST:event_fireCancelAddAttackEvent

private void fireAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackEvent
    if (!validateDistance()) {
        jDistance.setForeground(Color.RED);
    } else {
        jDistance.setForeground(Color.BLACK);
    }

    if (!validateTime()) {
        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die angegebene Ankunftszeit kann für mindestens ein Ziel nicht eingehalten werden.\nTrotzdem fortfahren?", "Ankunftszeit", "Nein", "Ja") != JOptionPane.YES_OPTION) {
            return;
        }
    }

    if (!validateDistance()) {
        if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die angegebene Entfernung kann von der gewählten Einheit für mindestens ein Ziel nicht zurückgelegt werden.\nTrotzdem fortfahren?", "Ankunftszeit", "Nein", "Ja") != JOptionPane.YES_OPTION) {
            return;
        }
    }

    Object plan = jAttackPlanBox.getSelectedItem();

    if (isMultiAttack) {
        for (Village source : mSources) {
            AttackManager.getSingleton().addAttack(source, mTarget, getSelectedUnit(), getTime(), (String) plan);
        }
    } else {
        AttackManager.getSingleton().addAttack(mSource, mTarget, getSelectedUnit(), getTime(), (String) plan);
    }
    GlobalOptions.setLastArriveTime(getTime());
    setVisible(false);
}//GEN-LAST:event_fireAddAttackEvent

private void fireSetLastArrivalEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSetLastArrivalEvent
    Date last = GlobalOptions.getLastArriveTime();
    if (last != null) {
        dateTimeField1.setDate(last);
    } else {
        JOptionPaneHelper.showWarningBox(this, "Noch kein Wert gespeichert", "Warnung");
    }
}//GEN-LAST:event_fireSetLastArrivalEvent
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.DateTimeField dateTimeField1;
    private javax.swing.JLabel jArriveTimeLabel;
    private javax.swing.JComboBox jAttackPlanBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JLabel jDistance;
    private javax.swing.JLabel jDistanceLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jOKButton;
    private javax.swing.JLabel jSourceLabel;
    private javax.swing.JLabel jSourceVillage;
    private javax.swing.JLabel jTargetLabel;
    private javax.swing.JLabel jTargetVillage;
    private javax.swing.JComboBox jUnitBox;
    private javax.swing.JLabel jUnitLabel;
    // End of variables declaration//GEN-END:variables
}
