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
package de.tor.tribes.ui.algo;

import de.tor.tribes.types.DefenseTimeSpan;
import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.algo.types.TimeFrame;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

/**
 * @author Torridity
 */
public class SettingsPanel extends javax.swing.JPanel {

    private static Logger logger = Logger.getLogger("AttackPlannerSettings");
    private AttackTimePanel timeSettingsPanel = null;

    /**
     * Creates new form TimePanel
     */
    public SettingsPanel(SettingsChangedListener pListener) {
        initComponents();
        // setBackground(Constants.DS_BACK_LIGHT);
        // jSendTimeSettingsPanel.setSettingsChangedListener(this);
        // jArriveTimeSettingsPanel.setSettingsChangedListener(this);
        timeSettingsPanel = new AttackTimePanel(pListener);
        jPanel2.add(timeSettingsPanel, BorderLayout.CENTER);
        reset();
    }

    public void reset() {
        // jSendTimeSettingsPanel.reset();
        // jArriveTimeSettingsPanel.reset();
        timeSettingsPanel.reset();
        timeFrameVisualizer1.setScrollPane(jScrollPane1);
        restoreProperties();
    }

    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();

        profile.addProperty("attack.frame.start", Long.toString(timeSettingsPanel.getStartTime().getTime()));
        profile.addProperty("attack.frame.arrive", Long.toString(timeSettingsPanel.getArriveTime().getTime()));

        String spanProp = "";
        for (TimeSpan span : timeSettingsPanel.getTimeSpans()) {
            spanProp += span.toPropertyString() + ";";
        }
        profile.addProperty("attack.frame.spans", spanProp);

        profile.addProperty("attack.frame.algo.type", Integer.toString(jAlgoBox.getSelectedIndex()));
        profile.addProperty("attack.frame.fake.off.targets", Boolean.toString(jFakeOffTargetsBox.isSelected()));
    }

    public void restoreProperties() {
        try {
            UserProfile profile = GlobalOptions.getSelectedProfile();
            String val = profile.getProperty("attack.frame.start");
            long start = (val != null) ? Long.parseLong(val) : System.currentTimeMillis();
            val = profile.getProperty("attack.frame.arrive");
            long arrive = (val != null) ? Long.parseLong(val) : System.currentTimeMillis();

            if (start < System.currentTimeMillis()) {
                start = System.currentTimeMillis();
            }

            if (arrive < System.currentTimeMillis()) {
                arrive = System.currentTimeMillis() + DateUtils.MILLIS_PER_HOUR;
            }

            timeSettingsPanel.setStartTime(new Date(start));
            timeSettingsPanel.setArriveTime(new Date(arrive));
            val = profile.getProperty("attack.frame.algo.type");
            jAlgoBox.setSelectedIndex((val != null) ? Integer.parseInt(val) : 0);
            jFakeOffTargetsBox.setSelected(Boolean.parseBoolean(profile.getProperty("attack.frame.fake.off.targets")));

            // <editor-fold defaultstate="collapsed" desc="Restore time spans">
            //restore send spans
            String spanProp = profile.getProperty("attack.frame.spans");
            if (spanProp == null) {
                spanProp = "";
            }
            String[] spans = spanProp.split(";");

            List<TimeSpan> spanList = new LinkedList<TimeSpan>();
            for (String span : spans) {
                try {
                    TimeSpan s = TimeSpan.fromPropertyString(span);
                    if (s != null) {
                        spanList.add(s);
                    }
                } catch (Exception invalid) {
                }
            }

            timeSettingsPanel.setTimeSpans(spanList);
            // </editor-fold>
        } catch (Exception e) {
            logger.error("Failed to restore attack planer settings", e);
            timeSettingsPanel.reset();
        }
    }

    /**
     * Return selected send time frames
     */
    public TimeFrame getTimeFrame() {
        return timeSettingsPanel.getTimeFrame();
    }

    public void addTimeSpanExternally(DefenseTimeSpan pSpan) {
        timeSettingsPanel.addDefenseTimeSpan(pSpan);
    }

    public boolean validatePanel() {
        boolean result = true;
        try {
            timeSettingsPanel.validateSettings();
        } catch (RuntimeException re) {
            String message = re.getMessage();
            if (message == null) {
                logger.error("Unexpected exception while validating", re);
                message = "Unerwarteter Fehler bei der Validierung der Einstellungen. Bitte wenden dich an den Support.";
            }
            if (message.indexOf("Nachtbonus") > -1 || message.indexOf("Vergangenheit") > -1) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, message + "\nMöchtest du fortfahren?", "Warnung", "Nein", "Ja") == JOptionPane.YES_OPTION) {
                    result = true;
                } else {
                    result = false;
                }
            } else {
                JOptionPaneHelper.showWarningBox(this, message, "Fehler");
                result = false;
            }
        }

        return result;
    }

    /**
     * Return whether to use BruteForce or Iterix as algorithm
     */
    public boolean useBruteForce() {
        return (jAlgoBox.getSelectedIndex() == 0);
    }

    public boolean fakeOffTargets() {
        return jFakeOffTargetsBox.isSelected();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jAlgoBox = new javax.swing.JComboBox();
        jFakeOffTargetsBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        timeFrameVisualizer1 = new de.tor.tribes.ui.algo.TimeFrameVisualizer();
        jPanel2 = new javax.swing.JPanel();

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Sonstige Einstellungen"));
        jPanel3.setOpaque(false);

        jLabel6.setText("Zielsuche");

        jAlgoBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Zufällig", "Systematisch" }));
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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                        .addComponent(jAlgoBox, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jFakeOffTargetsBox))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jAlgoBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jFakeOffTargetsBox)
                .addContainerGap(194, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Zeitrahmendarstellung"));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/refresh.png"))); // NOI18N
        jButton1.setToolTipText("Zeitrahmendarstellung aktualisieren");
        jButton1.setMaximumSize(new java.awt.Dimension(25, 25));
        jButton1.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton1.setPreferredSize(new java.awt.Dimension(25, 25));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRefreshTimeFrameVisualizerEvent(evt);
            }
        });

        javax.swing.GroupLayout timeFrameVisualizer1Layout = new javax.swing.GroupLayout(timeFrameVisualizer1);
        timeFrameVisualizer1.setLayout(timeFrameVisualizer1Layout);
        timeFrameVisualizer1Layout.setHorizontalGroup(
            timeFrameVisualizer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 643, Short.MAX_VALUE)
        );
        timeFrameVisualizer1Layout.setVerticalGroup(
            timeFrameVisualizer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 98, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(timeFrameVisualizer1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Zeiteinstellungen"));
        jPanel2.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fireRefreshTimeFrameVisualizerEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRefreshTimeFrameVisualizerEvent
        timeFrameVisualizer1.refresh(getTimeFrame());
    }//GEN-LAST:event_fireRefreshTimeFrameVisualizerEvent
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jAlgoBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jFakeOffTargetsBox;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private de.tor.tribes.ui.algo.TimeFrameVisualizer timeFrameVisualizer1;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final SettingsPanel sp = new SettingsPanel(null);
        f.add(sp);
        f.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
        f.pack();
        f.setVisible(true);
    }
}
