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
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.algo.TimeFrame;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner.DateEditor;

/**
 * @TODO (DIFF) Day dependent time frame added
 * @author Jejkal
 */
public class TimePanel extends javax.swing.JPanel {


    /** Creates new form TimePanel */
    public TimePanel() {
        initComponents();
        setBackground(Constants.DS_BACK_LIGHT);
        reset();
    }

    public void activateTolerance(boolean value) {
        jToleranceField.setEnabled(value);
        jLabel4.setEnabled(value);
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
        //setup time frame table
        DefaultListModel model = new DefaultListModel();
        jSendTimeFramesList.setModel(model);
        jArriveTime.setEditor(new DateEditor(jArriveTime, "dd.MM.yy HH:mm:ss"));
        c.setTimeInMillis(System.currentTimeMillis() + 2 * 60 * 60 * 1000);
        jArriveTime.setValue(c.getTime());
        jToleranceField.setValue(2l);
    }

    /**Return selected send time frames
     */
    public TimeFrame getTimeFrame() {
        TimeFrame result = new TimeFrame((Date) jSendTime.getValue(), (Date) jArriveTime.getValue());
        //set arrive tolerance in seconds
        result.setArriveTolerance((Long) jToleranceField.getValue());
        //add time frames
        DefaultListModel model = (DefaultListModel) jSendTimeFramesList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            TimeSpan span = (TimeSpan) model.getElementAt(i);
            /*String[] split = frame.split("-");
            int start = Integer.parseInt(split[0].replaceAll("Uhr", "").trim());
            int end = Integer.parseInt(split[1].replaceAll("Uhr", "").trim());
            //reduce end because following calculations use 59 minutes for the last hour
            end -= 1;*/
            Point s = span.getSpan();
            s.setLocation(s.getX(), s.getY() - 1);
            span.setSpan(s);
            //result.addFrame(start, end);
            result.addTimeSpan(span);
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

        //check if arrival might be in night bonus
        long arrivalTolerance = (Long) jToleranceField.getValue() * 60 * 60 * 1000;
        //check min case
        Date arrive = (Date) jArriveTime.getValue();
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
                //check min arrive time
                c.setTimeInMillis(arrive.getTime() - (jToleranceField.isEnabled() ? arrivalTolerance : 0));
                if (c.get(Calendar.HOUR_OF_DAY) >= 0 && c.get(Calendar.HOUR_OF_DAY) < 7) {
                    mightBeInNightBonus = true;
                } else {
                    //check max arrive time
                    c.setTimeInMillis(arrive.getTime() + (jToleranceField.isEnabled() ? arrivalTolerance : 0));
                    if (c.get(Calendar.HOUR_OF_DAY) >= 0 && c.get(Calendar.HOUR_OF_DAY) < 7) {
                        mightBeInNightBonus = true;
                    }
                }
                nightTime = "(0 - 7 Uhr)";
                break;
            }
            default: {
                if (c.get(Calendar.HOUR_OF_DAY) >= 0 && c.get(Calendar.HOUR_OF_DAY) < 8) {
                    //in night bonus
                    mightBeInNightBonus = true;
                }

                //check min arrive time
                c.setTimeInMillis(arrive.getTime() - (jToleranceField.isEnabled() ? arrivalTolerance : 0));
                if (c.get(Calendar.HOUR_OF_DAY) >= 0 && c.get(Calendar.HOUR_OF_DAY) < 8) {
                    mightBeInNightBonus = true;
                } else {
                    //check max arrive time
                    c.setTimeInMillis(arrive.getTime() + (jToleranceField.isEnabled() ? arrivalTolerance : 0));
                    if (c.get(Calendar.HOUR_OF_DAY) >= 0 && c.get(Calendar.HOUR_OF_DAY) < 8) {
                        mightBeInNightBonus = true;
                    }
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
        jLabel1 = new javax.swing.JLabel();
        jSendTime = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSendTimeFramesList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jSendTimeFrame = new com.visutools.nav.bislider.BiSlider();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jArriveTime = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jToleranceField = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        jEveryDayValid = new javax.swing.JRadioButton();
        jOnlyValidAt = new javax.swing.JRadioButton();
        jValidAtDay = new javax.swing.JSpinner();

        jLabel1.setText("Startzeit");

        jSendTime.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), new java.util.Date(), null, java.util.Calendar.SECOND));

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

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/remove.gif"))); // NOI18N
        jButton2.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton2.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton2.setPreferredSize(new java.awt.Dimension(23, 23));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveTimeFrameEvent(evt);
            }
        });

        jLabel3.setText("Ankunftszeit");

        jArriveTime.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), new java.util.Date(), null, java.util.Calendar.SECOND));

        jLabel4.setText("Toleranz");
        jLabel4.setEnabled(false);

        jLabel5.setText("+/-");
        jLabel5.setEnabled(false);

        jToleranceField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(java.text.NumberFormat.getIntegerInstance())));
        jToleranceField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jToleranceField.setText("10");
        jToleranceField.setEnabled(false);

        jLabel6.setText("Stunde(n)");
        jLabel6.setEnabled(false);

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

        jValidAtDay.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), new java.util.Date(), null, java.util.Calendar.DAY_OF_MONTH));
        jValidAtDay.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToleranceField))
                            .addComponent(jArriveTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6))
                    .addComponent(jEveryDayValid)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jOnlyValidAt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jValidAtDay))
                            .addComponent(jSendTimeFrame, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                            .addComponent(jSendTime, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(246, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSendTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSendTimeFrame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jEveryDayValid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jOnlyValidAt)
                    .addComponent(jValidAtDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jArriveTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jToleranceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))))
                .addContainerGap(74, Short.MAX_VALUE))
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

        DefaultListModel model = (DefaultListModel) jSendTimeFramesList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            TimeSpan frame = (TimeSpan) model.getElementAt(i);
            if (jEveryDayValid.isSelected()) {
                //check for every day option
                Point span = frame.getSpan();
                Line2D.Double currentFrame = new Line2D.Double(span.x + 0.1, 0, span.y - 0.1, 0);
                if (currentFrame.intersectsLine(newFrame)) {
                    intersection = i + 1;
                    break;
                }
            } else {
                if (frame.getAtDate() != null && frame.getAtDate().getTime() == ((Date) jValidAtDay.getValue()).getTime()) {
                    //check if date is the same
                    Point span = frame.getSpan();
                    Line2D.Double currentFrame = new Line2D.Double(span.x + 0.1, 0, span.y - 0.1, 0);
                    if (currentFrame.intersectsLine(newFrame)) {
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
                span = new TimeSpan(new Point(min, max));
            } else {
                span = new TimeSpan((Date) jValidAtDay.getValue(), new Point(min, max));
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JRadioButton jEveryDayValid;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JRadioButton jOnlyValidAt;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSendTime;
    private com.visutools.nav.bislider.BiSlider jSendTimeFrame;
    private javax.swing.JList jSendTimeFramesList;
    private javax.swing.JFormattedTextField jToleranceField;
    private javax.swing.JSpinner jValidAtDay;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.add(new TimePanel());
        f.pack();
        f.setVisible(true);


    }
}