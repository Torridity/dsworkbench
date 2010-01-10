/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * SendTimePanel.java
 *
 * Created on 05.05.2009, 09:34:03
 */
package de.tor.tribes.ui.algo;

import de.tor.tribes.db.DatabaseServerEntry;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.algo.TimeFrame;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner.DateEditor;

/**
 * @author Jejkal
 */
public class TimePanel extends javax.swing.JPanel {

    /** Creates new form TimePanel */
    public TimePanel() {
        initComponents();
        setBackground(Constants.DS_BACK_LIGHT);
        reset();
    }

    public void reset() {
        //setup of send time spinner
        jSendTime.setEditor(new DateEditor(jSendTime, "dd.MM.yy HH:mm:ss"));
        jValidAtDay.setEditor(new DateEditor(jValidAtDay, "dd.MM.yy"));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis() + 60 * 60 * 1000);
        jSendTime.setValue(c.getTime());
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
        jArriveTimeFrame.setMinimumValue(0);
        jArriveTimeFrame.setSliderBackground(Constants.DS_BACK);
        jArriveTimeFrame.setMaximumColor(Constants.DS_BACK_LIGHT);
        jArriveTimeFrame.setMinimumColor(Constants.DS_BACK_LIGHT);
        jArriveTimeFrame.setMaximumValue(24);
        jArriveTimeFrame.setSegmentSize(1);
        jArriveTimeFrame.setUnit("h");
        jArriveTimeFrame.setDecimalFormater(new DecimalFormat("##"));
        jArriveTimeFrame.setBackground(Constants.DS_BACK_LIGHT);


