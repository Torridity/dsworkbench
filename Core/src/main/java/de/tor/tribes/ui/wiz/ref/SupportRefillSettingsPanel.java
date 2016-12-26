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

import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSplitPane;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.php.UnitTableInterface;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.VillageOverviewMapPanel;
import de.tor.tribes.ui.models.REFSettingsTableModel;
import de.tor.tribes.ui.panels.TroopSelectionPanel;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.util.ColorGradientHelper;
import de.tor.tribes.ui.wiz.ref.types.REFTargetElement;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.TroopHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.netbeans.spi.wizard.*;

/**
 *
 * @author Torridity
 */
public class SupportRefillSettingsPanel extends WizardPage implements ActionListener {

    private static final String GENERAL_INFO = "In diesem Schritt kannst du bestimmen, wieviele Truppen in alles Zieldörfer sein sollen und wie groß eine "
            + "einzelne Unterstützung ist. DS Workbench wird versuchen, die Zielmenge durch alle bekannten Truppeninformationen im möglichst vielen Dörfern zu erreichen. "
            + "Beachte: Je kleiner die Einzelunterstützungen sind, desto schneller können Verluste ausgeglichen werden, jedoch dauert es auch eine Weile, bis man die Unterstützungen "
            + "auf den Weg geschickt hat. Hast du deine Einstellungen getroffen, klicke auf 'Notwendige Unterstützungen berechnen' um die Tabelle zu aktualisieren.";
    private static SupportRefillSettingsPanel singleton = null;
    private VillageOverviewMapPanel overviewPanel = null;
    private TroopSelectionPanel targetAmountPanel = null;
    private TroopSelectionPanel splitAmountPanel = null;

    public static synchronized SupportRefillSettingsPanel getSingleton() {
        if (singleton == null) {
            singleton = new SupportRefillSettingsPanel();
        }
        return singleton;
    }

