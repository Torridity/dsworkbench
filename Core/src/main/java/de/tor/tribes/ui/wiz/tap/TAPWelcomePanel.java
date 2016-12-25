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

import de.tor.tribes.ui.panels.TAPAttackInfoPanel;
import java.awt.event.ItemEvent;
import java.util.Map;
import javax.swing.ImageIcon;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 *
 * @author Torridity
 */
public class TAPWelcomePanel extends WizardPage {

    private final String ATTACK_HELP = "<html><b>Willkommen beim DS Workbench Taktikplaner.</b><br/><br/>"
            + "Du hast die Angriffsplanung gew&auml;hlt. Diese erlaubt es dir, Angriffe in gro&szlig;en Mengen zu erstellen. "
            + "Dabei bietet er dir die M&ouml;glichkeit, Abschick- und Ankunftzeiten nach deinen W&uuml;nschen zu gestalten. "
            + "Bitte beachte aber auch: Der automatische Angriffsplaner ist <b>NICHT</b> daf&uuml;r geeignet, die perfekten "
            + "Angriffspl&auml;ne inklusive Adelungen zu erstellen. F&uuml;r die Planung von Adelungen verwende bitte den "
            + "manuellen Angriffsplaner.</html>";
    private final String DEFENSE_HELP = "<html><b>Willkommen beim DS Workbench Taktikplaner.</b><br/><br/>"
            + "Du hast die Verteigungsplanung gew&auml;hlt. Diese erlaubt es dir, ausgehend von eingelesenen SOS-Anfragen, "
            + "einen Verteidigungsplan f&uuml;r die angegriffenen D&ouml;rfer zu erstellen. Die Verteidigungsplanung arbeitet "
            + "dabei eng mit dem SOS-Analyzer zusammen. SOS-Anfragen werden im SOS-Analyzer eingelesen, analysiert und k&ouml;nnen "
            + "dann von dort in die Verteidigungsplanung eingef&uuml;gt werden. DS Workbench kann anschlie&szlig;end versuchen, "
            + "mit den importierten Truppeninformationen einen Verteidigungsplan aufzustellen. Ergebnisse werden direkt in den SOS-Analyzer "
            + "zur&uuml;ck&uuml;bertragen und die Unterst&uuml;tzungsbefehle k&ouml;nnen dann von dort abgeschickt werden. Ebenso k&ouml;nnen "
            + "von dort aus Anfragen nach noch notwendigen Truppen gestellt werden.</html>";
    private final String REFILL_HELP = "<html><b>Willkommen beim DS Workbench Taktikplaner.</b><br/><br/>"
            + "Du hast die Auff&uuml;llung von Unterst&uuml;tzungen gew&auml;hlt. Diese erlaubt es dir, die Unterst&uuml;tzungen in deinen "
            + "D&ouml;rfern wieder auf einen bestimmten Stand zu bringen. Daf&uuml;r ist es notwendig, dass du deine Truppen aus dem Spiel "
            + "in DS Workbench importiert hast. W&auml;hrend der Auff&uuml;llung wird versucht, ausge&auml;hlte D&ouml;rfer mit freien Defensivtruppen "
            + "zu bef&uuml;llen, bis sie wieder einen bestimmten Truppenbestand aufweisen.</html>";
    private final String RETIME_HELP = "<html><b>Willkommen beim DS Workbench Taktikplaner.</b><br/><br/>"
            + "Der Retimer wird dazu verwendet, die Truppen von gegnerischen Angriffen bei der Rückkehr in ihr Heimatdorf zu<br/>"
            + "vernichten. Mit DS Workbench kannst du für einen oder mehrere Angriffe Retimes berechnen. Voraussetzung dafür ist, dass du deine Truppeninformationen<br/>"
            + "aus dem Spiel importiert hast. DS Workbench berechnet dir für alle gewünschten, eigenen Dörfer alle möglichen Retimes. Am Ende musst du nur noch<br/>"
            + "entscheiden, welche Retimes du wirklich abschicken möchtest. Falls du z.B. zur Abschickzeit eines Ramme-Retimes nicht Online sein kannst, so kannst<br/>"
            + "du den Retime mit Axtkämpfern wählen, der zwar eine geringere Durschlagskraft, aber auch eine kürzere Laufzeit hat.";
    private static TAPWelcomePanel singleton = null;
    public static final String TYPE = "type";
    public final static Integer ATTACK_TYPE = 0;
    public final static Integer DEFENSE_TYPE = 1;
    public final static Integer REFILL_TYPE = 2;
    public final static Integer RETIME_TYPE = 3;

    public static synchronized TAPWelcomePanel getSingleton() {
        if (singleton == null) {
            singleton = new TAPWelcomePanel();
        }
        return singleton;
    }

