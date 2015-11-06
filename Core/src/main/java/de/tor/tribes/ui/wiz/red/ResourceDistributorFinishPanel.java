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

import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSplitPane;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Resource;
import de.tor.tribes.types.StorageStatus;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.VillageMerchantInfo;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.ClickAccountPanel;
import de.tor.tribes.ui.components.ProfileQuickChangePanel;
import de.tor.tribes.ui.models.REDFinalDistributionTableModel;
import de.tor.tribes.ui.models.REDFinalTransportsTableModel;
import de.tor.tribes.ui.renderer.*;
import de.tor.tribes.ui.wiz.red.types.ExtendedTransport;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 *
 * @author Torridity
 */
public class ResourceDistributorFinishPanel extends WizardPage {

    private static final String GENERAL_INFO = "<html>Bist du bei diesem Schritt angekommen, hast du abschlie&szlig;end die M&ouml;glichkeit, "
            + "die berechneten Rohstoffe in den Browser zu &uuml;bertragen und abzuschicken. Voraussetzung hierf&uuml;r ist, dass du das "
            + "DS Workbench Userscript in deinem Browser (Firefox oder Opera) installiert hast. Markiere einfach in der unteren Tabelle die Transporte die du "
            + "&uuml;bertragen m&ouml;chtest in der Tabelle, f&uuml;lle das Klickkonto mit der ben&ouml;tigten Anzahl Klicks auf und "
            + "klicke auf den Button mit dem Firefox Symbol. Sind es zuviele Transporte um sie auf einmal abzuschicken, kannst du "
            + "DS Workbench auch schlie&szlig;en, den Rohstoffverteiler sp&auml;ter erneut starten und die errechneten Transporte im ersten "
            + "Schritt von deiner Festplatte laden.</html>";
    private static ResourceDistributorFinishPanel singleton = null;
    private ClickAccountPanel clickPanel = null;
    private ProfileQuickChangePanel quickProfilePanel = null;

    public static synchronized ResourceDistributorFinishPanel getSingleton() {
        if (singleton == null) {
            singleton = new ResourceDistributorFinishPanel();
        }

        return singleton;
    }

