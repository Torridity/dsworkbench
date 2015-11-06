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

import de.tor.tribes.types.Attack;
import de.tor.tribes.ui.components.ColoredProgressBar;
import de.tor.tribes.ui.components.TimerPanel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.SystrayHelper;
import de.tor.tribes.util.xml.JaxenUtils;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JSpinner.DateEditor;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Torridity
 */
public class ClockFrame extends javax.swing.JFrame implements ActionListener {

    private static Logger logger = Logger.getLogger("ClockFrame");

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("RemoveTimer")) {
            removeTimer((TimerPanel) e.getSource());
        }
    }
    private TimerThread tThread = null;
    private static ClockFrame SINGLETON = null;
    private ColoredProgressBar cp = null;
    private final List<TimerPanel> timers = new ArrayList<TimerPanel>();

    public static synchronized ClockFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ClockFrame();
        }
        return SINGLETON;
    }

    /**
     * Creates new form ClockFrame
     */
    ClockFrame() {
        initComponents();
        jSpinner1.setValue(new Date(System.currentTimeMillis()));
        ((DateEditor) jSpinner1.getEditor()).getFormat().applyPattern("dd.MM.yy HH:mm:ss.SSS");
        tThread = new TimerThread(this);
        tThread.start();

        String val = GlobalOptions.getProperty("clock.alwaysOnTop");
        if (val == null) {
            jCheckBox1.setSelected(false);
        } else {
            jCheckBox1.setSelected(Boolean.parseBoolean(val));

        }
        setAlwaysOnTop(jCheckBox1.isSelected());
        cp = new ColoredProgressBar(0, 1000);
        jPanel1.add(cp, BorderLayout.CENTER);

        jComboBox1.setModel(new DefaultComboBoxModel(new String[]{"Alarm", "Homer", "LetsGo", "NHL", "Roadrunner", "Schwing", "Sirene", "StarTrek1", "StarTrek2"}));
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.clock_tool", GlobalOptions.getHelpBroker().getHelpSet());
        }
        restoreTimers();
        // </editor-fold>
    }

    private void removeTimer(TimerPanel pPanel, boolean pSave) {
        jTimerContainer.remove(pPanel);
        timers.remove(pPanel);
        storeTimers();
    }

    private void removeTimer(TimerPanel pPanel) {
        removeTimer(pPanel, true);
    }

    protected void updateTime(String time, int millis) {
        if (isVisible()) {
            jLabel1.setText(time);
            double markerMin = 0;
            double markerMax = 1000;
            double diff = markerMax - markerMin;
            float ratio = 0;
            if (diff > 0) {
                ratio = (float) ((millis - markerMin) / (markerMax - markerMin));
            }

            Color c1 = Color.GREEN;
            if (millis >= 500) {
                c1 = Color.YELLOW;
            }
            Color c2 = Color.RED;
            if (millis < 500) {
                c2 = Color.YELLOW;
                ratio += .5f;
            } else {
                ratio -= .5f;
            }
            int red = (int) Math.rint(c2.getRed() * ratio + c1.getRed() * (1f - ratio));
            int green = (int) Math.rint(c2.getGreen() * ratio + c1.getGreen() * (1f - ratio));
            int blue = (int) Math.rint(c2.getBlue() * ratio + c1.getBlue() * (1f - ratio));

            red = (red < 0) ? 0 : red;
            green = (green < 0) ? 0 : green;
            blue = (blue < 0) ? 0 : blue;
            red = (red > 255) ? 255 : red;
            green = (green > 255) ? 255 : green;
            blue = (blue > 255) ? 255 : blue;
            cp.setForeground(new Color(red, green, blue));
            cp.setValue(millis);
        }

        for (TimerPanel p : timers.toArray(new TimerPanel[timers.size()])) {
            if (p.isExpired()) {
                playSound(p.getSound());
                SystrayHelper.showInfoMessage("Timer  '" + p.getName() + "' ist abgelaufen");
                removeTimer(p);
            } else {
                p.update();
            }
        }
    }

    private void addTimer() {
        String name = jTimerName.getText();
        if (name.length() < 1) {
            name = "Timer" + (timers.size() + 1);
        }

        TimerPanel panel = new TimerPanel(this, name, dateTimeField1.getSelectedDate().getTime(), (String) jComboBox1.getSelectedItem());
        timers.add(panel);
        jTimerContainer.add(panel);
        storeTimers();
    }

    public void addTimer(Attack pAttack, int pSecondsBefore) {
        if (pAttack == null) {
            return;
        }
        String name = pAttack.getSource() + " -> " + pAttack.getTarget();

        jTimerName.setText(name);
        dateTimeField1.setDate(new Date(pAttack.getSendTime().getTime() - (DateUtils.MILLIS_PER_SECOND * pSecondsBefore)));
        if (jComboBox1.getSelectedItem() == null) {
            jComboBox1.setSelectedIndex(0);
        }

        addTimer();
    }

    private void storeTimers() {
        try {
            FileWriter w = new FileWriter("timers.xml");
            w.write("<timers\n>");
            for (TimerPanel p : timers) {
                w.write(p.toXml());
            }
            w.write("</timers>");
            w.flush();
            w.close();
        } catch (IOException ioe) {
            logger.error("Failed to store timers", ioe);
        }
    }

    private void restoreTimers() {
        try {
            File timerFile = new File("timers.xml");
            if (timerFile.exists()) {
                String message = "Die folgenden Timer sind zwischenzeitlich abgelaufen:\n";
                long l = message.length();
                Document d = JaxenUtils.getDocument(timerFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//timers/timer")) {
                    TimerPanel p = new TimerPanel(this);
                    if (p.fromXml(e)) {
                        if (!p.isExpired()) {
                            timers.add(p);
                            jTimerContainer.add(p);
                        } else {
                            message += "* " + p.getName() + "\n";
                        }
                    } else {
                        logger.error("Failed to restore a timer");
                    }
                }
                if (message.length() > l) {
                    JOptionPaneHelper.showWarningBox(this, message, "Abgelaufene Timer");
                    storeTimers();
                }
            }
        } catch (Exception ioe) {
            logger.error("Failed to restore timers", ioe);
        }
    }

    public synchronized void playSound(String pSound) {
        Clip clip = null;
        AudioClip ac = null;
        try {
            if (org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS) {
                clip = AudioSystem.getClip();
                BufferedInputStream bin = new BufferedInputStream(ClockFrame.class.getResourceAsStream("/res/" + pSound + ".wav"));
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(bin);
                clip.open(inputStream);
                clip.start();
            } else {
                ac = Applet.newAudioClip(ClockFrame.class.getResource("/res/" + pSound + ".wav"));
                ac.play();
            }
        } catch (Exception e) {
            logger.error("Failed to play sound", e);
        }
        try {
            Thread.sleep(2500);
        } catch (Exception e) {
        }

        try {
            if (clip != null) {
                clip.stop();
                clip.flush();
                clip = null;
            }

            if (ac != null) {
                ac.stop();
                ac = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSpinner1 = new javax.swing.JSpinner();
        jActivateTimerButton = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox();
        dateTimeField1 = new de.tor.tribes.ui.components.DateTimeField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTestAlert = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTimerName = new org.jdesktop.swingx.JXTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTimerContainer = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        jSpinner1.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MILLISECOND));

        jActivateTimerButton.setBackground(new java.awt.Color(239, 235, 223));
        jActivateTimerButton.setText("Aktivieren");
        jActivateTimerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireActivateTimerEvent(evt);
            }
        });

        setTitle("Uhr");
        setMinimumSize(new java.awt.Dimension(280, 75));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setBackground(new java.awt.Color(239, 235, 223));
        jLabel1.setFont(new java.awt.Font("Verdana", 0, 36)); // NOI18N
        jLabel1.setText("Lade...");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel1.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(jLabel1, gridBagConstraints);

        jPanel1.setMinimumSize(new java.awt.Dimension(100, 20));
        jPanel1.setPreferredSize(new java.awt.Dimension(279, 20));
        jPanel1.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jCheckBox1.setText("Immer im Vordergrund");
        jCheckBox1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jCheckBox1.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jCheckBox1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireAlwaysOnTopChangedEvent(evt);
            }
        });
        jPanel2.add(jCheckBox1, java.awt.BorderLayout.SOUTH);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jComboBox1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanel3.add(dateTimeField1, gridBagConstraints);

        jLabel2.setText("Zeit");
        jLabel2.setMaximumSize(new java.awt.Dimension(80, 14));
        jLabel2.setMinimumSize(new java.awt.Dimension(80, 14));
        jLabel2.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Sound");
        jLabel3.setMaximumSize(new java.awt.Dimension(80, 14));
        jLabel3.setMinimumSize(new java.awt.Dimension(80, 14));
        jLabel3.setPreferredSize(new java.awt.Dimension(80, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel3, gridBagConstraints);

        jTestAlert.setText("Testen");
        jTestAlert.setMaximumSize(new java.awt.Dimension(81, 23));
        jTestAlert.setMinimumSize(new java.awt.Dimension(81, 23));
        jTestAlert.setPreferredSize(new java.awt.Dimension(81, 23));
        jTestAlert.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireTestSoundEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jTestAlert, gridBagConstraints);

        jButton1.setText("Timer erstellen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCreateTimer(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanel3.add(jButton1, gridBagConstraints);

        jLabel4.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanel3.add(jLabel4, gridBagConstraints);

        jTimerName.setPrompt("Bitte Timername eingeben");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanel3.add(jTimerName, gridBagConstraints);

        jPanel2.add(jPanel3, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(jPanel2, gridBagConstraints);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Aktive Timer"));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(246, 150));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(246, 150));

        jTimerContainer.setLayout(new java.awt.GridLayout(3, 3, 5, 5));
        jScrollPane1.setViewportView(jTimerContainer);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jButton2.setText("Alle Löschen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireRemoveAllTimersEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jButton2, gridBagConstraints);

        jButton3.setText("Auswahl löschen");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireRemoveSelectedTimersEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jButton3, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireActivateTimerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireActivateTimerEvent
    /*
     * if (jActivateTimerButton.isSelected()) { tThread.setNotifyTime(((Date) jSpinner1.getValue()).getTime()); } else {
     * tThread.setNotifyTime(-1); }
     */
}//GEN-LAST:event_fireActivateTimerEvent

private void fireAlwaysOnTopChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopChangedEvent
    setAlwaysOnTop(jCheckBox1.isSelected());
    GlobalOptions.addProperty("clock.alwaysOnTop", Boolean.toString(jCheckBox1.isSelected()));
}//GEN-LAST:event_fireAlwaysOnTopChangedEvent

private void fireTestSoundEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTestSoundEvent
    playSound((String) jComboBox1.getSelectedItem());
}//GEN-LAST:event_fireTestSoundEvent

    private void fireCreateTimer(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCreateTimer
        addTimer();
    }//GEN-LAST:event_fireCreateTimer

    private void fireRemoveAllTimersEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveAllTimersEvent
        for (TimerPanel p : timers.toArray(new TimerPanel[]{})) {
            removeTimer(p, false);
        }
        storeTimers();
        JOptionPaneHelper.showInformationBox(this, "Timer entfernt", "Alle Timer wurden entfernt.");
    }//GEN-LAST:event_fireRemoveAllTimersEvent

    private void fireRemoveSelectedTimersEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveSelectedTimersEvent
        int removed = 0;
        for (TimerPanel p : timers.toArray(new TimerPanel[]{})) {
            if (p.isSelected()) {
                removeTimer(p, false);
                removed++;
            }
        }
        storeTimers();
        JOptionPaneHelper.showInformationBox(this, "Timer entfernt", removed + " Timer wurden entfernt.");
    }//GEN-LAST:event_fireRemoveSelectedTimersEvent

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ClockFrame cf = new ClockFrame();
                cf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                cf.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.DateTimeField dateTimeField1;
    private javax.swing.JToggleButton jActivateTimerButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JButton jTestAlert;
    private javax.swing.JPanel jTimerContainer;
    private org.jdesktop.swingx.JXTextField jTimerName;
    // End of variables declaration//GEN-END:variables
}

class TimerThread extends Thread {

    private ClockFrame mParent;
    private final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");

    public TimerThread(ClockFrame pParent) {
        mParent = pParent;
        setName("ClockTimer");
        setDaemon(true);
    }

    public void setNotifyTime(long pTime) {
    }

    @Override
    public void run() {
        while (true) {
            long currentTime = System.currentTimeMillis();
            mParent.updateTime(FORMAT.format(new Date(currentTime)), (int) DateUtils.getFragmentInMilliseconds(new Date(), Calendar.SECOND));
            if (mParent.isVisible()) {
                mParent.repaint();
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                }
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
            }
        }
    }
}
