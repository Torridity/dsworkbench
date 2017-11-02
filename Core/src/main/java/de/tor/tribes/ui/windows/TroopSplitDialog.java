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
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.TroopSplit;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.CollapseExpandTrigger;
import de.tor.tribes.ui.renderer.TroopAmountListCellRenderer;
import de.tor.tribes.ui.renderer.TroopSplitListCellRenderer;
import de.tor.tribes.ui.renderer.UnitListCellRenderer;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.SplitSetHelper;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Torridity
 */
public class TroopSplitDialog extends javax.swing.JDialog {

    private static final Logger logger = Logger.getLogger("TroopSplitDialog");
    private boolean isInitialized = false;
    private TroopAmountFixed mSplitAmounts = new TroopAmountFixed();
    private List<TroopSplit> mSplits = new LinkedList<>();
    private Hashtable<String, TroopAmountFixed> splitSets = new Hashtable<>();

    /**
     * Creates new form TroopSplitDialog
     */
    public TroopSplitDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                removeSplitEnty();
            }
        };
        capabilityInfoPanel3.addActionListener(listener);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jTroopsPerSplitList.registerKeyboardAction(listener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        jSavedSplitsList.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                removeSavedSplit();
            }
        }, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        CollapseExpandTrigger trigger = new CollapseExpandTrigger();
        trigger.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                sourceInfoPanel.setCollapsed(!sourceInfoPanel.isCollapsed());
            }
        });
        jPanel7.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        jPanel7.add(trigger, BorderLayout.CENTER);
    }

    /**
     * Initialize all entries, renderers and reset the entire view
     */
    private void initialize() {
        DefaultComboBoxModel unitSelectionModel = new DefaultComboBoxModel(DataHolder.getSingleton().getUnits().toArray(new UnitHolder[]{}));
        jUnitSelectionBox.setModel(unitSelectionModel);
        jAmountField.setText("0");
        jTroopsPerSplitList.setModel(new DefaultListModel());
        jUnitSelectionBox.setRenderer(new UnitListCellRenderer());
        jTroopsPerSplitList.setCellRenderer(new TroopAmountListCellRenderer());
        jSplitsList.setCellRenderer(new TroopSplitListCellRenderer());
        mSplitAmounts.fill(-1);
        isInitialized = true;
        
        //TODO re-check where everything is stored and sort correctly
        try {
            UserProfile profile = GlobalOptions.getSelectedProfile();
            jToleranceSlider.setValue(Integer.parseInt(
                    profile.getProperty("tap.source.split.tolerance")));
        } catch(Exception e) {
            jToleranceSlider.setValue(0);
        }
    }

    /**
     * Insert the provided village list and show the split dialog
     */
    public void setupAndShow(List<Village> pVillageList) {
        if (!isInitialized) {
            initialize();
        }
        mSplits.clear();

        for (Village v : pVillageList) {
            mSplits.add(new TroopSplit(v));
        }
        loadSplitSets();
        updateSplitsList();
        setVisible(true);
    }

    public TroopSplit[] getSplits() {
        return mSplits.toArray(new TroopSplit[]{});
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        sourceInfoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jSavedSplitsList = new javax.swing.JList();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSplitsList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTroopsPerSplitList = new javax.swing.JList();
        jAmountField = new javax.swing.JTextField();
        jUnitSelectionBox = new javax.swing.JComboBox();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jToleranceSlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        capabilityInfoPanel3 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jButton2 = new javax.swing.JButton();
        jAcceptButton = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setTitle("Truppen aufsplitten");
        setAlwaysOnTop(true);
        setMinimumSize(new java.awt.Dimension(520, 430));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));
        jPanel1.setLayout(new java.awt.BorderLayout());

        sourceInfoPanel.setAnimated(false);
        sourceInfoPanel.setCollapsed(true);
        sourceInfoPanel.setDirection(org.jdesktop.swingx.JXCollapsiblePane.Direction.LEFT);
        sourceInfoPanel.setInheritAlpha(false);

        jPanel5.setMinimumSize(new java.awt.Dimension(190, 360));

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("Gespeicherte Splitsätze"));

        jSavedSplitsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jSavedSplitsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jSavedSplitsListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jSavedSplitsList);

        jTextField1.setMinimumSize(new java.awt.Dimension(100, 25));
        jTextField1.setPreferredSize(new java.awt.Dimension(100, 25));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jButton1.setToolTipText("Splitsatz speichern");
        jButton1.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton1.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton1.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireSaveSplitSetEvent(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/refresh.png"))); // NOI18N
        jButton4.setToolTipText("Splitsatz laden");
        jButton4.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton4.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton4.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireLoadSplitSetEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        sourceInfoPanel.add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel1.add(sourceInfoPanel, java.awt.BorderLayout.EAST);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel6.setBackground(new java.awt.Color(239, 235, 223));

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Resultierende Splits"));

        jScrollPane1.setViewportView(jSplitsList);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Truppen pro Split"));

        jScrollPane2.setViewportView(jTroopsPerSplitList);

        jAmountField.setText("700");
        jAmountField.setMaximumSize(new java.awt.Dimension(50, 25));
        jAmountField.setMinimumSize(new java.awt.Dimension(50, 25));
        jAmountField.setPreferredSize(new java.awt.Dimension(50, 25));

        jUnitSelectionBox.setMaximumSize(new java.awt.Dimension(100, 25));
        jUnitSelectionBox.setMinimumSize(new java.awt.Dimension(100, 25));
        jUnitSelectionBox.setPreferredSize(new java.awt.Dimension(100, 25));

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton3.setToolTipText("Truppenanzahl hinzufügen");
        jButton3.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton3.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton3.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddSplitAmountEvent(evt);
            }
        });

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Zulässige Abweichung");
        jLabel2.setMaximumSize(new java.awt.Dimension(130, 45));
        jLabel2.setMinimumSize(new java.awt.Dimension(130, 45));
        jLabel2.setPreferredSize(new java.awt.Dimension(130, 45));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel2.add(jLabel2, gridBagConstraints);

        jToleranceSlider.setMajorTickSpacing(10);
        jToleranceSlider.setMaximum(50);
        jToleranceSlider.setMinorTickSpacing(1);
        jToleranceSlider.setPaintLabels(true);
        jToleranceSlider.setPaintTicks(true);
        jToleranceSlider.setValue(10);
        jToleranceSlider.setMaximumSize(new java.awt.Dimension(200, 45));
        jToleranceSlider.setMinimumSize(new java.awt.Dimension(100, 45));
        jToleranceSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireToleranceChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel2.add(jToleranceSlider, gridBagConstraints);

        jLabel3.setText("%");
        jLabel3.setMaximumSize(new java.awt.Dimension(11, 45));
        jLabel3.setMinimumSize(new java.awt.Dimension(11, 45));
        jLabel3.setPreferredSize(new java.awt.Dimension(11, 45));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel2.add(jLabel3, gridBagConstraints);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jUnitSelectionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jUnitSelectionBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jAmountField, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4.add(jPanel6, java.awt.BorderLayout.CENTER);

        jPanel7.setMaximumSize(new java.awt.Dimension(32767, 20));
        jPanel7.setMinimumSize(new java.awt.Dimension(20, 20));
        jPanel7.setPreferredSize(new java.awt.Dimension(20, 20));
        jPanel7.setLayout(new java.awt.BorderLayout());
        jPanel4.add(jPanel7, java.awt.BorderLayout.EAST);

        jPanel1.add(jPanel4, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jPanel1, gridBagConstraints);

        capabilityInfoPanel3.setBbSupport(false);
        capabilityInfoPanel3.setCopyable(false);
        capabilityInfoPanel3.setPastable(false);
        capabilityInfoPanel3.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        getContentPane().add(capabilityInfoPanel3, gridBagConstraints);

        jButton2.setText("Abbrechen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSubmitEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        getContentPane().add(jButton2, gridBagConstraints);

        jAcceptButton.setText("Anwenden");
        jAcceptButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSubmitEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        getContentPane().add(jAcceptButton, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireAddSplitAmountEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddSplitAmountEvent
        int amount = 0;
        UnitHolder unit = null;
        try {
            amount = Integer.parseInt(jAmountField.getText());
        } catch (Exception e) {
            JOptionPaneHelper.showWarningBox(this, "Ungültige Truppenzahl", "Fehler");
            return;
        }

        try {
            unit = (UnitHolder) jUnitSelectionBox.getSelectedItem();
            if (unit == null) {
                unit = (UnitHolder) jUnitSelectionBox.getModel().getElementAt(0);
            }
        } catch (Exception e) {
            logger.error("Failed to obtain unit", e);
            JOptionPaneHelper.showWarningBox(this, "Ungültige Einheit", "Fehler");
            return;
        }
        mSplitAmounts.setAmountForUnit(unit, amount);
        updateAmountsList();
        saveSplitSets();
    }//GEN-LAST:event_fireAddSplitAmountEvent

    private void fireSubmitEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSubmitEvent
        if (evt.getSource() != jAcceptButton) {
            mSplits.clear();
        }
        saveSplitSets();
        setVisible(false);
    }//GEN-LAST:event_fireSubmitEvent

    private void fireToleranceChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireToleranceChangedEvent
        updateSplitsList();
    }//GEN-LAST:event_fireToleranceChangedEvent

    private void jSavedSplitsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jSavedSplitsListValueChanged
        jTextField1.setText((String) jSavedSplitsList.getSelectedValue());
    }//GEN-LAST:event_jSavedSplitsListValueChanged

    private void fireSaveSplitSetEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSaveSplitSetEvent
        String setName = jTextField1.getText();
        DefaultListModel filterModel = (DefaultListModel) jTroopsPerSplitList.getModel();

        if (setName == null || setName.length() == 0) {
            JOptionPaneHelper.showInformationBox(this, "Bitte einen Namen für das neue Splitset angeben", "Information");
            return;
        }

        if (filterModel.getSize() == 0) {
            JOptionPaneHelper.showInformationBox(this, "Ein Splitset muss mindestens einen Eintrag enthalten", "Information");
            return;
        }

        if (splitSets.get(setName) != null) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Das Splitset '" + setName + "' existiert bereits.\nMöchtest du es überschreiben?", "Bestätigung", "Nein", "Ja") != JOptionPane.OK_OPTION) {
                return;
            }
        }

        StringBuilder b = new StringBuilder();
        b.append(setName).append(",");
        TroopAmountFixed splits = mSplitAmounts.clone();

        splitSets.put(setName, splits);
        updateSplitSetList();

    }//GEN-LAST:event_fireSaveSplitSetEvent

    private void fireLoadSplitSetEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireLoadSplitSetEvent
        String selection = (String) jSavedSplitsList.getSelectedValue();
        if (selection != null) {
            mSplitAmounts = (TroopAmountFixed) splitSets.get(selection).clone();
            updateAmountsList();
        }
    }//GEN-LAST:event_fireLoadSplitSetEvent

    private void removeSavedSplit() {
        String set = (String) jSavedSplitsList.getSelectedValue();
        if (set != null) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Willst du das Split-Set '" + set + "' wirklich löschen?", "Löschen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
                return;
            }
        }

        splitSets.remove(set);
        saveSplitSets();
        updateSplitSetList();
    }

    private void saveSplitSets() {
        SplitSetHelper.saveSplitSets(splitSets);
    }

    private void loadSplitSets() {
        splitSets.clear();
        SplitSetHelper.loadSplitSets(splitSets);
        updateSplitSetList();
    }

    private void updateSplitSetList() {
        DefaultListModel model = new DefaultListModel();

        Enumeration<String> keys = splitSets.keys();
        while (keys.hasMoreElements()) {
            model.addElement(keys.nextElement());
        }

        jSavedSplitsList.setModel(model);
    }

    private void removeSplitEnty() {
        List selection = jTroopsPerSplitList.getSelectedValuesList();
        List<UnitHolder> units = new LinkedList<>();
        for (Object o : selection) {
            String unit = ((String) o).split(" ")[1].trim();
            mSplitAmounts.setAmountForUnit(unit, -1);
        }
        updateAmountsList();
    }

    /**
     * Update the list of split amounts
     */
    private void updateAmountsList() {
        DefaultListModel model = new DefaultListModel();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = mSplitAmounts.getAmountForUnit(unit);
            if (amount > 0) {
                model.addElement(amount + " " + unit.getPlainName());
            }
        }
        jTroopsPerSplitList.setModel(model);
        updateSplitsList();
    }

    /**
     * Update all splits and the split list itself
     */
    private void updateSplitsList() {
        DefaultListModel model = new DefaultListModel();
        for (TroopSplit split : mSplits) {
            split.update(mSplitAmounts, jToleranceSlider.getValue());
            model.addElement(split);
        }
        jSplitsList.setModel(model);
    }

    /**
     * Internal class for data holding
     */
  
    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        DataHolder.getSingleton().loadData(false);

        final TroopSplitDialog dialog = new TroopSplitDialog(null, false);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                dialog.setupAndShow(new LinkedList<Village>());
            }
        });

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel3;
    private javax.swing.JButton jAcceptButton;
    private javax.swing.JTextField jAmountField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JList jSavedSplitsList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList jSplitsList;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JSlider jToleranceSlider;
    private javax.swing.JList jTroopsPerSplitList;
    private javax.swing.JComboBox jUnitSelectionBox;
    private org.jdesktop.swingx.JXCollapsiblePane sourceInfoPanel;
    // End of variables declaration//GEN-END:variables

    public void saveSettings() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        profile.addProperty("tap.source.split.tolerance",jToleranceSlider.getValue());
    }
}
