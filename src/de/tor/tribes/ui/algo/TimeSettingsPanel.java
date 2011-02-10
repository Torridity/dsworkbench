/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TimeSettingsPanel.java
 *
 * Created on Dec 27, 2010, 5:34:19 PM
 */
package de.tor.tribes.ui.algo;

import de.tor.tribes.io.ServerManager;
import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.ui.renderer.TimeFrameListCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author Torridity
 */
public class TimeSettingsPanel extends javax.swing.JPanel {

    private String minMaxTimeLabel = "Nicht vor";
    private String timeFrameLabel = "Abschickzeitfenster";
    private boolean allowTribeSpecificFrames = true;
    private boolean allowExactDayArrival = true;
    private boolean allowDisableMaxTime = true;
    private boolean allowSetMaxTimeToMinTimePlus1Hour = true;
    private boolean allowSetMaxTimeToMinTime = true;

    /** Creates new form TimeSettingsPanel */
    public TimeSettingsPanel() {
        initComponents();
    }

    public void reset() {
        //setup of send time spinner
        jValidAtDay.setTimeEnabled(false);
        jValidAtExactDay.setTimeEnabled(false);
        Calendar c = Calendar.getInstance();
        jMinTime.setDate(c.getTime());
        c.setTimeInMillis(System.currentTimeMillis() + DateUtils.MILLIS_PER_HOUR);
        jMaxTime.setDate(c.getTime());
        //setup of time frame selection
        jSendTimeFrame.setMinimumValue(0);
        jSendTimeFrame.setSliderBackground(Constants.DS_BACK);
        jSendTimeFrame.setMaximumColor(Constants.DS_BACK_LIGHT);
        jSendTimeFrame.setMinimumColor(Constants.DS_BACK_LIGHT);
        jSendTimeFrame.setMaximumValue(24);
        jSendTimeFrame.setSegmentSize(1);
        jSendTimeFrame.setUnit("h");
        jSendTimeFrame.setDecimalFormater(new DecimalFormat("##"));
        jSendTimeFrame.setBackground(Constants.DS_BACK_LIGHT);

        //setup time frame table
        DefaultListModel model = new DefaultListModel();
        jSendTimeFramesList.setModel(model);
        jSendTimeFramesList.setCellRenderer(new TimeFrameListCellRenderer());
        jTribeOnlyBox.setModel(new DefaultComboBoxModel(new Object[]{"Alle"}));
        jSendTimeFrame.setSound(false);
    }

    public void setAllowTribeSpecificFrames(boolean pValue) {
        allowTribeSpecificFrames = pValue;
        jPlayerOnlyLabel.setVisible(pValue);
        jTribeOnlyBox.setVisible(pValue);
    }

    public boolean isAllowTribeSpecificFrames() {
        return allowTribeSpecificFrames;
    }

    public void setMinMaxTimeLabel(String pText) {
        minMaxTimeLabel = pText;
        jMinMaxTimeLabel.setText(minMaxTimeLabel);
    }

    public void setAllowExactDayArrival(boolean pValue) {
        allowExactDayArrival = pValue;
        jOnlyValidAtExactDay.setVisible(pValue);
        jValidAtExactDay.setVisible(pValue);
    }

    public boolean isAllowExactDayArrival() {
        return allowTribeSpecificFrames;
    }

    public String getMinMaxTimeLabel() {
        return minMaxTimeLabel;
    }

    public void setTimeFrameLabel(String pText) {
        timeFrameLabel = pText;
        jSendTimeFrameLabel.setText(timeFrameLabel);
    }

    public String getTimeFrameLabel() {
        return timeFrameLabel;
    }

