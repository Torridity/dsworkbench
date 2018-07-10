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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.VillageOverviewMapPanel;
import de.tor.tribes.ui.components.VillageSelectionPanel;
import de.tor.tribes.ui.models.TAPTargetTableModel;
import de.tor.tribes.ui.panels.TAPAttackInfoPanel;
import de.tor.tribes.ui.renderer.CustomBooleanRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackSourceElement;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackTargetElement;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.PluginManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.netbeans.spi.wizard.*;

/**
 * @author Torridity
 */
public class AttackTargetPanel extends WizardPage {

    private static final String GENERAL_INFO = "<html>Du befindest dich in der Zielauswahl f&uuml; die zu planenden Angriffe. W&auml;hle die "
            + "D&ouml;rfer aus die du angreifen magst und f&uuml;ge sie &uuml;ber den entsprechenden Button ein. Du kannst DS Workbench "
            + "per STRG+V auch dazu veranlassen, in der Zwischenablage nach Dorfkoordinaten zu suchen.<br/>"
            + "Willst du mehrere Angriffe auf ein Dorf durchf&uuml;hren, so f&uuml;ge diese entsprechenden D&ouml;rfer einfach mehrmals ein."
            + "</html>";
    private static AttackTargetPanel singleton = null;
    private VillageSelectionPanel villageSelectionPanel = null;
    private VillageOverviewMapPanel overviewPanel = null;

    public static synchronized AttackTargetPanel getSingleton() {
        if (singleton == null) {
            singleton = new AttackTargetPanel();
        }
        return singleton;
    }

    public static String getDescription() {
        return "Ziele";
    }

    public static String getStep() {
        return "id-attack-target";
    }

