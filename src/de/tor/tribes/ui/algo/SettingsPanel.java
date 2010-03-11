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
import java.text.SimpleDateFormat;
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
public class SettingsPanel extends javax.swing.JPanel {

    /** Creates new form TimePanel */
    public SettingsPanel() {
        initComponents();
        setBackground(Constants.DS_BACK_LIGHT);
        reset();
    }

    public void reset() {
        //setup of send time spinner
        jValidAtDay.setEditor(new DateEditor(jValidAtDay, "dd.MM.yy"));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis() + 60 * 60 * 1000);
        jSendTime.setDate(c.getTime());
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

        c.setTimeInMillis(System.currentTimeMillis() + 2 * 60 * 60 * 1000);
        jArriveTime.setDate(c.getTime());
        jTribeTimeFrameBox.setModel(new DefaultComboBoxModel(new Object[]{"Alle"}));
        restoreProperties();
    }

    public void storeProperties() {
        String server = GlobalOptions.getSelectedServer();
        GlobalOptions.addProperty(server + ".attack.frame.start.date", Long.toString(jSendTime.getSelectedDate().getTime()));
        GlobalOptions.addProperty(server + ".attack.frame.arrive.date", Long.toString(jArriveTime.getSelectedDate().getTime()));
        GlobalOptions.addProperty(server + ".attack.frame.arrive.frame.min", Double.toString(jArriveTimeFrame.getMinimumColoredValue()));
        GlobalOptions.addProperty(server + ".attack.frame.arrive.frame.max", Double.toString(jArriveTimeFrame.getMaximumColoredValue()));
        GlobalOptions.addProperty(server + ".attack.frame.attacks.per.village", Integer.toString((Integer) jAttackPerVillageSpinner.getValue()));
        GlobalOptions.addProperty(server + ".attack.frame.var.arrive.time", Boolean.toString(jVariableArriveTimeBox.isSelected()));
        GlobalOptions.addProperty(server + ".attack.frame.algo.type", Integer.toString(jAlgoBox.getSelectedIndex()));
        GlobalOptions.addProperty(server + ".attack.frame.fake.off.targets", Boolean.toString(jFakeOffTargetsBox.isSelected()));
        DefaultListModel model = (DefaultListModel) jSendTimeFramesList.getModel();
        String spanProp = "";
        for (int i = 0; i < model.getSize(); i++) {
            spanProp += ((TimeSpan) model.getElementAt(i)).toPropertyString() + ";";
        }
        GlobalOptions.addProperty(server + ".attack.frame.time.spans", spanProp);
    }

    public void restoreProperties() {
        try {
            String server = GlobalOptions.getSelectedServer();
            jSendTime.setDate(new Date(Long.parseLong(GlobalOptions.getProperty(server + ".attack.frame.start.date"))));
            jArriveTime.setDate(new Date(Long.parseLong(GlobalOptions.getProperty(server + ".attack.frame.arrive.date"))));
            jArriveTimeFrame.setMinimumColoredValue(Double.parseDouble(GlobalOptions.getProperty(server + ".attack.frame.arrive.frame.min")));
            jArriveTimeFrame.setMaximumColoredValue(Double.parseDouble(GlobalOptions.getProperty(server + ".attack.frame.arrive.frame.max")));
            jAttackPerVillageSpinner.setValue(Integer.parseInt(GlobalOptions.getProperty(server + ".attack.frame.attacks.per.village")));
            jAlgoBox.setSelectedIndex(Integer.parseInt(GlobalOptions.getProperty(server + ".attack.frame.algo.type")));
            jVariableArriveTimeBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty(server + ".attack.frame.var.arrive.time")));
            jFakeOffTargetsBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty(server + ".attack.frame.fake.off.targets")));
            String spanProp = GlobalOptions.getProperty(server + ".attack.frame.time.spans");
            String[] spans = spanProp.split(";");
            DefaultListModel model = (DefaultListModel) jSendTimeFramesList.getModel();
            for (String span : spans) {
                try {
                    TimeSpan s = TimeSpan.fromPropertyString(span);
                    if (s != null) {
                        model.addElement(s);
                    }
                } catch (Exception invalid) {
                }
            }

        } catch (Exception e) {
        }
        if (jVariableArriveTimeBox.isSelected()) {
            jArriveTime.setTimeEnabled(false);
        } else {
            jArriveTime.setTimeEnabled(true);
        }
    }

    /**Add tribe to timeframe list*/
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

    /**Remove tribe from  timeframe list (not used yet)*/
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
        TimeFrame result = new TimeFrame(jSendTime.getSelectedDate(), jArriveTime.getSelectedDate());
        //add time frames
        DefaultListModel model = (DefaultListModel) jSendTimeFramesList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            TimeSpan span = (TimeSpan) model.getElementAt(i);

            Point s = new Point(span.getSpan().x, span.getSpan().y);
            s.setLocation(s.getX(), s.getY() - 1);
            TimeSpan tmp = new TimeSpan(span.getAtDate(), s, span.isValidFor());
            result.addTimeSpan(tmp);
        }
        if (jVariableArriveTimeBox.isSelected()) {
            result.setUseVariableArriveTime(true);
            result.setArriveSpan((int) Math.rint(jArriveTimeFrame.getMinimumColoredValue()), (int) Math.rint(jArriveTimeFrame.getMaximumColoredValue()));
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

        Date sendTime = jSendTime.getSelectedDate();
        if (sendTime.getTime() < System.currentTimeMillis()) {
            if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die Startzeit liegt in der Vergangenheit. Daher könnten Abschickzeitpunkte bestimmt werden,\n" +
                    "die nicht eingehalten werden können. Trotzdem fortfahren?", "Startzeit in Vergangenheit", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            } else {
                result = false;
            }
        }

        //check min case
        Date arrive = jArriveTime.getSelectedDate();
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

        boolean mightBeInNightBonus = false;
        String nightTime = "(0 - 8 Uhr)";
        int nightBonus = ServerManager.getNightBonusRange(GlobalOptions.getSelectedServer());
        if (!jVariableArriveTimeBox.isSelected()) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(arrive.getTime());
            //check for night bonus
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

                    nightTime = "(0 - 7 Uhr)";
                    break;
                }
                default: {
                    if (c.get(Calendar.HOUR_OF_DAY) >= 0 && c.get(Calendar.HOUR_OF_DAY) < 8) {
                        //in night bonus
                        mightBeInNightBonus = true;
                    }
                    break;
                }
            }
        } else {
            switch (nightBonus) {
                case DatabaseServerEntry.NO_NIGHT_BONUS: {
                    mightBeInNightBonus = false;
                    break;
                }
                case DatabaseServerEntry.NIGHT_BONUS_0to7: {
                    if (jArriveTimeFrame.getMinimumColoredValue() <= 7 || jArriveTimeFrame.getMaximumColoredValue() <= 7) {
                        //in night bonus
                        mightBeInNightBonus = true;
                    }
                    nightTime = "(0 - 7 Uhr)";
                    break;
                }
                default: {
                    if (jArriveTimeFrame.getMinimumColoredValue() <= 8 || jArriveTimeFrame.getMaximumColoredValue() <= 8) {
                        //in night bonus
                        mightBeInNightBonus = true;
                    }
                    break;
                }
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

    /**Return whether to use BruteForce or Iterix as algorithm*/
    public boolean useBruteForce() {
        return (jAlgoBox.getSelectedIndex() == 0);
    }

    public boolean fakeOffTargets() {
        return jFakeOffTargetsBox.isSelected();
    }

    /**Return the max. number of attacks per village*/
    public int getAttacksPerVillage() {
        return (Integer) jAttackPerVillageSpinner.getValue();
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
        jSendTime = new de.tor.tribes.ui.components.DateTimeField();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jArriveTimeFrame = new com.visutools.nav.bislider.BiSlider();
        jArriveTime = new de.tor.tribes.ui.components.DateTimeField();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jAttackPerVillageSpinner = new javax.swing.JSpinner();
        jVariableArriveTimeBox = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jAlgoBox = new javax.swing.JComboBox();
        jFakeOffTargetsBox = new javax.swing.JCheckBox();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(0, 0, 0), null), "Start", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jPanel1.setOpaque(false);

        jLabel1.setText("Startdatum");

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

        jLabel2.setText("Abschickzeitfenster");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/add.gif"))); // NOI18N
        jButton1.setToolTipText("Abschickzeitrahmen hinzufügen");
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
        jEveryDayValid.setToolTipText("Abschickzeitrahmen für jeden Tag gültig");
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

        jTribeTimeFrameBox.setToolTipText("Spieler, für den der Abschickzeitrahmen gilt");
        jTribeTimeFrameBox.setMaximumSize(new java.awt.Dimension(150, 22));
        jTribeTimeFrameBox.setMinimumSize(new java.awt.Dimension(150, 22));
        jTribeTimeFrameBox.setPreferredSize(new java.awt.Dimension(150, 22));

        jValidAtDay.setModel(new javax.swing.SpinnerDateModel());
        jValidAtDay.setToolTipText("Festes Datum, an dem der Abschickzeitrahmen gilt");
        jValidAtDay.setEnabled(false);

        jSendTime.setToolTipText("Datum zu dem der erste Angriff frühestens abgeschickt werden soll");
        jSendTime.setOpaque(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSendTime, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                        .addGap(56, 56, 56))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jEveryDayValid)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jSendTimeFrame, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jOnlyValidAt, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jTribeTimeFrameBox, 0, 184, Short.MAX_VALUE)
                                            .addComponent(jValidAtDay, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jSendTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(0, 0, 0), null), "Ankunft", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jPanel2.setOpaque(false);

        jLabel3.setText("Ankunftszeit");

        jLabel4.setText("Ankunftszeitrahmen");

        jArriveTimeFrame.setToolTipText("Ankunftszeitrahmen bei variabler Ankunftszeit");

        jArriveTime.setToolTipText("Ankunftszeit der Truppen");
        jArriveTime.setOpaque(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jArriveTimeFrame, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jArriveTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jArriveTimeFrame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(0, 0, 0), null), "Sonstige Einstellungen", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        jPanel3.setOpaque(false);

        jLabel5.setText("Angriffe pro Dorf");

        jAttackPerVillageSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 100, 1));
        jAttackPerVillageSpinner.setToolTipText("<html>Maximale Anzahl von Angriffen die einem Zieldorf zugewiesen wird.<br/>\nDiese Einstellung kann jedoch unter Ziele für jedes Zieldorf getrennt festgelegt werden.\n</html>");
        jAttackPerVillageSpinner.setMaximumSize(new java.awt.Dimension(100, 20));
        jAttackPerVillageSpinner.setMinimumSize(new java.awt.Dimension(100, 20));
        jAttackPerVillageSpinner.setPreferredSize(new java.awt.Dimension(100, 20));

        jVariableArriveTimeBox.setText("Variable Ankunftszeit");
        jVariableArriveTimeBox.setToolTipText("Variable Ankunftszeit verwenden");
        jVariableArriveTimeBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jVariableArriveTimeBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jVariableArriveTimeBox.setIconTextGap(30);
        jVariableArriveTimeBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        jVariableArriveTimeBox.setOpaque(false);
        jVariableArriveTimeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeVariableArriveTimeEvent(evt);
            }
        });

        jLabel6.setText("Zielsuche");

        jAlgoBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Zufällig", "Systematisch" }));
        jAlgoBox.setSelectedIndex(1);
        jAlgoBox.setToolTipText("Komplexität der Berechnung");
        jAlgoBox.setMaximumSize(new java.awt.Dimension(100, 20));
        jAlgoBox.setMinimumSize(new java.awt.Dimension(100, 20));
        jAlgoBox.setPreferredSize(new java.awt.Dimension(100, 20));

        jFakeOffTargetsBox.setText("Off-Ziele faken");
        jFakeOffTargetsBox.setToolTipText("Ziele die nicht als Fake-Ziel markiert sind für Fakes sperren");
        jFakeOffTargetsBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jFakeOffTargetsBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jFakeOffTargetsBox.setIconTextGap(60);
        jFakeOffTargetsBox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        jFakeOffTargetsBox.setOpaque(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jVariableArriveTimeBox)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jAlgoBox, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel5)
                            .addGap(50, 50, 50)
                            .addComponent(jAttackPerVillageSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jFakeOffTargetsBox))
                .addContainerGap(67, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jAttackPerVillageSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jVariableArriveTimeBox)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jAlgoBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jFakeOffTargetsBox)
                .addContainerGap(155, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void fireChangeVariableArriveTimeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeVariableArriveTimeEvent
        if (jVariableArriveTimeBox.isSelected()) {
            jArriveTime.setTimeEnabled(false);
        } else {
            jArriveTime.setTimeEnabled(true);
        }
    }//GEN-LAST:event_fireChangeVariableArriveTimeEvent
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup frameValidityGroup;
    private javax.swing.JComboBox jAlgoBox;
    private de.tor.tribes.ui.components.DateTimeField jArriveTime;
    private com.visutools.nav.bislider.BiSlider jArriveTimeFrame;
    private javax.swing.JSpinner jAttackPerVillageSpinner;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JRadioButton jEveryDayValid;
    private javax.swing.JCheckBox jFakeOffTargetsBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JRadioButton jOnlyValidAt;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private de.tor.tribes.ui.components.DateTimeField jSendTime;
    private com.visutools.nav.bislider.BiSlider jSendTimeFrame;
    private javax.swing.JList jSendTimeFramesList;
    private javax.swing.JComboBox jTribeTimeFrameBox;
    private javax.swing.JSpinner jValidAtDay;
    private javax.swing.JCheckBox jVariableArriveTimeBox;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.add(new SettingsPanel());
        f.pack();
        f.setVisible(true);


    }
}