    /**
     * Creates new form ResourceDataReadPanel
     */
    ResourceDistributorFinishPanel() {
        initComponents();
        jButton1.setIcon(new ImageIcon("./graphics/big/firefox.png"));
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
        jTransportsTable.setModel(new REDFinalTransportsTableModel());
        jTransportsTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jTransportsTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jTransportsTable.setDefaultRenderer(Boolean.class, new SentNotSentCellRenderer());
        jTransportsTable.setDefaultRenderer(Integer.class, new NumberFormatCellRenderer());

        jDistributionTable.setModel(new REDFinalDistributionTableModel());
        jDistributionTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jDistributionTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jDistributionTable.setDefaultRenderer(StorageStatus.class, new StorageCellRenderer());
        jDistributionTable.setDefaultRenderer(VillageMerchantInfo.Direction.class, new TradeDirectionCellRenderer());
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jTransportsTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelection();
            }
        }, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        jTransportsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    showInfo(jTransportsTable.getSelectedRowCount() + " Transport(e) gewählt");
                }
            }
        });

        JideSplitPane split1 = new JideSplitPane();
        split1.setOrientation(JideSplitPane.HORIZONTAL_SPLIT);
        split1.setProportionalLayout(true);
        split1.setDividerSize(5);
        split1.setShowGripper(true);
        split1.setOneTouchExpandable(true);
        split1.setDividerStepSize(10);
        split1.setInitiallyEven(true);
        split1.add(jFinalDistributionPanel, JideBoxLayout.FLEXIBLE);
        split1.add(jFinalStatusPanel, JideBoxLayout.VARY);

        jideSplitPane1.setOrientation(JideSplitPane.VERTICAL_SPLIT);
        jideSplitPane1.setProportionalLayout(true);
        jideSplitPane1.setDividerSize(5);
        jideSplitPane1.setShowGripper(true);
        jideSplitPane1.setOneTouchExpandable(true);
        jideSplitPane1.setDividerStepSize(10);
        jideSplitPane1.setInitiallyEven(true);
        jideSplitPane1.add(split1, JideBoxLayout.FLEXIBLE);
        jideSplitPane1.add(jTransportsPanel, JideBoxLayout.VARY);
        jideSplitPane1.getDividerAt(0).addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jideSplitPane1.setProportions(new double[]{0.5});
                }
            }
        });
        jXCollapsiblePane2.setLayout(new BorderLayout());
        jXCollapsiblePane2.add(jInfoLabel, BorderLayout.CENTER);
        quickProfilePanel = new ProfileQuickChangePanel();
        clickPanel = new ClickAccountPanel();
        jClickAccountPanel.add(clickPanel, BorderLayout.CENTER);
        jQuickProfilePanel.add(quickProfilePanel, BorderLayout.CENTER);
    }

    public static String getDescription() {
        return "Fertig";
    }

    public static String getStep() {
        return "id-finish";
    }

    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        profile.addProperty("red.ignore.submitted", jIgnoreSubmitted.isSelected());
    }

    public void restoreProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        jIgnoreSubmitted.setSelected(Boolean.parseBoolean(profile.getProperty("red.ignore.submitted")));

    }

    private void showInfo(String pText) {
        jInfoLabel.setText(pText);
        jXCollapsiblePane2.setCollapsed(false);
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
        jTransportsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTransportsTable = new org.jdesktop.swingx.JXTable();
        jXCollapsiblePane2 = new org.jdesktop.swingx.JXCollapsiblePane();
        jFinalDistributionPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jDistributionTable = new org.jdesktop.swingx.JXTable();
        jInfoLabel = new javax.swing.JLabel();
        jFinalStatusPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTransports = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jUsedMerchants = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTransportedWood = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTransportedClay = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTransportedIron = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jPanel1 = new javax.swing.JPanel();
        jFinalActionPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jClickAccountPanel = new javax.swing.JPanel();
        jQuickProfilePanel = new javax.swing.JPanel();
        jIgnoreSubmitted = new javax.swing.JCheckBox();
        jideSplitPane1 = new com.jidesoft.swing.JideSplitPane();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html");
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        jTransportsPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Errechnete Transporte"));

        jTransportsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTransportsTable);

        jTransportsPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jXCollapsiblePane2.setCollapsed(true);
        jTransportsPanel.add(jXCollapsiblePane2, java.awt.BorderLayout.SOUTH);

        jFinalDistributionPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Resultierende Rohstoffverteilung"));

        jDistributionTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jDistributionTable);

        jFinalDistributionPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jInfoLabel.setText("jLabel2");
        jInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });

        jFinalStatusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sonstige Informationen"));
        jFinalStatusPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Errechnete Transporte");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalStatusPanel.add(jLabel2, gridBagConstraints);

        jTransports.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalStatusPanel.add(jTransports, gridBagConstraints);

        jLabel4.setText("Verwendete Händler");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalStatusPanel.add(jLabel4, gridBagConstraints);

        jUsedMerchants.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalStatusPanel.add(jUsedMerchants, gridBagConstraints);

        jLabel6.setText("Transportiertes Holz");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalStatusPanel.add(jLabel6, gridBagConstraints);

        jTransportedWood.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalStatusPanel.add(jTransportedWood, gridBagConstraints);

        jLabel8.setText("Transportierter Lehm");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalStatusPanel.add(jLabel8, gridBagConstraints);

        jTransportedClay.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalStatusPanel.add(jTransportedClay, gridBagConstraints);

        jLabel10.setText("Transportiertes Eisen");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalStatusPanel.add(jLabel10, gridBagConstraints);

        jTransportedIron.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalStatusPanel.add(jTransportedIron, gridBagConstraints);

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

        jFinalActionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Abschließende Aktionen"));
        jFinalActionPanel.setLayout(new java.awt.GridBagLayout());

        jButton1.setToolTipText("Gewählte Transporte  in den Browser übertragen");
        jButton1.setMaximumSize(new java.awt.Dimension(70, 70));
        jButton1.setMinimumSize(new java.awt.Dimension(70, 70));
        jButton1.setPreferredSize(new java.awt.Dimension(70, 70));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferSelectionToBrowserEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jFinalActionPanel.add(jButton1, gridBagConstraints);

        jClickAccountPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jClickAccountPanel.setMinimumSize(new java.awt.Dimension(100, 50));
        jClickAccountPanel.setPreferredSize(new java.awt.Dimension(100, 50));
        jClickAccountPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 2);
        jFinalActionPanel.add(jClickAccountPanel, gridBagConstraints);

        jQuickProfilePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jQuickProfilePanel.setMinimumSize(new java.awt.Dimension(100, 50));
        jQuickProfilePanel.setPreferredSize(new java.awt.Dimension(100, 50));
        jQuickProfilePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 5, 2);
        jFinalActionPanel.add(jQuickProfilePanel, gridBagConstraints);

        jIgnoreSubmitted.setSelected(true);
        jIgnoreSubmitted.setText("Übertragene Transporte ignorieren");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 5, 5);
        jFinalActionPanel.add(jIgnoreSubmitted, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel1.add(jFinalActionPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(jideSplitPane1, gridBagConstraints);

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

    private void fireTransferSelectionToBrowserEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferSelectionToBrowserEvent
        boolean outOfClicks = false;
        boolean browserAccessFailed = false;
        int transferred = 0;
        int ignored = 0;
        int[] selectedRows = jTransportsTable.getSelectedRows();
        if (selectedRows.length > 0) {
            for (int i : selectedRows) {
                ExtendedTransport transport = getTransportsModel().getRow(jTransportsTable.convertRowIndexToModel(i));
                boolean removeSelection = false;
                if (!jIgnoreSubmitted.isSelected() || !transport.isTransferredToBrowser()) {
                    if (selectedRows.length == 1 || clickPanel.useClick()) {
                        transport.setTransferredToBrowser(BrowserCommandSender.sendRes(transport.getSource(), transport.getTarget(), transport, quickProfilePanel.getSelectedProfile()));
                        if (!transport.isTransferredToBrowser()) {//if transfer failed, set browser access error flag and give click back
                            browserAccessFailed = (browserAccessFailed == false) ? true : browserAccessFailed;
                            if (selectedRows.length > 1) {
                                clickPanel.giveClickBack();
                            }
                        } else {
                            transferred++;
                            removeSelection = true;
                        }
                    } else {
                        outOfClicks = true;
                    }
                } else {
                    ignored++;
                    removeSelection = true;
                }

                if (removeSelection) {
                    jTransportsTable.getSelectionModel().removeSelectionInterval(i, i);
                }
            }
        } else {
            showInfo("Keine Transporte ausgewählt");
        }

        if (transferred + ignored != 0 && transferred + ignored == selectedRows.length) {
            int last = selectedRows[selectedRows.length - 1];
            if (jTransportsTable.getRowCount() > last) {
                jTransportsTable.getSelectionModel().addSelectionInterval(last + 1, last + 1);
            }
        }

        saveTransports();
        if (outOfClicks) {
            showInfo("Keine weiteren Klicks vorhanden.\n"
                    + "Es wurde(n) " + transferred + " Transport(e) übertragen.");
        }
        if (browserAccessFailed) {
            showInfo("Einer oder mehrere Transporte konnten nicht im Browser geöffnet werden.");
        }
    }//GEN-LAST:event_fireTransferSelectionToBrowserEvent

    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        jXCollapsiblePane2.setCollapsed(true);
    }//GEN-LAST:event_fireHideInfoEvent

    public REDFinalTransportsTableModel getTransportsModel() {
        return (REDFinalTransportsTableModel) jTransportsTable.getModel();
    }

    public REDFinalDistributionTableModel getDistributionModel() {
        return (REDFinalDistributionTableModel) jDistributionTable.getModel();
    }

    protected void setup() {
        Hashtable<Village, Hashtable<Village, List<Resource>>> transports = ResourceDistributorCalculationPanel.getSingleton().getTransports();
        Enumeration<Village> sourceKeys = transports.keys();
        REDFinalTransportsTableModel model = getTransportsModel();
        model.clear();

        VillageMerchantInfo[] infos = ResourceDistributorSettingsPanel.getSingleton().getAllElements();

        Hashtable<Village, VillageMerchantInfo> infoTable = new Hashtable<Village, VillageMerchantInfo>();

        for (VillageMerchantInfo info : infos) {
            infoTable.put(info.getVillage(), info);
        }
        int usedMerchants = 0;
        int transportedWood = 0;
        int transportedClay = 0;
        int transportedIron = 0;
        while (sourceKeys.hasMoreElements()) {
            Village sourceVillage = sourceKeys.nextElement();

            Hashtable<Village, List<Resource>> transportsFromSource = transports.get(sourceVillage);
            Enumeration<Village> destKeys = transportsFromSource.keys();
            while (destKeys.hasMoreElements()) {
                Village targetVillage = destKeys.nextElement();
                List<Resource> resources = transportsFromSource.get(targetVillage);
                if (model.addRow(sourceVillage, targetVillage, resources)) {
                    VillageMerchantInfo sourceInfo = infoTable.get(sourceVillage);
                    VillageMerchantInfo targetInfo = infoTable.get(targetVillage);

                    for (Resource r : resources) {
                        usedMerchants += r.getAmount() / 1000;
                        switch (r.getType()) {
                            case WOOD:
                                sourceInfo.setWoodStock(sourceInfo.getWoodStock() - r.getAmount());
                                targetInfo.setWoodStock(targetInfo.getWoodStock() + r.getAmount());
                                transportedWood += r.getAmount();
                                break;
                            case CLAY:
                                sourceInfo.setClayStock(sourceInfo.getClayStock() - r.getAmount());
                                targetInfo.setClayStock(targetInfo.getClayStock() + r.getAmount());
                                transportedClay += r.getAmount();
                                break;
                            case IRON:
                                sourceInfo.setIronStock(sourceInfo.getIronStock() - r.getAmount());
                                targetInfo.setIronStock(targetInfo.getIronStock() + r.getAmount());
                                transportedIron += r.getAmount();
                                break;
                        }
                    }
                }
            }
        }
        model.fireTableDataChanged();
        int overallTransports = model.getRowCount();

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        jTransports.setText(nf.format(overallTransports));
        jUsedMerchants.setText(nf.format(usedMerchants));
        jTransportedWood.setText(nf.format(transportedWood));
        jTransportedClay.setText(nf.format(transportedClay));
        jTransportedIron.setText(nf.format(transportedIron));

        REDFinalDistributionTableModel distributionModel = getDistributionModel();
        distributionModel.clear();

        Enumeration<Village> keys = infoTable.keys();
        while (keys.hasMoreElements()) {
            Village v = keys.nextElement();
            VillageMerchantInfo info = infoTable.get(v);
            distributionModel.addRow(v, info.getStashCapacity(), info.getWoodStock(), info.getClayStock(), info.getIronStock(), info.getDirection());
        }

        distributionModel.fireTableDataChanged();
        saveTransports();
        focusSubmit();
    }

    private void deleteSelection() {
        int[] selection = jTransportsTable.getSelectedRows();
        if (selection.length > 0) {
            List<Integer> rows = new LinkedList<Integer>();
            for (int i : selection) {
                rows.add(jTransportsTable.convertRowIndexToModel(i));
            }
            Collections.sort(rows);
            for (int i = rows.size() - 1; i >= 0; i--) {
                getTransportsModel().removeRow(rows.get(i));
            }
            if (getTransportsModel().getRowCount() == 0) {
                setProblem("Keine Dörfer vorhanden");
            }
        }
    }

    private void saveTransports() {
        StringBuilder b = new StringBuilder();
        int cnt = 0;
        REDFinalTransportsTableModel model = getTransportsModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            //get values
            Village source = (Village) jTransportsTable.getValueAt(i, 0);
            Village target = (Village) jTransportsTable.getValueAt(i, 1);
            Integer wood = (Integer) jTransportsTable.getValueAt(i, 2);
            Integer clay = (Integer) jTransportsTable.getValueAt(i, 3);
            Integer iron = (Integer) jTransportsTable.getValueAt(i, 4);
            Boolean submitted = (Boolean) jTransportsTable.getValueAt(i, 6);
            //build row
            b.append(source.getId()).append(",");
            b.append(wood).append(",").append(clay).append(",").append(iron).append(",");
            b.append(target.getId()).append(",");
            b.append(submitted).append("\n");
            cnt++;
        }

        String profileDir = GlobalOptions.getSelectedProfile().getProfileDirectory();
        FileWriter w = null;
        try {
            w = new FileWriter(new File(profileDir + "/transports.sav"));
            w.write(b.toString());
            w.flush();
        } catch (IOException ioe) {
            //FAILED
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected boolean loadTransports() {
        boolean result = false;
        String profileDir = GlobalOptions.getSelectedProfile().getProfileDirectory();
        File transportsFile = new File(profileDir + "/transports.sav");
        if (transportsFile.exists()) {
            BufferedReader r = null;
            REDFinalTransportsTableModel model = getTransportsModel();
            model.clear();
            try {
                r = new BufferedReader(new FileReader(transportsFile));
                String line = "";
                int cnt = 0;
                while ((line = r.readLine()) != null) {
                    String[] split = line.split(",");
                    Village sourceVillage = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(split[0]));
                    Resource wood = new Resource(Integer.parseInt(split[1]), Resource.Type.WOOD);
                    Resource clay = new Resource(Integer.parseInt(split[2]), Resource.Type.CLAY);
                    Resource iron = new Resource(Integer.parseInt(split[3]), Resource.Type.IRON);
                    Village targetVillage = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(split[4]));
                    boolean submitted = Boolean.parseBoolean(split[5]);

                    if (sourceVillage != null && targetVillage != null) {
                        List<Resource> resources = Arrays.asList(wood, clay, iron);
                        model.addRow(sourceVillage, targetVillage, resources, submitted);
                        cnt++;
                    }
                }
                model.fireTableDataChanged();
                result = true;
                focusSubmit();
            } catch (IOException ioe) {
            } catch (Exception e) {
            } finally {
                if (r != null) {
                    try {
                        r.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        if (result) {
            jideSplitPane1.setProportions(new double[]{0.0});
        }
        return result;
    }

    protected void focusSubmit() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                jButton1.requestFocusInWindow();
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jClickAccountPanel;
    private org.jdesktop.swingx.JXTable jDistributionTable;
    private javax.swing.JPanel jFinalActionPanel;
    private javax.swing.JPanel jFinalDistributionPanel;
    private javax.swing.JPanel jFinalStatusPanel;
    private javax.swing.JCheckBox jIgnoreSubmitted;
    private javax.swing.JLabel jInfoLabel;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jQuickProfilePanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel jTransportedClay;
    private javax.swing.JLabel jTransportedIron;
    private javax.swing.JLabel jTransportedWood;
    private javax.swing.JLabel jTransports;
    private javax.swing.JPanel jTransportsPanel;
    private org.jdesktop.swingx.JXTable jTransportsTable;
    private javax.swing.JLabel jUsedMerchants;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane2;
    private com.jidesoft.swing.JideSplitPane jideSplitPane1;
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
