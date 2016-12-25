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
package de.tor.tribes.util.generator.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.TargetInformation;
import de.tor.tribes.types.TimedAttack;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.*;
import de.tor.tribes.util.attack.AttackManager;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.DefaultComboBoxModel;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class SOSGenerator extends javax.swing.JFrame {

    /**
     * Creates new form SOSGenerator
     */
    public SOSGenerator() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jAmount = new org.jdesktop.swingx.JXTextField();
        jLabel7 = new javax.swing.JLabel();
        jAttackPlan = new javax.swing.JComboBox();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jAttack = new javax.swing.JComboBox();
        jNoDef = new javax.swing.JRadioButton();
        jMedDef = new javax.swing.JRadioButton();
        jFullDef = new javax.swing.JRadioButton();
        jIncludeTypes = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SOS Generator");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jButton1.setText("Generate Single Attack");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireGenerateAttackEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jButton1, gridBagConstraints);

        jButton2.setText("Generate SOS-Request");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireGenerateSOSRequestsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jButton2, gridBagConstraints);

        jAmount.setPrompt("Amount");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAmount, gridBagConstraints);

        jLabel7.setText("Attack Plan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jLabel7, gridBagConstraints);

        jAttackPlan.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAttackPlanSelectionChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAttackPlan, gridBagConstraints);

        jButton3.setText("R");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireReloadAttackPlansEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jButton3, gridBagConstraints);

        jLabel1.setText("Attack");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAttack, gridBagConstraints);

        buttonGroup1.add(jNoDef);
        jNoDef.setSelected(true);
        jNoDef.setText("No Deff");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jNoDef, gridBagConstraints);

        buttonGroup1.add(jMedDef);
        jMedDef.setText("Med Deff");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jMedDef, gridBagConstraints);

        buttonGroup1.add(jFullDef);
        jFullDef.setText("Full Deff");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jFullDef, gridBagConstraints);

        jIncludeTypes.setText("Include Attack Types");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jIncludeTypes, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireGenerateAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireGenerateAttackEvent
        generateAttack();
    }//GEN-LAST:event_fireGenerateAttackEvent

    private void fireReloadAttackPlansEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireReloadAttackPlansEvent
        DefaultComboBoxModel model = new DefaultComboBoxModel(AttackManager.getSingleton().getGroups());
        jAttackPlan.setModel(model);
        jAttack.setModel(new DefaultComboBoxModel(AttackManager.getSingleton().getAllElements((String) model.getElementAt(0)).toArray()));
    }//GEN-LAST:event_fireReloadAttackPlansEvent

    private void fireAttackPlanSelectionChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAttackPlanSelectionChangedEvent
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            jAttack.setModel(new DefaultComboBoxModel(AttackManager.getSingleton().getAllElements((String) jAttackPlan.getModel().getSelectedItem()).toArray()));
            jAmount.setText(Integer.toString(AttackManager.getSingleton().getAllElements((String) jAttackPlan.getModel().getSelectedItem()).size()));
        }
    }//GEN-LAST:event_fireAttackPlanSelectionChangedEvent

    private void fireGenerateSOSRequestsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireGenerateSOSRequestsEvent
        generateSOS();
    }//GEN-LAST:event_fireGenerateSOSRequestsEvent

    private void generateAttack() {
        Attack a = (Attack) jAttack.getSelectedItem();
        if (a == null) {
            System.err.println("No attack selected");
            return;
        }
        SimpleDateFormat f;
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format"));
        } else {
            f = new SimpleDateFormat(PluginManager.getSingleton().getVariableValue("sos.date.format.ms"));
        }
        StringBuilder b = new StringBuilder();

        b.append(PluginManager.getSingleton().getVariableValue("sos.source")).append(" ").
                append(a.getSource().toString()).append("\n");
        b.append("Ziel: ").append(a.getTarget().toString()).append("\n");
        b.append(PluginManager.getSingleton().getVariableValue("attack.arrive.time")).append(" ").
                append(f.format(a.getArriveTime())).append("\n");

        sendToClipboard(b.toString());
    }

    private void generateSOS() {
        String plan = (String) jAttackPlan.getSelectedItem();
        int amount = Math.min(UIHelper.parseIntFromField(jAmount, 1), jAttack.getItemCount());
        SOSRequest sos = null;
        Tribe t = null;
        for (int i = 0; i < amount; i++) {
            Attack a = AttackManager.getSingleton().getManagedElement(plan, i);
            if (sos == null) {
                sos = new SOSRequest(a.getTarget().getTribe());
                t = a.getTarget().getTribe();
            }

            if (t != null && a.getTarget().getTribe().getId() == t.getId()) {
                TargetInformation info = sos.addTarget(a.getTarget());
                if (info.getTroops().isEmpty()) {
                    Hashtable<UnitHolder, Integer> troops = getDefendingTroops();

                    Enumeration<UnitHolder> keys = troops.keys();
                    while (keys.hasMoreElements()) {
                        UnitHolder key = keys.nextElement();
                        info.addTroopInformation(key, troops.get(key));
                    }
                    info.setWallLevel(20);
                }
                if (jIncludeTypes.isSelected()) {
                    info.addAttack(a.getSource(), a.getArriveTime(), a.getUnit(), a.getType() == Attack.FAKE_TYPE, a.getUnit().getPlainName().equals("snob"));
                } else {
                    info.addAttack(a.getSource(), a.getArriveTime());
                }
            } else {
                System.err.println("Tribe " + a.getTarget().getTribe() + "does not fit request tribe " + sos.getDefender());
            }
        }

        /*
          [b]Dorf:[/b] [coord]112|87[/coord] [b]Wallstufe:[/b] 20 [b]Verteidiger:[/b] 23011 22928 0 266 0 814 0 0 0

          bäääng! [coord]282|306[/coord] --> Ankunftszeit: 11.10.11 14:37:57 [player]MrBlue76[/player]

          [b]Dorf:[/b] [coord]114|84[/coord] [b]Wallstufe:[/b] 20 [b]Verteidiger:[/b] 9079 9080 0 100 0 300 0 0 0

          bäääng! [coord]318|272[/coord] --> Ankunftszeit: 11.10.11 14:42:49 [player]MrBlue76[/player] bäääng! [coord]211|345[/coord] -->
          Ankunftszeit: 11.10.11 16:45:37 [player]MrBlue76[/player]
         */
        Enumeration<Village> targets = sos.getTargets();
        StringBuilder b = new StringBuilder();
        SimpleDateFormat df;
        if (de.tor.tribes.util.ServerSettings.getSingleton().isMillisArrival()) {
            df = new SimpleDateFormat("dd.MM.yy HH:mm:ss:SSS");
        } else {
            df = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        }
        while (targets.hasMoreElements()) {
            Village target = targets.nextElement();
            b.append("[b]Dorf:[/b] ").append(target.toBBCode()).append("\n");
            TargetInformation ti = sos.getTargetInformation(target);
            b.append("[b]Wallstufe:[/b] ").append(ti.getWallLevel()).append("\n");
            b.append("[b]Verteidiger:[/b] ");
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                b.append(ti.getTroops().get(unit)).append(" ");
            }
            b.append("\n\n");
            for (TimedAttack a : ti.getAttacks()) {
                if (jIncludeTypes.isSelected()) {
                    if (a.isPossibleFake()) {
                        b.append("Fake, ");
                    } else {
                        if (a.getUnit() != null) {
                            switch (a.getUnit().getPlainName()) {
                                case "axe":
                                    b.append("Axt, ");
                                    break;
                                case "light":
                                    b.append("LKAV, ");
                                    break;
                                case "snob":
                                    b.append("AG, ");
                                    break;
                                case "heavy":
                                    b.append("SKAV, ");
                                    break;
                                case "sword":
                                    b.append("Schwert, ");
                                    break;
                                case "catapult":
                                    b.append("Kata, ");
                                    break;
                            }
                        }
                    }
                }
                b.append(a.getSource().getName()).append(" ").append(a.getSource().toBBCode()).
                        append(" --> Ankunftszeit: ").append(df.format(new Date(a.getlArriveTime()))).append(" ").
                        append(a.getSource().getTribe().toBBCode()).
                        append("\n");
            }
            b.append("\n");
        }
        sendToClipboard(b.toString());

    }

    private Hashtable<UnitHolder, Integer> getDefendingTroops() {
        Hashtable<String, Integer> units = new Hashtable<>();
        if (jMedDef.isSelected()) {
            units.put("spear", getRandomValueInRange(1000, 2000));
            units.put("sword", getRandomValueInRange(1000, 2000));
            units.put("heavy", getRandomValueInRange(300, 500));
            units.put("spy", getRandomValueInRange(100, 200));
        } else if (jFullDef.isSelected()) {
            units.put("spear", getRandomValueInRange(5000, 6000));
            units.put("sword", getRandomValueInRange(5000, 6000));
            units.put("heavy", getRandomValueInRange(2000, 3000));
            units.put("spy", getRandomValueInRange(500, 800));
        } else if (jNoDef.isSelected()) {
            //add nothing
        }

        Hashtable<UnitHolder, Integer> result = TroopHelper.unitTableFromSerializableFormat(units);
        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
            if (!result.containsKey(u)) {
                result.put(u, 0);
            }
        }

        return result;
    }

    private void sendToClipboard(String pText) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(pText), null);
    }

    private int getRandomValueInRange(int min, int max) {
        return Math.max(min, (int) Math.random() * max);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | javax.swing.UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException ex) {
            java.util.logging.Logger.getLogger(SOSGenerator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedServer("de43");
        ProfileManager.getSingleton().loadProfiles();
        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de43")[0]);
        DataHolder.getSingleton().loadData(false);
        GlobalOptions.loadUserData();
        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new SOSGenerator().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private org.jdesktop.swingx.JXTextField jAmount;
    private javax.swing.JComboBox jAttack;
    private javax.swing.JComboBox jAttackPlan;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JRadioButton jFullDef;
    private javax.swing.JCheckBox jIncludeTypes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JRadioButton jMedDef;
    private javax.swing.JRadioButton jNoDef;
    // End of variables declaration//GEN-END:variables
}