    /**
     * Creates new form EntryPanel
     */
    TAPWelcomePanel() {
        initComponents();
        jAttackButton.setIcon(new ImageIcon("./graphics/big/axe.png"));
        jDefenseButton.setIcon(new ImageIcon("./graphics/big/sword.png"));
        jRefillButton.setIcon(new ImageIcon("./graphics/big/def_refill.png"));
        jRetimeButton.setIcon(new ImageIcon("./graphics/big/retime.png"));
        jTextPane1.setText(ATTACK_HELP);
    }

    public static String getDescription() {
        return "Willkommen";
    }

    public static String getStep() {
        return "id-tap-welcome";
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jAttackButton = new javax.swing.JToggleButton();
        jDefenseButton = new javax.swing.JToggleButton();
        jRefillButton = new javax.swing.JToggleButton();
        jRetimeButton = new javax.swing.JToggleButton();

        setMinimumSize(new java.awt.Dimension(600, 600));
        setPreferredSize(new java.awt.Dimension(600, 600));
        setLayout(new java.awt.BorderLayout());

        jTextPane1.setContentType("text/html");
        jTextPane1.setEditable(false);
        jTextPane1.setText("<html>Willkommen beim DS Workbench Taktikplaner.<br/>\nIn den folgenden Schritten kannst du dir entweder einen Angriffs- oder einen Verteidigungsplan erstellen lassen. W&auml;hle bitte zuerst aus, welche Art von Plan du erstellen möchtest.</html>");
        jScrollPane1.setViewportView(jTextPane1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        buttonGroup1.add(jAttackButton);
        jAttackButton.setSelected(true);
        jAttackButton.setMaximumSize(new java.awt.Dimension(80, 80));
        jAttackButton.setMinimumSize(new java.awt.Dimension(80, 80));
        jAttackButton.setPreferredSize(new java.awt.Dimension(80, 80));
        jAttackButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireMethodChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jAttackButton, gridBagConstraints);

        buttonGroup1.add(jDefenseButton);
        jDefenseButton.setMaximumSize(new java.awt.Dimension(80, 80));
        jDefenseButton.setMinimumSize(new java.awt.Dimension(80, 80));
        jDefenseButton.setPreferredSize(new java.awt.Dimension(80, 80));
        jDefenseButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireMethodChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jDefenseButton, gridBagConstraints);

        buttonGroup1.add(jRefillButton);
        jRefillButton.setMaximumSize(new java.awt.Dimension(80, 80));
        jRefillButton.setMinimumSize(new java.awt.Dimension(80, 80));
        jRefillButton.setPreferredSize(new java.awt.Dimension(80, 80));
        jRefillButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireMethodChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jRefillButton, gridBagConstraints);

        buttonGroup1.add(jRetimeButton);
        jRetimeButton.setMaximumSize(new java.awt.Dimension(80, 80));
        jRetimeButton.setMinimumSize(new java.awt.Dimension(80, 80));
        jRetimeButton.setPreferredSize(new java.awt.Dimension(80, 80));
        jRetimeButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireMethodChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jRetimeButton, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void fireMethodChangeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireMethodChangeEvent

        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (evt.getSource() == jAttackButton) {
                jTextPane1.setText(ATTACK_HELP);
            } else if (evt.getSource() == jDefenseButton) {
                jTextPane1.setText(DEFENSE_HELP);
            } else if (evt.getSource() == jRefillButton) {
                jTextPane1.setText(REFILL_HELP);
            } else if (evt.getSource() == jRetimeButton) {
                jTextPane1.setText(RETIME_HELP);
            }
        }

    }//GEN-LAST:event_fireMethodChangeEvent
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JToggleButton jAttackButton;
    private javax.swing.JToggleButton jDefenseButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton jRefillButton;
    private javax.swing.JToggleButton jRetimeButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {

        int type = ATTACK_TYPE;
        if (jDefenseButton.isSelected()) {
            type = DEFENSE_TYPE;
        } else if (jRefillButton.isSelected()) {
            type = REFILL_TYPE;
        } else if (jRetimeButton.isSelected()) {
            type = RETIME_TYPE;
        }

        TAPAttackInfoPanel.getSingleton().setVisible(type == ATTACK_TYPE);

        map.put(TYPE, type);
        return WizardPanelNavResult.PROCEED;
    }

    @Override
    public WizardPanelNavResult allowBack(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.REMAIN_ON_PAGE;
    }

    @Override
    public WizardPanelNavResult allowFinish(String string, Map map, Wizard wizard) {
        return WizardPanelNavResult.REMAIN_ON_PAGE;
    }
}
