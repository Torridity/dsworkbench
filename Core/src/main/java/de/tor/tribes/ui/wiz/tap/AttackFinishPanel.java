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

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.StandardAttack;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.VillageOverviewMapPanel;
import de.tor.tribes.ui.models.TAPResultDetailsTableModel;
import de.tor.tribes.ui.models.TAPResultTableModel;
import de.tor.tribes.ui.renderer.*;
import de.tor.tribes.ui.util.ColorGradientHelper;
import de.tor.tribes.ui.windows.AttackTransferDialog;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackSourceElement;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackTargetElement;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.algo.types.TimeFrame;
import de.tor.tribes.util.attack.StandardAttackManager;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class AttackFinishPanel extends WizardPage {

    private static final String GENERAL_INFO = "<html>In diesem abschlie&szlig;enden Schritt werden alle Ergebnisse der Berechnung angezeigt. "
            + "In der oberen Tabelle sind alle Ziele aufgelistet, sowie die prozentuale Angabe, wieviele der unter 'Ziele' angegebenen Anzahl von Angriffen auf "
            + "dieses Dorf zugeteilt werden konnten. Die Einzelangriff kannst du dir anzeigen lassen, indem du eins oder mehrere Zeilen ausw&auml;hlst und auf den "
            + "Button 'Details' klickst.<br/>"
            + "Bist du mit dem Ergebnis zufrieden, hast du im unteren Bereich verschiedene M&ouml;glichkeiten, alle oder ausgew&auml;hlte Angriffe in einen neuen "
            + "Angriffsplaner der Angriffs&uuml;bersicht zu &uuml;bertragen. Fahre mit der Maus &uuml;ber die einzelnen Buttons, um dir einen Tooltip mit der "
            + "entsprechenden Erkl&auml;rung anzeigen zu lassen."
            + "</html>";
    private static AttackFinishPanel singleton = null;
    private VillageOverviewMapPanel overviewPanel = null;

    public static synchronized AttackFinishPanel getSingleton() {
        if (singleton == null) {
            singleton = new AttackFinishPanel();
        }
        return singleton;
    }

    public static String getDescription() {
        return "Fertig";
    }

    public static String getStep() {
        return "id-attack-finish";
    }

    /**
     * Creates new form AttackSourcePanel
     */
    AttackFinishPanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
        jButton1.setIcon(new ImageIcon("./graphics/big/axe.png"));
        jButton2.setIcon(new ImageIcon("./graphics/big/axe_unfilled.png"));
        jxResultsTable.setModel(new TAPResultTableModel());
        jxResultsTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jxResultsTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        TAPResultDetailsTableModel model = new TAPResultDetailsTableModel();
        jXDetailsTable.setModel(model);
        jXDetailsTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jXDetailsTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jXDetailsTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jXDetailsTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jXDetailsTable.setDefaultRenderer(Integer.class, new NoteIconCellRenderer(NoteIconCellRenderer.ICON_TYPE.NOTE));
        overviewPanel = new VillageOverviewMapPanel();
        jPanel5.add(overviewPanel, BorderLayout.CENTER);
        jXCollapsiblePane2.add(jSummaryPanel, BorderLayout.CENTER);
        StandardAttackManager.getSingleton().addManagerListener(new GenericManagerListener() {

            @Override
            public void dataChangedEvent() {
                updateStandardAttacks();
            }

            @Override
            public void dataChangedEvent(String pGroup) {
                dataChangedEvent();
            }
        });
    }

    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        profile.addProperty("tap.finish.expert", jExpertView.isSelected());
        profile.addProperty("tap.finish.std.off", jStandardOff.getSelectedItem());
        profile.addProperty("tap.finish.std.fake", jStandardFake.getSelectedItem());
    }

    public void restoreProperties() {
        getModel().clear();
        getResultModel().clear();
        UserProfile profile = GlobalOptions.getSelectedProfile();
        jExpertView.setSelected(Boolean.parseBoolean(profile.getProperty("tap.finish.expert")));
        changeExpertView();
        updateStandardAttacks();
    }

    private void updateStandardAttacks() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        DefaultComboBoxModel offModel = new DefaultComboBoxModel();
        DefaultComboBoxModel fakeModel = new DefaultComboBoxModel();

        for (ManageableType t : StandardAttackManager.getSingleton().getAllElements()) {
            StandardAttack a = (StandardAttack) t;
            offModel.addElement(a.getName());
            fakeModel.addElement(a.getName());
        }
        jStandardOff.setModel(offModel);
        jStandardFake.setModel(fakeModel);

        String val = profile.getProperty("tap.finish.std.off");
        if (val == null || StandardAttackManager.getSingleton().isAllowedName(val)) {//no value set or std attack not used
            val = StandardAttackManager.getSingleton().getElementByIcon(StandardAttack.OFF_ICON).getName();
        }
        jStandardOff.setSelectedItem(val);


        val = profile.getProperty("tap.finish.std.fake");
        if (val == null || StandardAttackManager.getSingleton().isAllowedName(val)) {//no value set or std attack not used
            val = StandardAttackManager.getSingleton().getElementByIcon(StandardAttack.FAKE_ICON).getName();
        }
        jStandardFake.setSelectedItem(val);
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
        jScrollPane2 = new javax.swing.JScrollPane();
        jXDetailsTable = new org.jdesktop.swingx.JXTable();
        jSummaryPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jAttackedTargets = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jOverallAttacks = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jUsedSourceVillages = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPerfectTargets = new javax.swing.JLabel();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jxResultsTable = new org.jdesktop.swingx.JXTable();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSlider1 = new javax.swing.JSlider();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jXLabel2 = new org.jdesktop.swingx.JXLabel();
        jStandardOff = new javax.swing.JComboBox();
        jStandardFake = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jExpertView = new javax.swing.JCheckBox();
        jToggleButton2 = new javax.swing.JToggleButton();
        jXCollapsiblePane2 = new org.jdesktop.swingx.JXCollapsiblePane();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setEditable(false);
        jInfoTextPane.setContentType("text/html"); // NOI18N
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Angriffsdetails"));

        jXDetailsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jXDetailsTable);

        jSummaryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Zusammenfassung"));
        jSummaryPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Angegriffene Ziele");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSummaryPanel.add(jLabel2, gridBagConstraints);

        jAttackedTargets.setText("0");
        jAttackedTargets.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSummaryPanel.add(jAttackedTargets, gridBagConstraints);

        jLabel4.setText("Zugeteilte Angriffe");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSummaryPanel.add(jLabel4, gridBagConstraints);

        jOverallAttacks.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSummaryPanel.add(jOverallAttacks, gridBagConstraints);

        jLabel6.setText("Verwendete Herkunftsdörfer");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSummaryPanel.add(jLabel6, gridBagConstraints);

        jUsedSourceVillages.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSummaryPanel.add(jUsedSourceVillages, gridBagConstraints);

        jLabel3.setText("Voll belegte Ziele");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSummaryPanel.add(jLabel3, gridBagConstraints);

        jPerfectTargets.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jSummaryPanel.add(jPerfectTargets, gridBagConstraints);

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

        jPanel2.setMinimumSize(new java.awt.Dimension(650, 394));
        jPanel2.setPreferredSize(new java.awt.Dimension(650, 613));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Angegriffene Ziele"));

        jxResultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jxResultsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jScrollPane1, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Abschließende Aktionen"));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/48x48/half_axe_clipboard.png"))); // NOI18N
        jButton1.setToolTipText("Alle Angriffe in die Befehlsübersicht übertragen");
        jButton1.setMaximumSize(new java.awt.Dimension(70, 70));
        jButton1.setMinimumSize(new java.awt.Dimension(70, 70));
        jButton1.setPreferredSize(new java.awt.Dimension(70, 70));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferAllToAttackPlanEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 20);
        jPanel3.add(jButton1, gridBagConstraints);

        jButton2.setToolTipText("<html>Ziele entfernen, auf die weniger als die unten angezeigte Prozentzahl der max. Angriffe laufen.<br/>\nDurch die Entfernung werden ggf. Herkunftsd&ouml;rfer 'frei', die für andere Ziele verwendet werden können.</html>");
        jButton2.setMaximumSize(new java.awt.Dimension(70, 70));
        jButton2.setMinimumSize(new java.awt.Dimension(70, 70));
        jButton2.setPreferredSize(new java.awt.Dimension(70, 70));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRecalculateEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 20);
        jPanel3.add(jButton2, gridBagConstraints);

        jSlider1.setMajorTickSpacing(10);
        jSlider1.setMinimum(10);
        jSlider1.setMinorTickSpacing(5);
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setMinimumSize(new java.awt.Dimension(200, 60));
        jSlider1.setPreferredSize(new java.awt.Dimension(200, 60));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 20);
        jPanel3.add(jSlider1, gridBagConstraints);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/48x48/full_axe_clipboard.png"))); // NOI18N
        jButton3.setToolTipText("Nur volle Angriffe in die Befehlsübersicht übertragen");
        jButton3.setMaximumSize(new java.awt.Dimension(70, 70));
        jButton3.setMinimumSize(new java.awt.Dimension(70, 70));
        jButton3.setPreferredSize(new java.awt.Dimension(70, 70));
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFullToAttackPlanEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 20);
        jPanel3.add(jButton3, gridBagConstraints);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/48x48/selection_axe_clipboard.png"))); // NOI18N
        jButton4.setToolTipText("Ausgewählte Angriffe in die Befehlsübersicht übertragen");
        jButton4.setMaximumSize(new java.awt.Dimension(70, 70));
        jButton4.setMinimumSize(new java.awt.Dimension(70, 70));
        jButton4.setPreferredSize(new java.awt.Dimension(70, 70));
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireSelectedToAttackPlanEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 20);
        jPanel3.add(jButton4, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setMinimumSize(new java.awt.Dimension(351, 60));
        jPanel1.setPreferredSize(new java.awt.Dimension(361, 60));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jXLabel1.setText("Standardangriff für Offs:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jXLabel1, gridBagConstraints);

        jXLabel2.setText("Standardangriff für Fakes:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jXLabel2, gridBagConstraints);

        jStandardOff.setMinimumSize(new java.awt.Dimension(50, 18));
        jStandardOff.setPreferredSize(new java.awt.Dimension(50, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jStandardOff, gridBagConstraints);

        jStandardFake.setMinimumSize(new java.awt.Dimension(50, 18));
        jStandardFake.setPreferredSize(new java.awt.Dimension(50, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jStandardFake, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 20);
        jPanel3.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel3, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel5.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel5.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 0);
        jPanel4.add(jPanel5, gridBagConstraints);

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/search.png"))); // NOI18N
        jToggleButton1.setToolTipText("Informationskarte vergrößern");
        jToggleButton1.setMaximumSize(new java.awt.Dimension(100, 23));
        jToggleButton1.setMinimumSize(new java.awt.Dimension(100, 23));
        jToggleButton1.setPreferredSize(new java.awt.Dimension(100, 23));
        jToggleButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireViewStateChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel4.add(jToggleButton1, gridBagConstraints);

        jExpertView.setText("Expertenansicht");
        jExpertView.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeExpertViewEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel4.add(jExpertView, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel4, gridBagConstraints);

        jToggleButton2.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jToggleButton2.setText("Zusammenfassung anzeigen");
        jToggleButton2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireShowHideSummaryEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jToggleButton2, gridBagConstraints);

        jXCollapsiblePane2.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jXCollapsiblePane2, gridBagConstraints);

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

    private void fireRecalculateEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRecalculateEvent
        float max = (float) jSlider1.getValue();
        List<Village> targetsToRemove = new LinkedList<>();
        for (int i = 0; i < getModel().getRowCount(); i++) {
            Village target = (Village) getModel().getValueAt(i, 1);
            float perc = (Float) getModel().getValueAt(i, 2);
            perc *= 100f;
            if (perc < max) {
                targetsToRemove.add(target);
            }
        }

        if (!targetsToRemove.isEmpty()) {
            if (AttackTargetPanel.getSingleton().removeTargets(targetsToRemove)) {
                JOptionPaneHelper.showInformationBox(this, targetsToRemove.size() + " Ziel(e) wurden entfernt. Wechsle nun zum vorherigen Schritt\n"
                        + "und führe die Berechnung erneut durch.", "Information");
                ValidationPanel.getSingleton().setup();
                AttackSourceFilterPanel.getSingleton().setup();
            } else {
                JOptionPaneHelper.showInformationBox(this, "Es wurden alle Ziele entfernt.\nBitte wähle neue Ziele oder verwende die bisherigen Erbgenisse.", "Information");
            }
        } else {
            JOptionPaneHelper.showInformationBox(this, "Keine entsprechenden Ziele gefunden.", "Information");
        }
    }//GEN-LAST:event_fireRecalculateEvent

    private void fireViewStateChangeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireViewStateChangeEvent
        if (jToggleButton1.isSelected()) {
            overviewPanel.setOptimalSize(2);
            jScrollPane1.setViewportView(overviewPanel);
            jPanel2.remove(overviewPanel);
        } else {
            changeExpertView();
            jPanel5.add(overviewPanel, BorderLayout.CENTER);

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    jPanel5.updateUI();
                }
            });
        }
    }//GEN-LAST:event_fireViewStateChangeEvent

    private void fireShowHideSummaryEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireShowHideSummaryEvent
        jXCollapsiblePane2.setCollapsed(!jToggleButton2.isSelected());
    }//GEN-LAST:event_fireShowHideSummaryEvent

    private void fireTransferAllToAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferAllToAttackPlanEvent
        List<Attack> attacks = new LinkedList<>();
        for (int row = 0; row < jxResultsTable.getRowCount(); row++) {
            int modelRow = jxResultsTable.convertRowIndexToModel(row);
            AbstractTroopMovement move = getModel().getRow(modelRow);
            attacks.addAll(Arrays.asList(move.getFinalizedAttacks()));
        }
        transferToAttackView(attacks);
    }//GEN-LAST:event_fireTransferAllToAttackPlanEvent

    private void fireFullToAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireFullToAttackPlanEvent
        List<Attack> attacks = new LinkedList<>();
        for (int row = 0; row < jxResultsTable.getRowCount(); row++) {
            int modelRow = jxResultsTable.convertRowIndexToModel(row);
            AbstractTroopMovement move = getModel().getRow(modelRow);
            if (move.offComplete()) {
                attacks.addAll(Arrays.asList(move.getFinalizedAttacks()));
            }
        }
        transferToAttackView(attacks);
    }//GEN-LAST:event_fireFullToAttackPlanEvent

    private void fireSelectedToAttackPlanEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireSelectedToAttackPlanEvent
        if (jScrollPane1.getViewport().getView().equals(jxResultsTable)) {
            int[] selection = jxResultsTable.getSelectedRows();
            List<Attack> attacks = new LinkedList<>();
            for (int row : selection) {
                int modelRow = jxResultsTable.convertRowIndexToModel(row);
                AbstractTroopMovement move = getModel().getRow(modelRow);
                attacks.addAll(Arrays.asList(move.getFinalizedAttacks()));
            }
            transferToAttackView(attacks);
        } else if (jScrollPane1.getViewport().getView().equals(jXDetailsTable)) {
            int[] selection = jXDetailsTable.getSelectedRows();
            List<Attack> attacks = new LinkedList<>();
            for (int row : selection) {
                int modelRow = jXDetailsTable.convertRowIndexToModel(row);
                Attack a = getResultModel().getRow(modelRow);
                attacks.add(a);
            }
            transferToAttackView(attacks);
        }
    }//GEN-LAST:event_fireSelectedToAttackPlanEvent

    private void fireChangeExpertViewEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeExpertViewEvent
        changeExpertView();
    }//GEN-LAST:event_fireChangeExpertViewEvent

    private void changeExpertView() {
        if (jExpertView.isSelected()) {
            jScrollPane1.setViewportView(jXDetailsTable);
            jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Zieldetails"));
        } else {
            jScrollPane1.setViewportView(jxResultsTable);
            jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Angegriffene Ziele"));
        }
    }

    private void transferToAttackView(List<Attack> pToTransfer) {
        if (pToTransfer.isEmpty()) {
            JOptionPaneHelper.showInformationBox(this, "Keine Angriffe gewählt", "Information");
            return;
        }

        //modify attack types
        String stdOff = (String) jStandardOff.getSelectedItem();
        if (stdOff == null) {
            stdOff = StandardAttackManager.getSingleton().getElementByIcon(StandardAttack.OFF_ICON).getName();
        }

        String stdFake = (String) jStandardFake.getSelectedItem();
        if (stdFake == null) {
            stdFake = StandardAttackManager.getSingleton().getElementByIcon(StandardAttack.FAKE_ICON).getName();
        }

        List<Attack> modifiedTransfer = new LinkedList<>();
        for (Attack a : pToTransfer) {
            Attack newAttack = new Attack(a);

            if (a.getType() == Attack.FAKE_TYPE) {
                newAttack.setType(StandardAttackManager.getSingleton().getElementByName(stdFake).getIcon());
            } else if (a.getType() == Attack.CLEAN_TYPE) {
                newAttack.setType(StandardAttackManager.getSingleton().getElementByName(stdOff).getIcon());
            }
            modifiedTransfer.add(newAttack);
        }

        new AttackTransferDialog(TacticsPlanerWizard.getFrame(), true).setupAndShow(modifiedTransfer.toArray(new Attack[modifiedTransfer.size()]));
    }

    private TAPResultTableModel getModel() {
        return (TAPResultTableModel) jxResultsTable.getModel();
    }

    private TAPResultDetailsTableModel getResultModel() {
        return (TAPResultDetailsTableModel) jXDetailsTable.getModel();
    }

    public void update() {
        List<AbstractTroopMovement> results = AttackCalculationPanel.getSingleton().getResults();
        TimeFrame timeFrame = TimeSettingsPanel.getSingleton().getTimeFrame();
        List<Long> used = new LinkedList<>();
        TAPResultTableModel model = new TAPResultTableModel();
        int perfectOffs = 0;
        int maxAttacks = 0;
        int assignedAttacks = 0;
        int attackedTargets = 0;
        List<Village> usedSources = new LinkedList<>();
        overviewPanel.reset();

        for (TAPAttackSourceElement elem : AttackSourceFilterPanel.getSingleton().getFilteredElements()) {
            overviewPanel.addVillage(new Point(elem.getVillage().getX(), elem.getVillage().getY()), Color.BLACK);
        }

        for (TAPAttackTargetElement elem : AttackTargetPanel.getSingleton().getAllElements()) {
            overviewPanel.addVillage(new Point(elem.getVillage().getX(), elem.getVillage().getY()), Color.BLACK);
        }

        for (AbstractTroopMovement result : results) {
            result.finalizeMovement(timeFrame, used);
            if (result.offComplete()) {
                perfectOffs++;
            }
            maxAttacks += result.getMaxOffs();
            int offCount = result.getOffCount();
            assignedAttacks += offCount;

            if (offCount > 0) {
                attackedTargets++;
            }

            for (Attack a : result.getFinalizedAttacks()) {
                if (!usedSources.contains(a.getSource())) {
                    overviewPanel.addVillage(new Point(a.getSource().getX(), a.getSource().getY()), Color.YELLOW);
                }
                usedSources.add(a.getSource());
            }
            overviewPanel.addVillage(new Point(result.getTarget().getX(), result.getTarget().getY()),
                    ColorGradientHelper.getGradientColor(100.0f * (float) result.getFinalizedAttacks().length / (float) result.getMaxOffs(), Color.RED, Color.BLACK));
            model.addRow(result);
        }

        jxResultsTable.setModel(model);
        jxResultsTable.setDefaultRenderer(String.class, new PercentCellRenderer(true));

        updateAttackDetails();
        jAttackedTargets.setText(Integer.toString(attackedTargets) + " von " + results.size());
        jPerfectTargets.setText(Integer.toString(perfectOffs));
        jOverallAttacks.setText(Integer.toString(assignedAttacks) + " von " + Integer.toString(maxAttacks));
        jUsedSourceVillages.setText(Integer.toString(usedSources.size()) + " von " + AttackSourceFilterPanel.getSingleton().getFilteredElements().length);
        focusSubmit();
    }

    private void updateAttackDetails() {
        TAPResultDetailsTableModel model = getResultModel();
        model.clear();

        for (int row = 0; row < jxResultsTable.getRowCount(); row++) {
            int modelRow = jxResultsTable.convertRowIndexToModel(row);
            AbstractTroopMovement move = getModel().getRow(modelRow);
            for (Attack a : move.getFinalizedAttacks()) {
                model.addRow(a);
            }
        }
    }

    private void focusSubmit() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                jButton1.requestFocusInWindow();
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jAttackedTargets;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jExpertView;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jOverallAttacks;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel jPerfectTargets;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JComboBox jStandardFake;
    private javax.swing.JComboBox jStandardOff;
    private javax.swing.JPanel jSummaryPanel;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JLabel jUsedSourceVillages;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane2;
    private org.jdesktop.swingx.JXTable jXDetailsTable;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXLabel jXLabel2;
    private org.jdesktop.swingx.JXTable jxResultsTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
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