    public void addTribe(Tribe t) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) jTribeOnlyBox.getModel();
        List<Tribe> tribes = new LinkedList<Tribe>();
        for (int i = 0; i < model.getSize(); i++) {
            try {
                tribes.add((Tribe) model.getElementAt(i));
            } catch (Exception e) {
            }
        }
        if (!tribes.contains(t)) {
            tribes.add(t);
            Collections.sort(tribes);
            model = new DefaultComboBoxModel();
            model.addElement("Alle");
            for (Tribe tribe : tribes) {
                model.addElement(tribe);
            }
            jTribeOnlyBox.setModel(model);
        }
    }

    public void removeTribe(Tribe pTribe) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) jTribeOnlyBox.getModel();
        List<Tribe> tribes = new LinkedList<Tribe>();
        for (int i = 0; i < model.getSize(); i++) {
            try {
                tribes.add((Tribe) model.getElementAt(i));
            } catch (Exception e) {
            }
        }
        tribes.remove(pTribe);
        Collections.sort(tribes);
        model = new DefaultComboBoxModel();
        model.addElement("Alle");
        for (Tribe tribe : tribes) {
            model.addElement(tribe);
        }
        jTribeOnlyBox.setModel(model);
    }

    public List<TimeSpan> getTimeSpans() {
        List<TimeSpan> timeSpans = new LinkedList<TimeSpan>();

        //add time frames
        DefaultListModel model = (DefaultListModel) jSendTimeFramesList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            TimeSpan span = (TimeSpan) model.getElementAt(i);
            IntRange range = null;
            if (!span.isValidAtExactTime()) {
                range = new IntRange(span.getSpan().getMinimumInteger(), span.getSpan().getMaximumInteger());
            }
            TimeSpan tmp = new TimeSpan(span.getAtDate(), range, span.isValidFor());
            timeSpans.add(tmp);
        }

        return timeSpans;
    }

    public void setTimeSpans(List<TimeSpan> pSpans) {
        DefaultListModel model = new DefaultListModel();
        for (TimeSpan span : pSpans) {
            model.addElement(span);
        }
        jSendTimeFramesList.setModel(model);
    }

    public Date getMinTime() {
        return jMinTime.getSelectedDate();
    }

    public void setMinTime(Date pDate) {
        jMinTime.setDate(pDate);
    }

    public Date getMaxTime() {
        if (jMaxTime.isEnabled()) {
            return jMaxTime.getSelectedDate();
        } else {
            return null;
        }
    }

    public void setMaxTime(Date pDate) {
        jMaxTime.setDate(pDate);
    }

    public boolean isMaxTimeEnabled() {
        return jMaxTime.isEnabled();
    }

    public void setMaxTimeEnabled(boolean pValue) {
        jMaxTime.setEnabled(pValue);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jUseMaxTime = new javax.swing.JCheckBox();
        jMaxTimeToMinTimePlus1Hour = new javax.swing.JButton();
        jMaxTimeToMinTime = new javax.swing.JButton();
        jMinMaxTimeLabel = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSendTimeFramesList = new javax.swing.JList();
        jSendTimeFrameLabel = new javax.swing.JLabel();
        jSendTimeFrame = new com.visutools.nav.bislider.BiSlider();
        jButton1 = new javax.swing.JButton();
        jEveryDayValid = new javax.swing.JRadioButton();
        jOnlyValidAt = new javax.swing.JRadioButton();
        jPlayerOnlyLabel = new javax.swing.JLabel();
        jTribeOnlyBox = new javax.swing.JComboBox();
        jMinTime = new de.tor.tribes.ui.components.DateTimeField();
        jValidAtDay = new de.tor.tribes.ui.components.DateTimeField();
        jOnlyValidAtExactDay = new javax.swing.JRadioButton();
        jValidAtExactDay = new de.tor.tribes.ui.components.DateTimeField();
        jMinMaxTimeLabel1 = new javax.swing.JLabel();
        jMaxTime = new de.tor.tribes.ui.components.DateTimeField();

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        jUseMaxTime.setToolTipText("Späteste Abschickzeit berücksichtigen");
        jUseMaxTime.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jUseMaxTime.setMaximumSize(new java.awt.Dimension(20, 20));
        jUseMaxTime.setMinimumSize(new java.awt.Dimension(20, 20));
        jUseMaxTime.setOpaque(false);
        jUseMaxTime.setPreferredSize(new java.awt.Dimension(20, 20));
        jUseMaxTime.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/checkbox.png"))); // NOI18N
        jUseMaxTime.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeUseMaxTimeEvent(evt);
            }
        });
        jPanel1.add(jUseMaxTime);

        jMaxTimeToMinTimePlus1Hour.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add_one_hour.png"))); // NOI18N
        jMaxTimeToMinTimePlus1Hour.setToolTipText("Auf Startzeit + 1 Stunde stellen");
        jMaxTimeToMinTimePlus1Hour.setMaximumSize(new java.awt.Dimension(20, 20));
        jMaxTimeToMinTimePlus1Hour.setMinimumSize(new java.awt.Dimension(20, 20));
        jMaxTimeToMinTimePlus1Hour.setPreferredSize(new java.awt.Dimension(20, 20));
        jMaxTimeToMinTimePlus1Hour.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSetMaxTimeToMinTimePlus1HourEvent(evt);
            }
        });
        jPanel1.add(jMaxTimeToMinTimePlus1Hour);

        jMaxTimeToMinTime.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/time_equal.png"))); // NOI18N
        jMaxTimeToMinTime.setToolTipText("Auf Startzeit stellen");
        jMaxTimeToMinTime.setMaximumSize(new java.awt.Dimension(20, 20));
        jMaxTimeToMinTime.setMinimumSize(new java.awt.Dimension(20, 20));
        jMaxTimeToMinTime.setPreferredSize(new java.awt.Dimension(20, 20));
        jMaxTimeToMinTime.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSetMaxTimeToMinTimeEvent(evt);
            }
        });
        jPanel1.add(jMaxTimeToMinTime);

        jMinMaxTimeLabel.setText("Nicht vor dem");

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton2.setToolTipText("Gewählten Zeitrahmen entfernen");
        jButton2.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton2.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton2.setPreferredSize(new java.awt.Dimension(23, 23));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveTimeFrameEvent(evt);
            }
        });

        jScrollPane1.setViewportView(jSendTimeFramesList);

        jSendTimeFrameLabel.setText("Abschickzeitfenster");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton1.setToolTipText("Zeitrahmen hinzufügen");
        jButton1.setMaximumSize(new java.awt.Dimension(36, 36));
        jButton1.setMinimumSize(new java.awt.Dimension(36, 36));
        jButton1.setPreferredSize(new java.awt.Dimension(36, 36));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddNewTimeFrameEvent(evt);
            }
        });

        buttonGroup1.add(jEveryDayValid);
        jEveryDayValid.setSelected(true);
        jEveryDayValid.setText("Jeder Tag");
        jEveryDayValid.setToolTipText("Zeitrahmen für jeden Tag gültig");
        jEveryDayValid.setOpaque(false);
        jEveryDayValid.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireValidityChangedEvent(evt);
            }
        });

        buttonGroup1.add(jOnlyValidAt);
        jOnlyValidAt.setText("Nur am:");
        jOnlyValidAt.setOpaque(false);
        jOnlyValidAt.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireValidityChangedEvent(evt);
            }
        });

        jPlayerOnlyLabel.setText("Nur Spieler");

        jTribeOnlyBox.setToolTipText("Spieler, für den der Zeitrahmen gilt");
        jTribeOnlyBox.setMaximumSize(new java.awt.Dimension(150, 22));
        jTribeOnlyBox.setMinimumSize(new java.awt.Dimension(150, 22));
        jTribeOnlyBox.setPreferredSize(new java.awt.Dimension(150, 22));

        jMinTime.setToolTipText("Datum vor dem keine Angriffe abgeschickt werden/ankommen sollen");
        jMinTime.setOpaque(false);

        jValidAtDay.setEnabled(false);

        buttonGroup1.add(jOnlyValidAtExactDay);
        jOnlyValidAtExactDay.setText("Genau am/um:");
        jOnlyValidAtExactDay.setOpaque(false);
        jOnlyValidAtExactDay.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireValidityChangedEvent(evt);
            }
        });

        jMinMaxTimeLabel1.setText("Nicht nach dem");

        jMaxTime.setToolTipText("Datum vor dem keine Angriffe abgeschickt werden/ankommen sollen");
        jMaxTime.setEnabled(false);
        jMaxTime.setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMinMaxTimeLabel)
                    .addComponent(jMinMaxTimeLabel1)
                    .addComponent(jSendTimeFrameLabel))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE)
                            .addComponent(jSendTimeFrame, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPlayerOnlyLabel)
                                    .addComponent(jOnlyValidAtExactDay)
                                    .addComponent(jOnlyValidAt, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jValidAtExactDay, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                                    .addComponent(jValidAtDay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                                    .addComponent(jTribeOnlyBox, 0, 329, Short.MAX_VALUE)))
                            .addComponent(jEveryDayValid, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jMaxTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                            .addComponent(jMinTime, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMinTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMinMaxTimeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMinMaxTimeLabel1)
                    .addComponent(jMaxTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSendTimeFrameLabel)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE))
                        .addGap(14, 14, 14))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSendTimeFrame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jEveryDayValid)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jOnlyValidAt, 0, 0, Short.MAX_VALUE)
                    .addComponent(jValidAtDay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jOnlyValidAtExactDay, 0, 0, Short.MAX_VALUE)
                    .addComponent(jValidAtExactDay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTribeOnlyBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPlayerOnlyLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fireRemoveTimeFrameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveTimeFrameEvent
        Object[] selection = jSendTimeFramesList.getSelectedValues();
        if (selection == null || selection.length == 0) {
            return;
        }

        String message = "Zeitrahmen wirklich entfernen?";
        if (selection.length > 1) {
            message = selection.length + " " + message;
        }

        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Entfernen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            DefaultListModel model = (DefaultListModel) jSendTimeFramesList.getModel();
            for (Object o : selection) {
                model.removeElement(o);
            }
        }
}//GEN-LAST:event_fireRemoveTimeFrameEvent

    private void fireAddNewTimeFrameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddNewTimeFrameEvent
        int min = (int) Math.rint(jSendTimeFrame.getMinimumColoredValue());
        int max = (int) Math.rint(jSendTimeFrame.getMaximumColoredValue());
        if (min == max) {
            //start == end
            JOptionPaneHelper.showWarningBox(this, "Der angegebene Zeitrahmen ist ungültig", "Warnung");
            return;
        }

        //check if timeframe exists or intersects with other existing frame
        int intersection = -1;
        Tribe t = null;
        try {
            t = (Tribe) jTribeOnlyBox.getSelectedItem();
        } catch (Exception e) {
        }

        TimeSpan newSpan = null;
        if (jEveryDayValid.isSelected()) {
            newSpan = new TimeSpan(new IntRange(min, max), t);
        } else if (jOnlyValidAt.isSelected()) {
            newSpan = new TimeSpan(jValidAtDay.getSelectedDate(), new IntRange(min, max), t);
        } else {
            newSpan = new TimeSpan(jValidAtExactDay.getSelectedDate(), null, t);
        }

        DefaultListModel model = (DefaultListModel) jSendTimeFramesList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            TimeSpan existingSpan = (TimeSpan) model.getElementAt(i);
            if (newSpan.intersects(existingSpan)) {
                intersection = i + 1;
                break;
            }
        }

        if (intersection == -1) {
            model.addElement(newSpan);
        } else {
            JOptionPaneHelper.showWarningBox(this, "Das gewählte Zeitfenster überschneidet sich mit dem " + intersection + ". Eintrag.\n"
                    + "Bitte wähle die Zeitfenster so, dass es zu keinen Überschneidungen kommt.", "Überschneidung");
        }
}//GEN-LAST:event_fireAddNewTimeFrameEvent

    private void fireValidityChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireValidityChangedEvent
        if (jEveryDayValid.isSelected()) {
            jValidAtDay.setEnabled(false);
            jValidAtExactDay.setEnabled(false);
        } else if (jOnlyValidAt.isSelected()) {
            jValidAtDay.setEnabled(true);
            jValidAtExactDay.setEnabled(false);
        } else {
            jValidAtDay.setEnabled(false);
            jValidAtExactDay.setEnabled(true);
        }
}//GEN-LAST:event_fireValidityChangedEvent

    private void fireChangeUseMaxTimeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeUseMaxTimeEvent
        jMaxTime.setEnabled(jUseMaxTime.isSelected());
    }//GEN-LAST:event_fireChangeUseMaxTimeEvent

    private void fireSetMaxTimeToMinTimePlus1HourEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSetMaxTimeToMinTimePlus1HourEvent
        Date d = jMinTime.getSelectedDate();
        jMaxTime.setDate(DateUtils.addHours(d, 1));
    }//GEN-LAST:event_fireSetMaxTimeToMinTimePlus1HourEvent

    private void fireSetMaxTimeToMinTimeEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSetMaxTimeToMinTimeEvent
        Date d = jMinTime.getSelectedDate();
        jMaxTime.setDate(d);
    }//GEN-LAST:event_fireSetMaxTimeToMinTimeEvent

    public void addDefaultTimeFrame() {
        jSendTimeFrame.setMinimumColoredValue(8);
        jSendTimeFrame.setMinimumColoredValue(24);
        jEveryDayValid.setSelected(true);
        fireAddNewTimeFrameEvent(null);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JRadioButton jEveryDayValid;
    private de.tor.tribes.ui.components.DateTimeField jMaxTime;
    private javax.swing.JButton jMaxTimeToMinTime;
    private javax.swing.JButton jMaxTimeToMinTimePlus1Hour;
    private javax.swing.JLabel jMinMaxTimeLabel;
    private javax.swing.JLabel jMinMaxTimeLabel1;
    private de.tor.tribes.ui.components.DateTimeField jMinTime;
    private javax.swing.JRadioButton jOnlyValidAt;
    private javax.swing.JRadioButton jOnlyValidAtExactDay;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel jPlayerOnlyLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private com.visutools.nav.bislider.BiSlider jSendTimeFrame;
    private javax.swing.JLabel jSendTimeFrameLabel;
    private javax.swing.JList jSendTimeFramesList;
    private javax.swing.JComboBox jTribeOnlyBox;
    private javax.swing.JCheckBox jUseMaxTime;
    private de.tor.tribes.ui.components.DateTimeField jValidAtDay;
    private de.tor.tribes.ui.components.DateTimeField jValidAtExactDay;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the allowDisableMaxTime
     */
    public boolean isAllowDisableMaxTime() {
        return allowDisableMaxTime;
    }

    /**
     * @param allowDisableMaxTime the allowDisableMaxTime to set
     */
    public void setAllowDisableMaxTime(boolean allowDisableMaxTime) {
        jUseMaxTime.setVisible(allowDisableMaxTime);
        if (!allowDisableMaxTime && !jMaxTime.isEnabled()) {
            jMaxTime.setEnabled(true);
        }
        this.allowDisableMaxTime = allowDisableMaxTime;
    }

    /**
     * @return the allowSetMaxTimeToMinTimePlus1Hour
     */
    public boolean isAllowSetMaxTimeToMinTimePlus1Hour() {
        return allowSetMaxTimeToMinTimePlus1Hour;
    }

    /**
     * @param allowSetMaxTimeToMinTimePlus1Hour the allowSetMaxTimeToMinTimePlus1Hour to set
     */
    public void setAllowSetMaxTimeToMinTimePlus1Hour(boolean allowSetMaxTimeToMinTimePlus1Hour) {
        jMaxTimeToMinTimePlus1Hour.setVisible(allowSetMaxTimeToMinTimePlus1Hour);
        this.allowSetMaxTimeToMinTimePlus1Hour = allowSetMaxTimeToMinTimePlus1Hour;
    }

    /**
     * @return the allowSetMaxTimeToMinTime
     */
    public boolean isAllowSetMaxTimeToMinTime() {
        return allowSetMaxTimeToMinTime;
    }

    /**
     * @param allowSetMaxTimeToMinTime the allowSetMaxTimeToMinTime to set
     */
    public void setAllowSetMaxTimeToMinTime(boolean allowSetMaxTimeToMinTime) {
        jMaxTimeToMinTime.setVisible(allowSetMaxTimeToMinTime);
        this.allowSetMaxTimeToMinTime = allowSetMaxTimeToMinTime;
    }
}
