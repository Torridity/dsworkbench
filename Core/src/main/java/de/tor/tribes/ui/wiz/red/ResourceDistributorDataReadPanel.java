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
package de.tor.tribes.ui.wiz.red;

import de.tor.tribes.types.StorageStatus;
import de.tor.tribes.types.VillageMerchantInfo;
import de.tor.tribes.ui.models.REDSourceTableModel;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.NumberFormatCellRenderer;
import de.tor.tribes.ui.renderer.StorageCellRenderer;
import de.tor.tribes.ui.renderer.EnumImageCellRenderer;
import de.tor.tribes.util.*;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.sort.TableSortController;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 *
 * @author Torridity
 */
public class ResourceDistributorDataReadPanel extends WizardPage {

    private static final String GENERAL_INFO = "<html>In diesem Schritt musst du die notwendigen Daten aus dem Spiel importieren. "
            + "Wechsle daf&uml;r im Spiel in die Produktions&uuml;bersicht, markiere die komplette Seite per STRG+A und klicke anschlie&szlig;end "
            + "auf den gro&szlig;en Button im oberen Bereich des Rohstoffverteilers, um die Daten aus der Zwischenablage zu lesen. Danach "
            + "m&uuml;ssten alle gefunden Informationen in der Tabelle aufgelistet sein. Sollte das nicht funktionieren, versuche bitte "
            + "vor dem Kopieren der Produktions&uuml;bersicht alle Scripte im Spiel zu deaktivieren, die Ver&auml;nderungen an dieser "
            + "&Uuml;bersicht vornehmen. Warst du erfolgreich, kannst du auf 'Weiter' klicken.</html>";
    private static ResourceDistributorDataReadPanel singleton = null;

    public static synchronized ResourceDistributorDataReadPanel getSingleton() {
        if (singleton == null) {
            singleton = new ResourceDistributorDataReadPanel();
        }

        return singleton;
    }

    /**
     * Creates new form ResourceDataReadPanel
     */
    ResourceDistributorDataReadPanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
        jAddAsBothButton.setIcon(new ImageIcon(ResourceDistributorDataReadPanel.class.getResource("/res/48x48/merchant_both.png")));
        jAddAsSenderButton.setIcon(new ImageIcon(ResourceDistributorDataReadPanel.class.getResource("/res/48x48/merchant_send.png")));
        jAddAsReceiverButton.setIcon(new ImageIcon(ResourceDistributorDataReadPanel.class.getResource("/res/48x48/merchant_receive.png")));

        jDataTable.setModel(new REDSourceTableModel());
        jDataTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jDataTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jDataTable.setDefaultRenderer(VillageMerchantInfo.Direction.class, new EnumImageCellRenderer(EnumImageCellRenderer.LayoutStyle.TradeDirection));
        jDataTable.setDefaultRenderer(Integer.class, new NumberFormatCellRenderer());
        jDataTable.setDefaultRenderer(StorageStatus.class, new StorageCellRenderer());

