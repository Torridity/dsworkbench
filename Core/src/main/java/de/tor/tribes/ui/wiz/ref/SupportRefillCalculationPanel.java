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
package de.tor.tribes.ui.wiz.ref;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.TroopMovement;
import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.wiz.ref.types.REFSourceElement;
import de.tor.tribes.ui.wiz.ref.types.REFTargetElement;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.algo.AbstractAttackAlgorithm;
import de.tor.tribes.util.algo.BruteForce;
import de.tor.tribes.util.algo.Iterix;
import de.tor.tribes.util.algo.types.TimeFrame;
import java.awt.BorderLayout;
import java.awt.Point;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.time.DateUtils;
import org.netbeans.spi.wizard.*;

/**
 *
 * @author Torridity
 */
public class SupportRefillCalculationPanel extends WizardPage {
    
    private static final String GENERAL_INFO = "In diesem Schritt kannst du mögliche Unterstützungen für die eingegebenen Einstellungen errechnen lassen. "
            + "Was du nun noch brauchst ist eine Ankunftzeit. Alle Unterstützungen werden so berechnet, dass sie genau zu diesem Zeitpunkt ankommen. "
            + "Als früheste Abschickzeit wird die aktuelle Zeit gewählt, mögliche Abschickzeiten liegen zwischen jetzt und der eingestellten Ankunftzeit. "
            + "Drücke auf 'Unterstützungen berechnen' um die Berechnung zu starten.";
    private static SupportRefillCalculationPanel singleton = null;
    private AbstractAttackAlgorithm calculator = null;
    private SimpleDateFormat dateFormat = null;
    
    public static synchronized SupportRefillCalculationPanel getSingleton() {
        if (singleton == null) {
            singleton = new SupportRefillCalculationPanel();
        }
        return singleton;
    }