        //setup time frame table
        DefaultListModel model = new DefaultListModel();
        jSendTimeFramesList.setModel(model);
        jArriveTime.setEditor(new DateEditor(jArriveTime, "dd.MM.yy HH:mm:ss"));
        c.setTimeInMillis(System.currentTimeMillis() + 2 * 60 * 60 * 1000);
        jArriveTime.setValue(c.getTime());
        jTribeTimeFrameBox.setModel(new DefaultComboBoxModel(new Object[]{"Alle"}));
    }

    public void addTribe(Tribe t) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) jTribeTimeFrameBox.getModel();
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
            jTribeTimeFrameBox.setModel(model);
        }
    }

    public void removeTribe(Tribe pTribe) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) jTribeTimeFrameBox.getModel();
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
        jTribeTimeFrameBox.setModel(model);
    }

    /**Return selected send time frames
     */
    public TimeFrame getTimeFrame() {
        TimeFrame result = new TimeFrame((Date) jSendTime.getValue(), (Date) jArriveTime.getValue());
        //add time frames
        DefaultListModel model = (DefaultListModel) jSendTimeFramesList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            TimeSpan span = (TimeSpan) model.getElementAt(i);

            Point s = new Point(span.getSpan().x, span.getSpan().y);
            s.setLocation(s.getX(), s.getY() - 1);
            TimeSpan tmp = new TimeSpan(span.getAtDate(), s, span.isValidFor());

            result.addTimeSpan(tmp);

        }
        return result;
    }

    public boolean validatePanel() {
        //no time frame specified
        boolean result = true;

        if (jSendTimeFramesList.getModel().getSize() == 0) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Es muss mindestens ein Abschickzeitfenster angegebene werden.\n" +
                    "Soll der Standardzeitrahmen (8 - 24 Uhr) verwendet werden?", "Fehlendes Zeitfenster", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                TimeSpan span = new TimeSpan(new Point(8, 24));
                ((DefaultListModel) jSendTimeFramesList.getModel()).addElement(span);
            } else {
                result = false;
            }
        }

        Date sendTime = (Date) jSendTime.getValue();
        if (sendTime.getTime() < System.currentTimeMillis()) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die Startzeit liegt in der Vergangenheit. Daher könnten Abschickzeitpunkte bestimmt werden,\n" +
                    "die nicht eingehalten werden können. Trotzdem fortfahren?", "Startzeit in Vergangenheit", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            } else {
                result = false;
            }
        }

        //check min case
        Date arrive = (Date) jArriveTime.getValue();

        if (sendTime.getTime() >= arrive.getTime()) {
            //check if start is after arrive
            JOptionPaneHelper.showWarningBox(this, "Die Startzeit ist größer als/identisch mit der Ankunftszeit.\n" +
                    "Du musst dies korrigieren bevor du fortfahren kannst.", "Startzeit in nach Ankunftszeit");
            result = false;
        } else {
            //check difference between start and arrive
            if (Math.abs(sendTime.getTime() - arrive.getTime()) < 30 * 60 * 1000) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Der Abstand zwischen Start- und Ankunftszeit ist extrem klein (< 30 Minuten).\n" +
                        "Höchstwahrscheinlich werden keine Ergebnisse gefunden. Trotzdem fortfahren?", "Start- und Endzeit zu dicht beieinander", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                } else {
                    result = false;
                }
            }
        }

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(arrive.getTime());
        boolean mightBeInNightBonus = false;
        //check for night bonus
        int nightBonus = ServerManager.getNightBonusRange(GlobalOptions.getSelectedServer());
        String nightTime = "(0 - 8 Uhr)";
        switch (nightBonus) {
            case DatabaseServerEntry.NO_NIGHT_BONUS: {
                mightBeInNightBonus = false;
                break;
            }
            case DatabaseServerEntry.NIGHT_BONUS_0to7: {
                if (c.get(Calendar.HOUR_OF_DAY) >= 0 && c.get(Calendar.HOUR_OF_DAY) < 7) {
                    //in night bonus
                    mightBeInNightBonus = true;
                }

                double max = jArriveTimeFrame.getMaximumColoredValue();
                if (max <= 7) {
                    mightBeInNightBonus = true;
                }
                nightTime = "(0 - 7 Uhr)";
                break;
            }
            default: {
                if (c.get(Calendar.HOUR_OF_DAY) >= 0 && c.get(Calendar.HOUR_OF_DAY) < 8) {
                    //in night bonus
                    mightBeInNightBonus = true;
                }
                double max = jArriveTimeFrame.getMaximumColoredValue();
                if (max <= 8) {
                    mightBeInNightBonus = true;
                }
                break;
            }
        }

        if (mightBeInNightBonus) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die angegebene Ankunftszeit kann unter Umständen im Nachbonus " + nightTime + " liegen.\n" +
                    "Willst du die Ankunftszeit entsprechend korrigieren?", "Nachtbonus", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                //correction requested
                result = false;
            }
        }
        return result;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frameValidityGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSendTime = new javax.swing.JSpinner();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSendTimeFramesList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jSendTimeFrame = new com.visutools.nav.bislider.BiSlider();
        jButton1 = new javax.swing.JButton();
        jEveryDayValid = new javax.swing.JRadioButton();
        jOnlyValidAt = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        jTribeTimeFrameBox = new javax.swing.JComboBox();
        jValidAtDay = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jArriveTime = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jArriveTimeFrame = new com.visutools.nav.bislider.BiSlider();
        jTimeFrameStepSize = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Start"));
        jPanel1.setOpaque(false);

        jLabel1.setText("Startzeit");

        jSendTime.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.SECOND));

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton2.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton2.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton2.setPreferredSize(new java.awt.Dimension(23, 23));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveTimeFrameEvent(evt);
            }
        });

        jScrollPane1.setViewportView(jSendTimeFramesList);

        jLabel2.setText("Abschickzeitfenster");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(36, 36));
        jButton1.setMinimumSize(new java.awt.Dimension(36, 36));
        jButton1.setPreferredSize(new java.awt.Dimension(36, 36));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddNewTimeFrameEvent(evt);
            }
        });

        frameValidityGroup.add(jEveryDayValid);
        jEveryDayValid.setSelected(true);
        jEveryDayValid.setText("Jeder Tag");
        jEveryDayValid.setOpaque(false);
        jEveryDayValid.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireValidityChangedEvent(evt);
            }
        });

        frameValidityGroup.add(jOnlyValidAt);
        jOnlyValidAt.setText("Nur am:");
        jOnlyValidAt.setOpaque(false);
        jOnlyValidAt.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireValidityChangedEvent(evt);
            }
        });

        jLabel7.setText("Nur Spieler");

        jTribeTimeFrameBox.setMaximumSize(new java.awt.Dimension(150, 22));
        jTribeTimeFrameBox.setMinimumSize(new java.awt.Dimension(150, 22));
        jTribeTimeFrameBox.setPreferredSize(new java.awt.Dimension(150, 22));

        jValidAtDay.setModel(new javax.swing.SpinnerDateModel());
        jValidAtDay.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jEveryDayValid)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSendTimeFrame, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                            .addComponent(jSendTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jOnlyValidAt, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTribeTimeFrameBox, 0, 150, Short.MAX_VALUE)
                                    .addComponent(jValidAtDay, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSendTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSendTimeFrame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jEveryDayValid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jOnlyValidAt)
                    .addComponent(jValidAtDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTribeTimeFrameBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Ankunft"));
        jPanel2.setOpaque(false);

        jLabel3.setText("Ankunftszeit");

        jArriveTime.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.SECOND));

        jLabel4.setText("Ankunftszeitrahmen");

        jTimeFrameStepSize.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sekunde", "Minute" }));
        jTimeFrameStepSize.setSelectedIndex(1);

        jLabel5.setText("Schrittweite");

        jLabel8.setText("Min. Abstand");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(60, 0, 600, 1));

        jLabel6.setText("Sekunden");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jSpinner1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6))
                    .addComponent(jTimeFrameStepSize, javax.swing.GroupLayout.Alignment.LEADING, 0, 266, Short.MAX_VALUE)
                    .addComponent(jArriveTimeFrame, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(jArriveTimeFrame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTimeFrameStepSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fireAddNewTimeFrameEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddNewTimeFrameEvent
        int min = (int) Math.rint(jSendTimeFrame.getMinimumColoredValue());
        int max = (int) Math.rint(jSendTimeFrame.getMaximumColoredValue());

        if (min == max) {
            //start == end
            JOptionPaneHelper.showWarningBox(this, "Der angegebene Zeitrahmen ist ungültig", "Warnung");
            return;

        }

        Line2D.Double newFrame = new Line2D.Double((double) min, 0, (double) max - 0.1, 0);
        //check if timeframe exists or intersects with other existing frame
        int intersection = -1;
        Tribe t = null;
        try {
            t = (Tribe) jTribeTimeFrameBox.getSelectedItem();
        } catch (Exception e) {
        }

        DefaultListModel model = (DefaultListModel) jSendTimeFramesList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            TimeSpan frame = (TimeSpan) model.getElementAt(i);
            if (jEveryDayValid.isSelected()) {
                //check for every day option
                Point span = frame.getSpan();
                Line2D.Double currentFrame = new Line2D.Double(span.x + 0.1, 0, span.y - 0.1, 0);
                if (currentFrame.intersectsLine(newFrame) && t == frame.isValidFor()) {
                    intersection = i + 1;
                    break;
                }
            } else {
                if (frame.getAtDate() != null && frame.getAtDate().getTime() == ((Date) jValidAtDay.getValue()).getTime()) {
                    //check if date is the same
                    Point span = frame.getSpan();
                    Line2D.Double currentFrame = new Line2D.Double(span.x + 0.1, 0, span.y - 0.1, 0);
                    if (currentFrame.intersectsLine(newFrame) && t == frame.isValidFor()) {
                        intersection = i + 1;
                        break;
                    }
                }
            }
        }

        if (intersection == -1) {
            // model.addElement(min + " Uhr - " + max + " Uhr");
            TimeSpan span = null;
            if (jEveryDayValid.isSelected()) {
                span = new TimeSpan(new Point(min, max), t);
            } else {
                span = new TimeSpan((Date) jValidAtDay.getValue(), new Point(min, max), t);
            }
            model.addElement(span);
        } else {
            JOptionPaneHelper.showWarningBox(this, "Das gewählte Zeitfenster überschneidet sich mit dem " + intersection + ". Eintrag.\n" +
                    "Bitte wähle die Zeitfenster so, dass es zu keinen Überschneidungen kommt.", "Überschneidung");
        }
    }//GEN-LAST:event_fireAddNewTimeFrameEvent

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

    private void fireValidityChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireValidityChangedEvent
        if (jEveryDayValid.isSelected()) {
            jValidAtDay.setEnabled(false);
        } else {
            jValidAtDay.setEnabled(true);
        }
    }//GEN-LAST:event_fireValidityChangedEvent
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup frameValidityGroup;
    private javax.swing.JSpinner jArriveTime;
    private com.visutools.nav.bislider.BiSlider jArriveTimeFrame;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JRadioButton jEveryDayValid;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JRadioButton jOnlyValidAt;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSendTime;
    private com.visutools.nav.bislider.BiSlider jSendTimeFrame;
    private javax.swing.JList jSendTimeFramesList;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JComboBox jTimeFrameStepSize;
    private javax.swing.JComboBox jTribeTimeFrameBox;
    private javax.swing.JSpinner jValidAtDay;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.add(new TimePanel());
        f.pack();
        f.setVisible(true);


    }
}