    /**
     * Creates new form AttackTargetPanel
     */
    AttackTargetPanel() {
        initComponents();
        jVillageTable.setModel(new TAPTargetTableModel());
        jVillageTable.setDefaultRenderer(Boolean.class, new CustomBooleanRenderer(CustomBooleanRenderer.LayoutStyle.FAKE_NOFAKE));
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jVillageTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jVillageTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        villageSelectionPanel = new VillageSelectionPanel(new VillageSelectionPanel.VillageSelectionPanelListener() {

            @Override
            public void fireVillageSelectionEvent(Village[] pSelection) {
                addVillages(pSelection);
            }
        });

        villageSelectionPanel.setFakeSelectionEnabled(true);
        villageSelectionPanel.setAmountSelectionEnabled(true);
        villageSelectionPanel.enableSelectionElement(VillageSelectionPanel.SELECTION_ELEMENT.GROUP, false);
        jDataPanel.add(villageSelectionPanel, BorderLayout.CENTER);

        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        ActionListener panelListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Paste")) {
                    pasteFromClipboard();
                } else if (e.getActionCommand().equals("Delete")) {
                    deleteSelection();
                }
            }
        };
        jVillageTable.registerKeyboardAction(panelListener, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jVillageTable.registerKeyboardAction(panelListener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        capabilityInfoPanel1.addActionListener(panelListener);

        jInfoTextPane.setText(GENERAL_INFO);
        overviewPanel = new VillageOverviewMapPanel();
        jPanel2.add(overviewPanel, BorderLayout.CENTER);

        jVillageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRows = jVillageTable.getSelectedRowCount();
                if (selectedRows != 0) {
                    jStatusLabel.setText(selectedRows + " Dorf/Dörfer gewählt");
                }
            }
        });
    }

    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        profile.addProperty("tap.target.expert", villageSelectionPanel.isExpertSelection());
        profile.addProperty("tap.target.unit", villageSelectionPanel.getSelectedUnit().getPlainName());
        profile.addProperty("tap.target.fake", villageSelectionPanel.isFake());
        profile.addProperty("tap.target.amount", villageSelectionPanel.getAmount());
    }

    public void restoreProperties() {
        getModel().clear();
        UserProfile profile = GlobalOptions.getSelectedProfile();
        villageSelectionPanel.setExpertSelection(Boolean.parseBoolean(profile.getProperty("tap.target.expert")));
        String unit = profile.getProperty("tap.target.unit");
        if (unit != null) {
            villageSelectionPanel.setSelectedUnit(DataHolder.getSingleton().getUnitByPlainName(unit));
        }
        villageSelectionPanel.setFake(Boolean.parseBoolean(profile.getProperty("tap.target.fake")));
        String value = profile.getProperty("tap.target.amount");
        if (value != null) {
            villageSelectionPanel.setAmount(Integer.parseInt(value));
        }
        villageSelectionPanel.setup();
        updateOverview();
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
        jVillageTablePanel = new javax.swing.JPanel();
        jTableScrollPane = new javax.swing.JScrollPane();
        jVillageTable = new org.jdesktop.swingx.JXTable();
        jPanel2 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jStatusLabel = new javax.swing.JLabel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jPanel3 = new javax.swing.JPanel();
        jAddAttackButton = new javax.swing.JButton();
        jRemoveAttackButton = new javax.swing.JButton();
        jToFakeButton = new javax.swing.JButton();
        jToNoFakeButton = new javax.swing.JButton();
        jDataPanel = new javax.swing.JPanel();

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
        gridBagConstraints.weightx = 1.0;
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
        gridBagConstraints.weightx = 1.0;
        add(jLabel1, gridBagConstraints);

        jVillageTablePanel.setMinimumSize(new java.awt.Dimension(400, 257));
        jVillageTablePanel.setPreferredSize(new java.awt.Dimension(400, 257));
        jVillageTablePanel.setLayout(new java.awt.GridBagLayout());

        jTableScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Anzugreifende Dörfer"));
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
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jToggleButton1, gridBagConstraints);

        jStatusLabel.setMaximumSize(new java.awt.Dimension(34, 16));
        jStatusLabel.setMinimumSize(new java.awt.Dimension(34, 16));
        jStatusLabel.setPreferredSize(new java.awt.Dimension(34, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jStatusLabel, gridBagConstraints);

        capabilityInfoPanel1.setBbSupport(false);
        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(capabilityInfoPanel1, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jAddAttackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/add_attack.png"))); // NOI18N
        jAddAttackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeAttackCountEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jAddAttackButton, gridBagConstraints);

        jRemoveAttackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/remove_attack.png"))); // NOI18N
        jRemoveAttackButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeAttackCountEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(jRemoveAttackButton, gridBagConstraints);

        jToFakeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/fake.png"))); // NOI18N
        jToFakeButton.setToolTipText("Gewählte Ziele auf \"Fake-Ziele\" setzen");
        jToFakeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeFakeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel3.add(jToFakeButton, gridBagConstraints);

        jToNoFakeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/no_fake.png"))); // NOI18N
        jToNoFakeButton.setToolTipText("Gewählte Ziele auf \"Keine Fake-Ziele\" setzen");
        jToNoFakeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireChangeFakeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel3.add(jToNoFakeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jPanel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(jVillageTablePanel, gridBagConstraints);

        jDataPanel.setMinimumSize(new java.awt.Dimension(400, 200));
        jDataPanel.setName(""); // NOI18N
        jDataPanel.setPreferredSize(new java.awt.Dimension(400, 200));
        jDataPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(jDataPanel, gridBagConstraints);
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

                @Override
                public void run() {
                    jPanel2.updateUI();
                }
            });
        }
    }//GEN-LAST:event_fireViewStateChangeEvent

    private void fireChangeAttackCountEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeAttackCountEvent
        boolean increase = false;
        if (evt.getSource() == jAddAttackButton) {
            increase = true;
        } else if (evt.getSource() == jRemoveAttackButton) {
            increase = false;
        }

        int[] selection = jVillageTable.getSelectedRows();
        if (selection.length > 0) {
            int modificationCount = 0;
            for (int i : selection) {
                TAPAttackTargetElement elem = getModel().getRow(jVillageTable.convertRowIndexToModel(i));
                if (increase) {
                    elem.addAttack();
                    modificationCount++;
                } else {
                    if (elem.removeAttack()) {
                        modificationCount++;
                    }
                }
            }

            jStatusLabel.setText(modificationCount + " Angriff(e) " + ((increase) ? "hinzugefügt" : "entfernt"));
            if (modificationCount > 0) {
                // getModel().fireTableDataChanged();
                jVillageTable.repaint();
            }
        } else {
            jStatusLabel.setText("Keine Ziele gewählt");
        }
        updateOverview();
    }//GEN-LAST:event_fireChangeAttackCountEvent

    private void fireChangeFakeEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireChangeFakeEvent
        boolean toFake = evt.getSource() == jToFakeButton;
        for (TAPAttackTargetElement elem : getSelection()) {
            elem.setFake(toFake);
        }
        updateOverview();
        repaint();
    }//GEN-LAST:event_fireChangeFakeEvent

    private TAPTargetTableModel getModel() {
        return (TAPTargetTableModel) jVillageTable.getModel();
    }

    public void addVillages(Village[] pVillages) {
        if (pVillages.length == 0) {
            return;
        }
        for (Village village : pVillages) {
            getModel().addRow(village, villageSelectionPanel.isFake(), villageSelectionPanel.getAmount());
        }
        if (getModel().getRowCount() > 0) {
            setProblem(null);
        }
        jStatusLabel.setText(pVillages.length + " Dorf/Dörfer eingefügt");
        updateOverview();
    }

    private void pasteFromClipboard() {
        String data;
        try {
            data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            List<Village> villages = PluginManager.getSingleton().executeVillageParser(data);
            if (!villages.isEmpty()) {
                addVillages(villages.toArray(new Village[villages.size()]));
            }
        } catch (HeadlessException | IOException | UnsupportedFlavorException ignored) {
        }
    }

    private void deleteSelection() {
        int[] selection = jVillageTable.getSelectedRows();
        if (selection.length > 0) {
            List<Integer> rows = new LinkedList<>();
            for (int i : selection) {
                rows.add(jVillageTable.convertRowIndexToModel(i));
            }
            Collections.sort(rows);
            for (int i = rows.size() - 1; i >= 0; i--) {
                getModel().removeRow(rows.get(i));
            }
            if (getModel().getRowCount() == 0) {
                setProblem("Keine Ziele gewählt");
            }
            jStatusLabel.setText(selection.length + " Dorf/Dörfer entfernt");
            updateOverview();
        }
    }

    public Village[] getUsedVillages() {
        List<Village> result = new LinkedList<>();
        TAPTargetTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            result.add(model.getRow(i).getVillage());
        }
        return result.toArray(new Village[result.size()]);
    }

    public List<TAPAttackTargetElement> getAllElements() {
        List<TAPAttackTargetElement> elements = new LinkedList<>();
        TAPTargetTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            elements.add(model.getRow(i));
        }
        return elements;
    }

    public List<TAPAttackTargetElement> getSelection() {
        List<TAPAttackTargetElement> elements = new LinkedList<>();
        TAPTargetTableModel model = getModel();
        for (int i : jVillageTable.getSelectedRows()) {
            elements.add(model.getRow(jVillageTable.convertRowIndexToModel(i)));
        }
        return elements;
    }

    protected boolean removeTargets(List<Village> toRemove) {
        getModel().removeTargets(toRemove);
        updateOverview();
        if (getModel().getRowCount() > 0) {
            AttackCalculationPanel.getSingleton().updateStatus();
            return true;
        }
        return false;
    }

    protected void updateOverview() {
        overviewPanel.reset();
        for (TAPAttackSourceElement element : AttackSourceFilterPanel.getSingleton().getFilteredElements()) {
            overviewPanel.addVillage(new Point(element.getVillage().getX(), element.getVillage().getY()), (!element.isIgnored()) ? Color.yellow : Color.lightGray);
        }

        int target = 0;
        int fake = 0;
        int ignored = 0;
        for (int i = 0; i < getModel().getRowCount(); i++) {
            TAPAttackTargetElement te = getModel().getRow(i);

            if (te.isIgnored()) {
                ignored++;
            } else {
                if (te.isFake()) {
                    fake++;
                }
            }
            Village v = te.getVillage();
            overviewPanel.addVillage(new Point(v.getX(), v.getY()), (!te.isIgnored()) ? Color.red : Color.lightGray);
            target += te.getAttacks();
        }

        TAPAttackInfoPanel.getSingleton().updateTarget(target, fake, ignored);
        overviewPanel.repaint();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JButton jAddAttackButton;
    private javax.swing.JPanel jDataPanel;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton jRemoveAttackButton;
    private javax.swing.JLabel jStatusLabel;
    private javax.swing.JScrollPane jTableScrollPane;
    private javax.swing.JButton jToFakeButton;
    private javax.swing.JButton jToNoFakeButton;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXTable jVillageTable;
    private javax.swing.JPanel jVillageTablePanel;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (getModel().getRowCount() <= 0) {
            setProblem("Keine Ziele gewählt");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }

        AttackTargetFilterPanel.getSingleton().setup();

        AttackCalculationPanel.getSingleton().updateStatus();
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