    /**
     * Creates new form AttackSourcePanel
     */
    SupportRefillSettingsPanel() {
        initComponents();
        jVillageTable.setModel(new REFSettingsTableModel());
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jVillageTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jVillageTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());

        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
        jVillageTable.registerKeyboardAction(SupportRefillSettingsPanel.this, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        capabilityInfoPanel1.addActionListener(SupportRefillSettingsPanel.this);

        jideSplitPane1.setOrientation(JideSplitPane.VERTICAL_SPLIT);
        jideSplitPane1.setProportionalLayout(true);
        jideSplitPane1.setDividerSize(5);
        jideSplitPane1.setShowGripper(true);
        jideSplitPane1.setOneTouchExpandable(true);
        jideSplitPane1.setDividerStepSize(10);
        jideSplitPane1.setInitiallyEven(true);
        jideSplitPane1.add(jDataPanel, JideBoxLayout.FLEXIBLE);
        jideSplitPane1.add(jVillageTablePanel, JideBoxLayout.VARY);
        jideSplitPane1.getDividerAt(0).addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jideSplitPane1.setProportions(new double[]{0.5});
                }
            }
        });

        jVillageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRows = jVillageTable.getSelectedRowCount();
                if (selectedRows != 0) {
                    jStatusLabel.setText(selectedRows + " Dorf/Dörfer gewählt");
                }
            }
        });

        targetAmountPanel = new TroopSelectionPanel();
        targetAmountPanel.setupDefense(true);
        jTargetAmountsPanel.add(targetAmountPanel, BorderLayout.CENTER);
        splitAmountPanel = new TroopSelectionPanel();
        splitAmountPanel.setupDefense(true);
        jSplitSizePanel.add(splitAmountPanel, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
        overviewPanel = new VillageOverviewMapPanel();
        jPanel2.add(overviewPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("BBCopy")) {
            copyDefRequests();
        }
    }

    private void copyDefRequests() {
        REFTargetElement[] selection = getSelectedElements();

        if (selection.length == 0) {
            jStatusLabel.setText("Keine Einträge gewählt");
            return;
        }
        boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        StringBuilder b = new StringBuilder();
        b.append("Ich benötige die aufgelisteten oder vergleichbare Unterstützungen in den folgenden Dörfern:\n\n");

        Hashtable<UnitHolder, Integer> split = splitAmountPanel.getAmounts();

        for (REFTargetElement defense : selection) {
            Village target = defense.getVillage();
            int needed = defense.getNeededSupports();
            Hashtable<UnitHolder, Integer> need = new Hashtable<>();
            Set<Map.Entry<UnitHolder, Integer>> entries = split.entrySet();
            for (Map.Entry<UnitHolder, Integer> entry : entries) {
                need.put(entry.getKey(), needed * entry.getValue());
            }

            if (extended) {
                b.append("[table]\n");
                b.append("[**]").append(target.toBBCode()).append("[|]");
                b.append("[img]").append(UnitTableInterface.createDefenderUnitTableLink(need)).append("[/img][/**]\n");
                b.append("[/table]\n");
            } else {
                b.append(buildSimpleRequestTable(target, need, defense));
            }
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
            jStatusLabel.setText("Unterstützungsanfragen in die Zwischenablage kopiert");
        } catch (HeadlessException hex) {
            jStatusLabel.setText("Fehler beim Kopieren in die Zwischenablage");
        }
    }

    private String buildSimpleRequestTable(Village pTarget, Hashtable<UnitHolder, Integer> pNeed, REFTargetElement pDefense) {
        StringBuilder b = new StringBuilder();
        b.append("[table]\n");
        b.append("[**]").append(pTarget.toBBCode());
        int colCount = 0;

        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            Integer value = pNeed.get(unit);
            if (value != null && value > 0) {
                b.append("[|]").append("[unit]").append(unit.getPlainName()).append("[/unit]");
                colCount++;
            }
        }
        b.append("[/**]\n");

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        b.append("[*]");
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            Integer value = pNeed.get(unit);
            if (value != null && value > 0) {
                b.append("[|]").append(nf.format(value));
            }
        }

        for (int i = 0; i < colCount; i++) {
            b.append("[|]");
        }
        b.append("\n");

        for (int i = 0; i < colCount; i++) {
            b.append("[|]");
        }
        b.append("\n");
        b.append("[/table]\n");

        return b.toString();
    }

    public static String getDescription() {
        return "Einstellungen";
    }

    public static String getStep() {
        return "id-ref-settings";
    }

    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        profile.addProperty("ref.filter.amount", TroopHelper.unitTableToProperty(targetAmountPanel.getAmounts()));
        profile.addProperty("ref.filter.split", TroopHelper.unitTableToProperty(splitAmountPanel.getAmounts()));
        profile.addProperty("ref.allow.similar.amount", jAllowSimilarTroops.isSelected());
    }

    public void restoreProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        targetAmountPanel.setAmounts(TroopHelper.propertyToUnitTable(profile.getProperty("ref.filter.amount")));
        splitAmountPanel.setAmounts(TroopHelper.propertyToUnitTable(profile.getProperty("ref.filter.split")));
        String val = profile.getProperty("ref.allow.similar.amount");
        if (val == null) {
            jAllowSimilarTroops.setSelected(true);
        } else {
            jAllowSimilarTroops.setSelected(Boolean.parseBoolean(val));
        }
    }

    public Hashtable<UnitHolder, Integer> getSplit() {
        return splitAmountPanel.getAmounts();
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

        jInfoScrollPane = new javax.swing.JScrollPane();
        jInfoTextPane = new javax.swing.JTextPane();
        jDataPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jTargetAmountsPanel = new javax.swing.JPanel();
        jAllowSimilarTroops = new javax.swing.JCheckBox();
        jSplitSizePanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jVillageTablePanel = new javax.swing.JPanel();
        jTableScrollPane = new javax.swing.JScrollPane();
        jVillageTable = new org.jdesktop.swingx.JXTable();
        jPanel2 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jStatusLabel = new javax.swing.JLabel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jideSplitPane1 = new com.jidesoft.swing.JideSplitPane();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html"); // NOI18N
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        jDataPanel.setMinimumSize(new java.awt.Dimension(0, 130));
        jDataPanel.setPreferredSize(new java.awt.Dimension(0, 130));
        jDataPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jTargetAmountsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Gewünschte Truppenstärke"));
        jTargetAmountsPanel.setLayout(new java.awt.BorderLayout());

        jAllowSimilarTroops.setSelected(true);
        jAllowSimilarTroops.setText("Gleichwertige Truppenstärke zulassen");
        jAllowSimilarTroops.setToolTipText("<html>Ist diese Option aktiviert, so werden nicht zwingend die vorgegebenen Truppen aufgef&uuml;llt.<br/>Stattdessen wird versucht, unter Ber&uuml;cksichtigung der bereits \nstationierten Truppen,<br/>die vorgegebene St&auml;rke der Verteidigung zu erreichen.</html>");
        jAllowSimilarTroops.setMaximumSize(new java.awt.Dimension(150, 23));
        jAllowSimilarTroops.setMinimumSize(new java.awt.Dimension(150, 23));
        jAllowSimilarTroops.setPreferredSize(new java.awt.Dimension(150, 23));
        jTargetAmountsPanel.add(jAllowSimilarTroops, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jTargetAmountsPanel, gridBagConstraints);

        jSplitSizePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Einzelunterstützung"));
        jSplitSizePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jSplitSizePanel, gridBagConstraints);

        jButton1.setText("<html>Notwendige Unterstützungen<br/>berechnen</html>");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCalculateNeededSupportsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jButton1, gridBagConstraints);

        jButton2.setText("<html>Notwendige Truppen als<br/>BB-Code exportieren</html>");
        jButton2.setToolTipText("Exportiert die Differenz zur gewünschten Truppenstärke für alle Dörfer in die Zwischenablage");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCalculateAndExportRequiredTroopsEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jButton2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDataPanel.add(jPanel1, gridBagConstraints);

        jVillageTablePanel.setLayout(new java.awt.GridBagLayout());

        jTableScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Berücksichtigte Dörfer"));
        jTableScrollPane.setMinimumSize(new java.awt.Dimension(23, 100));
        jTableScrollPane.setPreferredSize(new java.awt.Dimension(23, 100));

        jVillageTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableScrollPane.setViewportView(jVillageTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jTableScrollPane, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel2.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel2.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 5, 5);
        jVillageTablePanel.add(jPanel2, gridBagConstraints);

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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jToggleButton1, gridBagConstraints);

        jStatusLabel.setMaximumSize(new java.awt.Dimension(0, 16));
        jStatusLabel.setMinimumSize(new java.awt.Dimension(0, 16));
        jStatusLabel.setPreferredSize(new java.awt.Dimension(0, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jStatusLabel, gridBagConstraints);

        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setDeletable(false);
        capabilityInfoPanel1.setPastable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(capabilityInfoPanel1, gridBagConstraints);

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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jideSplitPane1, gridBagConstraints);
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

    private void fireViewStateChangeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireViewStateChangeEvent
        if (jToggleButton1.isSelected()) {
            overviewPanel.setOptimalSize();
            jTableScrollPane.setViewportView(overviewPanel);
            jPanel2.remove(overviewPanel);
        } else {
            jTableScrollPane.setViewportView(jVillageTable);
            jPanel2.add(overviewPanel, BorderLayout.CENTER);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jPanel2.updateUI();
                }
            });
        }
    }//GEN-LAST:event_fireViewStateChangeEvent

    private void fireCalculateNeededSupportsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateNeededSupportsEvent
        Hashtable<UnitHolder, Integer> target = targetAmountPanel.getAmounts();
        Hashtable<UnitHolder, Integer> split = splitAmountPanel.getAmounts();

        if (TroopHelper.getPopulation(target) == 0) {
            jStatusLabel.setText("Keine gewünschte Truppenstärke angegeben");
            return;
        }
        if (TroopHelper.getPopulation(split) == 0) {
            jStatusLabel.setText("Menge einer Einzelunterstützung nicht angegeben");
            return;
        }

        int max = 0;
        for (int i = 0; i < getModel().getRowCount(); i++) {
            REFTargetElement elem = getModel().getRow(jVillageTable.convertRowIndexToModel(i));
            elem.setNeededSupports(TroopHelper.getNeededSupports(elem.getVillage(), target, split, jAllowSimilarTroops.isSelected()));
            max = Math.max(elem.getNeededSupports(), max);
        }
        getModel().fireTableDataChanged();

        for (REFTargetElement element : getAllElements()) {
            overviewPanel.addVillage(new Point(element.getVillage().getX(), element.getVillage().getY()),
                    ColorGradientHelper.getGradientColor(100.0f * (float) element.getNeededSupports() / (float) max, Color.RED, Color.GREEN));
        }
        overviewPanel.repaint();
        setProblem(null);
    }//GEN-LAST:event_fireCalculateNeededSupportsEvent

    private void fireCalculateAndExportRequiredTroopsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateAndExportRequiredTroopsEvent
        Hashtable<UnitHolder, Integer> target = targetAmountPanel.getAmounts();
        if (TroopHelper.getPopulation(target) == 0) {
            jStatusLabel.setText("Keine gewünschte Truppenstärke angegeben");
            return;
        }
        StringBuilder b = new StringBuilder();
        b.append("Ich benötige die aufgelisteten oder vergleichbare Unterstützungen in den folgenden Dörfern:\n\n");

        for (int i = 0; i < getModel().getRowCount(); i++) {
            REFTargetElement elem = getModel().getRow(jVillageTable.convertRowIndexToModel(i));
            Village v = elem.getVillage();
            Hashtable<UnitHolder, Integer> requiredTroops = TroopHelper.getRequiredTroops(v, target);
            b.append("[table]\n");
            b.append("[**]").append(v.toBBCode()).append("[|]");
            b.append("[img]").append(UnitTableInterface.createDefenderUnitTableLink(requiredTroops)).append("[/img][/**]\n");
            b.append("[/table]\n");
        }

        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
            jStatusLabel.setText("Unterstützungsanfragen in die Zwischenablage kopiert");
        } catch (HeadlessException hex) {
            jStatusLabel.setText("Fehler beim Kopieren in die Zwischenablage");
        }

    }//GEN-LAST:event_fireCalculateAndExportRequiredTroopsEvent

    private REFSettingsTableModel getModel() {
        return (REFSettingsTableModel) jVillageTable.getModel();
    }

    public void update() {
        REFSettingsTableModel model = getModel();
        model.clear();
        for (Village v : SupportRefillTargetPanel.getSingleton().getAllElements()) {
            model.addRow(v);
        }
        updateOverview(true);
    }

    private void updateOverview(boolean pReset) {
        if (pReset) {
            overviewPanel.reset();
        }
        for (REFTargetElement element : getAllElements()) {
            overviewPanel.addVillage(new Point(element.getVillage().getX(), element.getVillage().getY()), Color.yellow);
        }
        overviewPanel.repaint();
    }

    public REFTargetElement[] getAllElements() {
        List<REFTargetElement> result = new LinkedList<>();
        REFSettingsTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            result.add(model.getRow(jVillageTable.convertRowIndexToModel(i)));
        }
        return result.toArray(new REFTargetElement[result.size()]);
    }

    public REFTargetElement[] getSelectedElements() {
        List<REFTargetElement> result = new LinkedList<>();
        REFSettingsTableModel model = getModel();

        for (int i : jVillageTable.getSelectedRows()) {
            result.add(model.getRow(jVillageTable.convertRowIndexToModel(i)));
        }
        return result.toArray(new REFTargetElement[result.size()]);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JCheckBox jAllowSimilarTroops;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jDataPanel;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jSplitSizePanel;
    private javax.swing.JLabel jStatusLabel;
    private javax.swing.JScrollPane jTableScrollPane;
    private javax.swing.JPanel jTargetAmountsPanel;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXTable jVillageTable;
    private javax.swing.JPanel jVillageTablePanel;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private com.jidesoft.swing.JideSplitPane jideSplitPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (getAllElements().length == 0) {
            setProblem("Keine Dörfer gewählt");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        int need = 0;
        for (REFTargetElement elem : getAllElements()) {
            need += elem.getNeededSupports();
        }

        if (need == 0) {
            setProblem("Keine notwendigen Unterstützungen bestimmmt");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }

        SupportRefillSourcePanel.getSingleton().update();
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