        TableSortController sorter = (TableSortController) jDataTable.getRowSorter();
        ResourceComparator resourceComparator = new ResourceComparator();
        sorter.setComparator(1, resourceComparator);
        SlashComparator splitComparator = new SlashComparator();
        sorter.setComparator(3, splitComparator);
        sorter.setComparator(4, splitComparator);

        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        ActionListener deleteListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelection();
            }
        };
        jDataTable.registerKeyboardAction(deleteListener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        capabilityInfoPanel1.addActionListener(deleteListener);
    }

    public static String getDescription() {
        return "Datenauswahl";
    }

    public static String getStep() {
        return "id-data";
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
        jLabel1 = new javax.swing.JLabel();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jDataTable = new org.jdesktop.swingx.JXTable();
        jStatusLabel = new javax.swing.JLabel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();
        jButtonPanel = new javax.swing.JPanel();
        jAddAsBothButton = new javax.swing.JButton();
        jAddAsSenderButton = new javax.swing.JButton();
        jAddAsReceiverButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html");
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        setPreferredSize(new java.awt.Dimension(520, 320));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Informationen einblenden");
        jLabel1.setToolTipText("Blendet Informationen zu dieser Ansicht und zu den Datenquellen ein/aus");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowHideInfoEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel1, gridBagConstraints);

        jXCollapsiblePane1.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jXCollapsiblePane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jDataTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jDataTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jStatusLabel.setMaximumSize(new java.awt.Dimension(0, 16));
        jStatusLabel.setMinimumSize(new java.awt.Dimension(0, 16));
        jStatusLabel.setPreferredSize(new java.awt.Dimension(0, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jStatusLabel, gridBagConstraints);

        capabilityInfoPanel1.setBbSupport(false);
        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setPastable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(capabilityInfoPanel1, gridBagConstraints);

        jButtonPanel.setLayout(new java.awt.GridBagLayout());

        jAddAsBothButton.setToolTipText("In der Zwischenablage nach kopierter Produktionsübersicht suchen");
        jAddAsBothButton.setMaximumSize(new java.awt.Dimension(120, 60));
        jAddAsBothButton.setMinimumSize(new java.awt.Dimension(120, 60));
        jAddAsBothButton.setPreferredSize(new java.awt.Dimension(120, 60));
        jAddAsBothButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireReadDataFromClipboardEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jButtonPanel.add(jAddAsBothButton, gridBagConstraints);

        jAddAsSenderButton.setToolTipText("In der Zwischenablage nach kopierter Produktionsübersicht suchen und Dörfer als Lieferanten eintragen");
        jAddAsSenderButton.setMaximumSize(new java.awt.Dimension(120, 60));
        jAddAsSenderButton.setMinimumSize(new java.awt.Dimension(120, 60));
        jAddAsSenderButton.setPreferredSize(new java.awt.Dimension(120, 60));
        jAddAsSenderButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireReadDataFromClipboardEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jButtonPanel.add(jAddAsSenderButton, gridBagConstraints);

        jAddAsReceiverButton.setToolTipText("In der Zwischenablage nach kopierter Produktionsübersicht suchen und Dörfer als Empfänger eintragen");
        jAddAsReceiverButton.setMaximumSize(new java.awt.Dimension(120, 60));
        jAddAsReceiverButton.setMinimumSize(new java.awt.Dimension(120, 60));
        jAddAsReceiverButton.setPreferredSize(new java.awt.Dimension(120, 60));
        jAddAsReceiverButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireReadDataFromClipboardEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jButtonPanel.add(jAddAsReceiverButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jButtonPanel, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Rohstoffspalte sortieren nach");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jLabel2, gridBagConstraints);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "allen Rohstoffen", "Holz", "Lehm", "Eisen" }));
        jComboBox1.setMinimumSize(new java.awt.Dimension(150, 20));
        jComboBox1.setPreferredSize(new java.awt.Dimension(150, 20));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireChangeSortTypeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jComboBox1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jPanel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fireShowHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowHideInfoEvent
        if (jXCollapsiblePane1.isCollapsed()) {
            jXCollapsiblePane1.setCollapsed(false);
            jLabel1.setText("Informationen ausblenden");
        } else {
            jXCollapsiblePane1.setCollapsed(true);
            jLabel1.setText("Informationen einblenden");
        }
    }//GEN-LAST:event_fireShowHideInfoEvent

    private void fireReadDataFromClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireReadDataFromClipboardEvent
        if (evt.getSource() == jAddAsBothButton) {
            readMerchantInfoFromClipboard(VillageMerchantInfo.Direction.BOTH);
        } else if (evt.getSource() == jAddAsSenderButton) {
            readMerchantInfoFromClipboard(VillageMerchantInfo.Direction.OUTGOING);
        } else if (evt.getSource() == jAddAsReceiverButton) {
            readMerchantInfoFromClipboard(VillageMerchantInfo.Direction.INCOMING);
        }
    }//GEN-LAST:event_fireReadDataFromClipboardEvent

    private void fireChangeSortTypeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireChangeSortTypeEvent
        TableSortController sorter = (TableSortController) jDataTable.getRowSorter();
        ResourceComparator resourceComparator = null;
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            switch (jComboBox1.getSelectedIndex()) {
                case 1:
                    resourceComparator = new ResourceComparator(ResourceComparator.COMPARE_RESOURCE.WOOD);
                    break;
                case 2:
                    resourceComparator = new ResourceComparator(ResourceComparator.COMPARE_RESOURCE.CLAY);
                    break;
                case 3:
                    resourceComparator = new ResourceComparator(ResourceComparator.COMPARE_RESOURCE.IRON);
                    break;
                default:
                    resourceComparator = new ResourceComparator();
                    break;
            }
        }

        sorter.setComparator(1, resourceComparator);
    }//GEN-LAST:event_fireChangeSortTypeEvent

    public void setup(int pType) {
        jAddAsSenderButton.setVisible(pType == ResourceDistributorWelcomePanel.FILL_DISTRIBUTION);
        jAddAsReceiverButton.setVisible(pType == ResourceDistributorWelcomePanel.FILL_DISTRIBUTION);
    }

    public REDSourceTableModel getModel() {
        return (REDSourceTableModel) jDataTable.getModel();
    }

    private void readMerchantInfoFromClipboard(VillageMerchantInfo.Direction pDirection) {
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            String data = (String) t.getTransferData(DataFlavor.stringFlavor);
            List<VillageMerchantInfo> infos = PluginManager.getSingleton().executeMerchantParser(data);
            if (infos.isEmpty()) {
                setProblem("Keine Einträge in der Zwischenablage gefunden");
                return;
            }

            for (VillageMerchantInfo newInfo : infos) {
                newInfo.setDirection(pDirection);
                getModel().addRow(newInfo.getVillage(),
                        newInfo.getStashCapacity(),
                        newInfo.getWoodStock(),
                        newInfo.getClayStock(),
                        newInfo.getIronStock(),
                        newInfo.getAvailableMerchants(),
                        newInfo.getOverallMerchants(),
                        newInfo.getAvailableFarm(),
                        newInfo.getOverallFarm(),
                        newInfo.getDirection());
            }

            switch (pDirection) {
                case INCOMING:
                    jStatusLabel.setText(infos.size() + " Empfänger eingefügt/aktualisiert");
                    break;
                case OUTGOING:
                    jStatusLabel.setText(infos.size() + " Lieferanten eingefügt/aktualisiert");
                    break;
                default:
                    jStatusLabel.setText(infos.size() + " Einträge eingefügt/aktualisiert");
            }
            if (getModel().getRowCount() > 500) {
                JOptionPaneHelper.showWarningBox(this, "Es wurden mehr als 500 Einträge eingefügt, die Berechnung der Transporte kann daher sehr lange dauern.\n"
                        + "Während die Berechnung läuft wird DS Workbench nicht reagieren.\n"
                        + "Es wird dringend empfohlen, die Berechnung in kleineren Einzelschritten durchzuführen.", "Warnung");
            }

            if (getModel().getRowCount() > 0) {
                setProblem(null);
            }
        } catch (Exception e) {
            setProblem("Fehler beim Lesen aus der Zwischenablage");
        }
    }

    private void deleteSelection() {
        int[] selection = jDataTable.getSelectedRows();
        if (selection.length > 0) {
            List<Integer> rows = new LinkedList<>();
            for (int i : selection) {
                rows.add(jDataTable.convertRowIndexToModel(i));
            }
            Collections.sort(rows);
            for (int i = rows.size() - 1; i >= 0; i--) {
                getModel().removeRow(rows.get(i));
            }
            if (getModel().getRowCount() == 0) {
                setProblem("Keine Dörfer vorhanden");
            }
        }
    }

    public VillageMerchantInfo[] getAllElements() {
        List<VillageMerchantInfo> elements = new LinkedList<>();
        REDSourceTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            elements.add(model.getRow(i).clone());
        }
        return elements.toArray(new VillageMerchantInfo[elements.size()]);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JButton jAddAsBothButton;
    private javax.swing.JButton jAddAsReceiverButton;
    private javax.swing.JButton jAddAsSenderButton;
    private javax.swing.JPanel jButtonPanel;
    private javax.swing.JComboBox jComboBox1;
    private org.jdesktop.swingx.JXTable jDataTable;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel jStatusLabel;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (getModel().getRowCount() == 0) {
            setProblem("Keine Einträge vorhanden");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }

        ResourceDistributorSettingsPanel.getSingleton().setup();
        ResourceDistributorCalculationPanel.getSingleton().setup(!ResourceDistributorWelcomePanel.BALANCE_DISTRIBUTION.equals(map.get(ResourceDistributorWelcomePanel.TYPE)));
        
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