    /**
     * Creates new form AttackSourcePanel
     */
    SupportRefillCalculationPanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
        StyledDocument doc = (StyledDocument) jTextPane1.getDocument();
        Style defaultStyle = doc.addStyle("Default", null);
        StyleConstants.setItalic(defaultStyle, true);
        StyleConstants.setFontFamily(defaultStyle, "SansSerif");
        dateFormat = new SimpleDateFormat("HH:mm:ss");
    }
    
    public static String getDescription() {
        return "Berechnung";
    }
    
    public static String getStep() {
        return "id-ref-calculation";
    }
    
    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        profile.addProperty("ref.calculation.arrive", jArriveTime.getSelectedDate().getTime());
        if(jRadioNoArrive.isSelected()) {
            profile.addProperty("ref.calculation.arrive", "0");
        } else if(jRadioLastArrive.isSelected()) {
            profile.addProperty("ref.calculation.arrive", "1");
        } else if(jRadioFixedArrive.isSelected()) {
            profile.addProperty("ref.calculation.arrive", "2");
        }
    }
    
    public void restoreProperties() {
        calculator = null;
        UserProfile profile = GlobalOptions.getSelectedProfile();
        long date = System.currentTimeMillis();
        
        try {
            date = Long.parseLong(profile.getProperty("ref.calculation.arrive"));
        } catch (Exception ignored) {
        }
        jArriveTime.setDate(new Date(date));

        try {
            int arriveType = Integer.parseInt(profile.getProperty("ref.calculation.arrive"));
            jRadioNoArrive.setSelected((arriveType > 2 || arriveType < 1)?(true):(false));
            jRadioLastArrive.setSelected((arriveType == 1)?(true):(false));
            jRadioFixedArrive.setSelected((arriveType == 2)?(true):(false));
        } catch (Exception ignored) {
            jRadioNoArrive.setSelected(true);
            jRadioLastArrive.setSelected(false);
            jRadioFixedArrive.setSelected(false);
        }
        
        jRadioArriveActionPerformed(null);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jInfoScrollPane = new javax.swing.JScrollPane();
        jInfoTextPane = new javax.swing.JTextPane();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jCalculateButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jNeededSupports = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jAvailableSupports = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jPanel3 = new javax.swing.JPanel();
        jArriveTime = new de.tor.tribes.ui.components.DateTimeField();
        jBruteForce = new javax.swing.JRadioButton();
        jSystematicCalculation = new javax.swing.JRadioButton();
        jRadioLastArrive = new javax.swing.JRadioButton();
        jRadioFixedArrive = new javax.swing.JRadioButton();
        jRadioNoArrive = new javax.swing.JRadioButton();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html"); // NOI18N
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        setLayout(new java.awt.GridBagLayout());

        jXCollapsiblePane1.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jXCollapsiblePane1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Informationen einblenden");
        jLabel1.setToolTipText("Blendet Informationen zu dieser Ansicht und zu den Datenquellen ein/aus");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Informationen zur Berechnung"));
        jScrollPane1.setViewportView(jTextPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jScrollPane1, gridBagConstraints);

        jCalculateButton.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jCalculateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/select.png"))); // NOI18N
        jCalculateButton.setText("Unterstützungen berechnen");
        jCalculateButton.setMaximumSize(new java.awt.Dimension(240, 40));
        jCalculateButton.setMinimumSize(new java.awt.Dimension(240, 40));
        jCalculateButton.setPreferredSize(new java.awt.Dimension(240, 40));
        jCalculateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCalculateAttacksEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jCalculateButton, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Zusammenfassung"));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Notwendige Unterstützungen");
        jLabel2.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel2, gridBagConstraints);

        jNeededSupports.setText("10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jNeededSupports, gridBagConstraints);

        jLabel10.setText("Verfügbare Unterstützungen");
        jLabel10.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel10, gridBagConstraints);

        jAvailableSupports.setText("10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jAvailableSupports, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel1, gridBagConstraints);

        jProgressBar1.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jProgressBar1, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Einstellungen"));
        jPanel3.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jArriveTime, gridBagConstraints);

        buttonGroup1.add(jBruteForce);
        jBruteForce.setSelected(true);
        jBruteForce.setText("Zufällige Berechnung");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jBruteForce, gridBagConstraints);

        buttonGroup1.add(jSystematicCalculation);
        jSystematicCalculation.setText("Systematische Berechnung");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jSystematicCalculation, gridBagConstraints);

        buttonGroup2.add(jRadioLastArrive);
        jRadioLastArrive.setText("sp\u00E4teste Ankunftszeit");
        jRadioLastArrive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioArriveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel3.add(jRadioLastArrive, gridBagConstraints);

        buttonGroup2.add(jRadioFixedArrive);
        jRadioFixedArrive.setText("fixe Ankunftszeit");
        jRadioFixedArrive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioArriveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel3.add(jRadioFixedArrive, gridBagConstraints);

        buttonGroup2.add(jRadioNoArrive);
        jRadioNoArrive.setSelected(true);
        jRadioNoArrive.setText("Ohne Ankunftszeit");
        jRadioNoArrive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioArriveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel3.add(jRadioNoArrive, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        if (jXCollapsiblePane1.isCollapsed()) {
            jXCollapsiblePane1.setCollapsed(false);
            jLabel1.setText("Informationen ausblenden");
        } else {
            jXCollapsiblePane1.setCollapsed(true);
            jLabel1.setText("Informationen einblenden");
        }
    }//GEN-LAST:event_fireHideInfoEvent
    
    private void fireCalculateAttacksEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateAttacksEvent
        if (calculator == null) {//not used yet
            initializeCalculation();
        } else if (calculator.isRunning()) {//in use...abort
            calculator.abort();
            return;
        } else {//not in use...recalculate
            if (calculator.hasResults() && JOptionPaneHelper.showQuestionConfirmBox(this, "Vorherige Berechnung verwerfen?", "Berechnung verwerfen", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                //not recalculate
                return;
            } else {
                //recalculate
                initializeCalculation();
            }
        }
        
        if(calculator != null && !calculator.hasResults()) {
            //do only if there were no problems during initiation of calculation
            jCalculateButton.setText("Abbrechen");
            calculator.start();
            setBusy(true);
            //wait until calculation is running
            try {
                Thread.sleep(20);
            } catch (Exception ignored) {
            }
        }
    }//GEN-LAST:event_fireCalculateAttacksEvent

    private void jRadioArriveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioArriveActionPerformed
        jArriveTime.setEnabled(jRadioLastArrive.isSelected() || jRadioFixedArrive.isSelected());
    }//GEN-LAST:event_jRadioArriveActionPerformed
    
    protected TimeFrame getTimeFrame() {
        if(jRadioNoArrive.isSelected()) {
            //add 1 Jear to timespan to ensure that every movement is possible
            TimeFrame f = new TimeFrame(new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()),
                    new Date(System.currentTimeMillis() + 60 * 60 * 24 * 365 * 1000), new Date(System.currentTimeMillis() + 60 * 60 * 24 * 365 * 1000));
            
            f.addArriveTimeSpan(new TimeSpan(new IntRange(0,24)));
            f.addStartTimeSpan(new TimeSpan(new IntRange(0,24)));
            return f;
        } else if(jRadioLastArrive.isSelected()) {
            Date arrive = jArriveTime.getSelectedDate();
            TimeFrame f = new TimeFrame(new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), arrive, arrive);
            f.addStartTimeSpan(new TimeSpan(new LongRange(System.currentTimeMillis(), arrive.getTime())));
            f.addArriveTimeSpan(new TimeSpan(new IntRange(0,24)));
            return f;
        } else if(jRadioFixedArrive.isSelected()) {
            Date arrive = jArriveTime.getSelectedDate();
            TimeFrame f = new TimeFrame(new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), arrive, arrive);
            f.addArriveTimeSpan(new TimeSpan(arrive));
            f.addStartTimeSpan(new TimeSpan(new IntRange(0, 24)));
            return f;
        } else {
            notifyStatusUpdate("Kein Ankunftszeit Typ gew\u00E4hlt");
            notifyStatusUpdate("Berechnung abgebrochen!");
            return null;
        }
    }
    
    private void initializeCalculation() {
        TimeFrame f = getTimeFrame();
        
        if (f.getArriveRange().getMaximumLong() < System.currentTimeMillis()) {
            notifyStatusUpdate("Die gewählte Ankunftzeit liegt in der Vergangenheit");
            notifyStatusUpdate("Berechnung abgebrochen!");
            return;
        }
        if (jBruteForce.isSelected()) {
            calculator = new BruteForce();
        } else if (jSystematicCalculation.isSelected()) {
            calculator = new Iterix();
        } else {
            notifyStatusUpdate("Kein Berechnungsverfahren gew\u00E4hlt");
            notifyStatusUpdate("Berechnung abgebrochen!");
            return;
        }
        
        Hashtable<UnitHolder, List<Village>> sources = new Hashtable<>();
        UnitHolder slowest = SupportRefillSettingsPanel.getSingleton().getSplit().getSlowestUnit();
        
        List<Village> sourceVillages = new LinkedList<>();
        for (REFSourceElement element : SupportRefillSourcePanel.getSingleton().getAllElements()) {
            for (int i = 0; i < element.getAvailableSupports(); i++) {
                sourceVillages.add(element.getVillage());
            }
        }
        sources.put(slowest, sourceVillages);
        
        List<Village> targets = new LinkedList<>();
        Hashtable<Village, Integer> maxSupports = new Hashtable<>();
        for (REFTargetElement element : SupportRefillSettingsPanel.getSingleton().getAllElements()) {
            //ignore Targets that don't need any support, because the algorithm can't handle such targets
            if(element.getNeededSupports() <= 0) continue;
            targets.add(element.getVillage());
            maxSupports.put(element.getVillage(), element.getNeededSupports());
        }
        calculator.initialize(sources, new Hashtable<UnitHolder, List<Village>>(), targets, new LinkedList<Village>(), maxSupports, f, false);
        jProgressBar1.setValue(0);
        calculator.setLogListener(new AbstractAttackAlgorithm.LogListener() {
            
            @Override
            public void logMessage(String pMessage) {
                notifyStatusUpdate(pMessage);
            }
            
            @Override
            public void calculationFinished() {
                notifyCalculationFinished();
            }
            
            @Override
            public void updateProgress(double pPercent) {
                jProgressBar1.setValue((int) Math.rint(pPercent));
            }
        });
    }
    
    public void updateStatus() {
        int need = 0;
        for (REFTargetElement elem : SupportRefillSettingsPanel.getSingleton().getAllElements()) {
            need += Math.max(elem.getNeededSupports(), 0);
        }
        jNeededSupports.setText(Integer.toString(need));
        
        int available = 0;
        for (REFSourceElement elem : SupportRefillSourcePanel.getSingleton().getAllElements()) {
            available += elem.getAvailableSupports();
        }
        jAvailableSupports.setText(Integer.toString(available));
    }
    
    public void notifyCalculationFinished() {
        setBusy(false);
        if (calculator.hasResults()) {
            setProblem(null);
        } else {
            setProblem("Berechnung erzielte keine Ergebnisse");
        }
        jCalculateButton.setText("Unterstützungen berechnen");
    }
    
    public void notifyStatusUpdate(String pMessage) {
        try {
            StyledDocument doc = jTextPane1.getStyledDocument();
            doc.insertString(doc.getLength(), "(" + dateFormat.format(new Date(System.currentTimeMillis())) + ") " + pMessage + "\n", doc.getStyle("Info"));
            SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                    scroll();
                }
            });
        } catch (BadLocationException ignored) {
        }
    }
    
    private void scroll() {
        Point point = new Point(0, (int) (jTextPane1.getSize().getHeight()));
        JViewport vp = jScrollPane1.getViewport();
        if (vp == null) {
            return;
        }
        vp.setViewPosition(point);
    }
    
    public List<TroopMovement> getResults() {
        return calculator.getResults();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private de.tor.tribes.ui.components.DateTimeField jArriveTime;
    private javax.swing.JLabel jAvailableSupports;
    private javax.swing.JRadioButton jBruteForce;
    private javax.swing.JButton jCalculateButton;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jNeededSupports;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JRadioButton jRadioFixedArrive;
    private javax.swing.JRadioButton jRadioLastArrive;
    private javax.swing.JRadioButton jRadioNoArrive;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton jSystematicCalculation;
    private javax.swing.JTextPane jTextPane1;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (calculator == null) {
            setProblem("Noch keine Berechnung durchgeführt");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        
        if (calculator != null && calculator.isRunning()) {
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        SupportRefillFinishPanel.getSingleton().update();
        return WizardPanelNavResult.PROCEED;
    }
    
    @Override
    public WizardPanelNavResult allowBack(String string, Map map, Wizard wizard) {
        if (calculator != null && calculator.isRunning()) {
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        return WizardPanelNavResult.PROCEED;
        
    }
    
    @Override
    public WizardPanelNavResult allowFinish(String string, Map map, Wizard wizard) {
        if (calculator != null && calculator.isRunning()) {
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        return WizardPanelNavResult.PROCEED;
    }
}
