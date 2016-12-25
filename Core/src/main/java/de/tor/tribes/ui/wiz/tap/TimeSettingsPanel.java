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
package de.tor.tribes.ui.wiz.tap;

import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.ui.algo.AttackTimePanel;
import de.tor.tribes.ui.algo.SettingsChangedListener;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.algo.types.TimeFrame;
import java.awt.BorderLayout;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.time.DateUtils;
import org.netbeans.spi.wizard.*;

/**
 *
 * @author Torridity
 */
public class TimeSettingsPanel extends WizardPage implements SettingsChangedListener {

    @Override
    public void fireTimeFrameChangedEvent() {
        setProblem(null);
    }
    
    private static final String GENERAL_INFO = "Die Zeiteinstellungen dienen dazu, die m&ouml;glichen Angriffe zeitlich einzuordnen. "
            + "Die grobe Einordnung aller Angriffe geschieht &uuml;ber die beiden Felder 'Startdatum' und 'Enddatum'. DS Workbench "
            + "wird keinen Angriff vor bzw. nach diesen Daten planen. Je gr&ouml;&szlig;er der Abstand zwischen diesen Zeitpunkten ist, "
            + "desto l&auml;nger dauert die Berechnung.<br/>"
            + "Eine genauere Festlegung von Abschick- und Ankunftzeiten geschieht anschlie&szlig;end &uuml;ber die Zeitrahmen."
            + "Hier gibt es die folgenden M&ouml;glichkeiten:"
            + "<ul><li>Immer: Zeitrahmen von diesem Typ gelten an jedem Tag zwischen den angegebenen Stunden</li> "
            + "<li>Tag: Zeitrahmen von diesem Typ gelten nur an dem festgelegten Tag zwischen den angegebenen Stunden</li>"
            + "<li>Zeitpunkt: Elemente von diesem Typ gelten an dem festgelegten Tag und zu der festgelegten Uhrzeit. Sie sind daher vorrangig f&uuml;r die Ankunft von Angriffen gedacht.</li>"
            + "</ul>"
            + "Eingestellte Zeitrahmen werden per Drag&amp;Drop in die Zeitrahmenliste gezogen und k&ouml;nnen mit ENTF wieder gel&ouml;scht werden."
            + "Um eine Berechnung durchzuf&uuml;hren, wird mindestens ein Abschick- und ein Ankunftzeitrahmen ben&ouml;tigt.</html>";
    private static TimeSettingsPanel singleton = null;
    private AttackTimePanel timePanel = null;

    public static synchronized TimeSettingsPanel getSingleton() {
        if (singleton == null) {
            singleton = new TimeSettingsPanel();
        }
        return singleton;
    }

    /**
     * Creates new form AttackSourcePanel
     */
    TimeSettingsPanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
        timePanel = new AttackTimePanel(this);
        jPanel1.add(timePanel, BorderLayout.CENTER);
    }

    public static String getDescription() {
        return "Zeiteinstellungen";
    }

    public static String getStep() {
        return "id-attack-time";
    }

    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();

        profile.addProperty("attack.frame.start", Long.toString(timePanel.getStartTime().getTime()));
        profile.addProperty("attack.frame.arrive", Long.toString(timePanel.getArriveTime().getTime()));

        String spanProp = "";
        for (TimeSpan span : timePanel.getTimeSpans()) {
            spanProp += span.toPropertyString() + ";";
        }
        profile.addProperty("attack.frame.spans", spanProp);
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

            timePanel.setStartTime(new Date(start));
            timePanel.setArriveTime(new Date(arrive));
            // <editor-fold defaultstate="collapsed" desc="Restore time spans">
            //restore send spans
            String spanProp = profile.getProperty("attack.frame.spans");
            if (spanProp == null) {
                spanProp = "";
            }
            String[] spans = spanProp.split(";");

            List<TimeSpan> spanList = new LinkedList<>();
            for (String span : spans) {
                try {
                    TimeSpan s = TimeSpan.fromPropertyString(span);
                    if (s != null) {
                        spanList.add(s);
                    }
                } catch (Exception invalid) {
                }
            }

            timePanel.setTimeSpans(spanList);
            // </editor-fold>
        } catch (Exception e) {
            timePanel.reset();
        }
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
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html");
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

        jPanel1.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel1, gridBagConstraints);
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

    public TimeFrame getTimeFrame() {
        return timePanel.getTimeFrame();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (!getTimeFrame().isValid()) {
            setProblem("Zeiteinstellungen unvollständig");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        ValidationPanel.getSingleton().setup();
        return WizardPanelNavResult.PROCEED;
    }

    @Override
    public WizardPanelNavResult allowBack(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;

    }

    @Override
    public WizardPanelNavResult allowFinish(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.PROCEED;
    }
}
