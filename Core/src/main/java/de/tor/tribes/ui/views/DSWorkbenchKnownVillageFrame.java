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
package de.tor.tribes.ui.views;

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.editors.BuildingLevelCellEditor;
import de.tor.tribes.ui.models.KnownVillageTableModel;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.renderer.BuildingLevelCellRenderer;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.util.*;
import de.tor.tribes.util.bb.KnownVillageListFormatter;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.village.KnownVillage;
import de.tor.tribes.util.village.KnownVillageManager;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * @author Charon
 * @author extremeCrazyCoder
 */
public class DSWorkbenchKnownVillageFrame extends AbstractDSWorkbenchFrame implements GenericManagerListener, ListSelectionListener {

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        ((KnownVillageTableModel) jKnownVillageTable.getModel()).fireTableDataChanged();
    }
    private final static Logger logger = LogManager.getLogger("KnownVillageView");
    private static DSWorkbenchKnownVillageFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;

    public static synchronized DSWorkbenchKnownVillageFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchKnownVillageFrame();
        }
        return SINGLETON;
    }

    /**
     * Creates new form DSWorkbenchKnownVillageFrame
     */
    DSWorkbenchKnownVillageFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jKnownVillagePanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildComponent(jXPanel1);
        buildMenu();

        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("Delete".equals(e.getActionCommand())) {
                    deleteSelection();
                } else if ("BBCopy".equals(e.getActionCommand())) {
                    bbCopySelection();
                }
            }
        };
        capabilityInfoPanel1.addActionListener(listener);
        jKnownVillageTable.registerKeyboardAction(listener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jKnownVillageTable.registerKeyboardAction(listener, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        jKnownVillageFrameAlwaysOnTop.setSelected(GlobalOptions.getProperties().getBoolean("watchtower.frame.alwaysOnTop"));
        setAlwaysOnTop(jKnownVillageFrameAlwaysOnTop.isSelected());

        jKnownVillageTable.setModel(new KnownVillageTableModel());
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            //TODO create help page
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.church_view", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
        jKnownVillageTable.getSelectionModel().addListSelectionListener(DSWorkbenchKnownVillageFrame.this);
        pack();
    }

    @Override
    public void toBack() {
        jKnownVillageFrameAlwaysOnTop.setSelected(false);
        fireKnownVillageFrameOnTopEvent(null);
        super.toBack();
    }

    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jKnownVillageFrameAlwaysOnTop.isSelected());

        PropertyHelper.storeTableProperties(jKnownVillageTable, pConfig, getPropertyPrefix());

    }

    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));

        try {
            jKnownVillageFrameAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }

        setAlwaysOnTop(jKnownVillageFrameAlwaysOnTop.isSelected());

        PropertyHelper.restoreTableProperties(jKnownVillageTable, pConfig, getPropertyPrefix());
    }

    @Override
    public String getPropertyPrefix() {
        return "knownVillage.view";
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

        jXPanel1 = new org.jdesktop.swingx.JXPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jKnownVillagePanel = new org.jdesktop.swingx.JXPanel();
        jKnownVillageFrameAlwaysOnTop = new javax.swing.JCheckBox();
        capabilityInfoPanel1 = new de.tor.tribes.ui.components.CapabilityInfoPanel();

        jXPanel1.setLayout(new java.awt.BorderLayout());

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setText("Keine Meldung");
        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jXLabel1fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        jXPanel1.add(infoPanel, java.awt.BorderLayout.SOUTH);

        jKnownVillageTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jKnownVillageTable);

        jXPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        setTitle("Dörfer");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jKnownVillagePanel.setBackground(new java.awt.Color(239, 235, 223));
        jKnownVillagePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 500;
        gridBagConstraints.ipady = 300;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jKnownVillagePanel, gridBagConstraints);

        jKnownVillageFrameAlwaysOnTop.setText("Immer im Vordergrund");
        jKnownVillageFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireKnownVillageFrameOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jKnownVillageFrameAlwaysOnTop, gridBagConstraints);

        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setPastable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireKnownVillageFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireKnownVillageFrameOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireKnownVillageFrameOnTopEvent

    private void jXLabel1fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXLabel1fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXLabel1fireHideInfoEvent

    private void buildMenu() {
        JXTaskPane transferPane = new JXTaskPane();
        transferPane.setTitle("Übertragen");
        JXButton transferVillageList = new JXButton(new ImageIcon(DSWorkbenchKnownVillageFrame.class.getResource("/res/ui/center_ingame.png")));
        transferVillageList.setToolTipText("Zentriert das Dorf im Spiel");
        transferVillageList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                centerVillageInGame();
            }
        });
        transferPane.getContentPane().add(transferVillageList);

        if (!GlobalOptions.isMinimal()) {
            JXButton button = new JXButton(new ImageIcon(DSWorkbenchKnownVillageFrame.class.getResource("/res/center_24x24.png")));
            button.setToolTipText("Zentriert das Dorf auf der Hauptkarte");
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    centerVillage();
                }
            });

            transferPane.getContentPane().add(button);
        }
        centerPanel.setupTaskPane(transferPane);
    }

    private KnownVillage getSelectedVillage() {
        int row = jKnownVillageTable.getSelectedRow();
        if (row >= 0) {
            try {
                return (KnownVillage) jKnownVillageTable.getModel().getValueAt(jKnownVillageTable.convertRowIndexToModel(row), 1);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void centerVillage() {
        KnownVillage v = getSelectedVillage();
        if (v != null) {
            DSWorkbenchMainFrame.getSingleton().centerVillage(v.getVillage());
        } else {
            showInfo("Kein Dorf gewählt");
        }
    }

    private void centerVillageInGame() {
        KnownVillage v = getSelectedVillage();
        if (v != null) {
            BrowserInterface.centerVillage(v.getVillage());
        } else {
            showInfo("Kein Dorf gewählt");
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jKnownVillageTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Dorf gewählt" : " Dörfer gewählt"));
            }
        }
    }

    public void showInfo(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(getBackground()));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    public void showError(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.RED));
        jXLabel1.setForeground(Color.WHITE);
        jXLabel1.setText(pMessage);
    }

    public void showSuccess(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.GREEN));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    private void deleteSelection() {
        int[] rows = jKnownVillageTable.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        String message = ((rows.length == 1) ? "Dorf " : (rows.length + " Dörfer ")) + "wirklich löschen?";
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            //get markers to remove
            List<Village> toRemove = new ArrayList<>();
            jKnownVillageTable.invalidate();
            for (int i = rows.length - 1; i >= 0; i--) {
                int row = jKnownVillageTable.convertRowIndexToModel(rows[i]);
                int col = jKnownVillageTable.convertColumnIndexToModel(1);
                Village v = ((KnownVillage) jKnownVillageTable.getModel()
                        .getValueAt(row, col)).getVillage();
                toRemove.add(v);
            }
            jKnownVillageTable.revalidate();
            //remove all selected markers and update the view once
            KnownVillageManager.getSingleton().removeVillages(toRemove.toArray(new Village[]{}));
            showSuccess(toRemove.size() + ((toRemove.size() == 1) ? " Dorf gelöscht" : " Dörfer gelöscht"));
        }
    }

    private void bbCopySelection() {
        try {
            List<KnownVillage> selVill = getSelectedVillages();
            if (selVill.size() == 0) {
                return;
            }

            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]Dörfer[/size][/u]\n\n");
            } else {
                buffer.append("[u]Dörfer[/u]\n\n");
            }
            buffer.append(new KnownVillageListFormatter().formatElements(selVill, extended));

            if (extended) {
                buffer.append("\n[size=8]Erstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/size]\n");
            } else {
                buffer.append("\nErstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "\n");
            }

            String b = buffer.toString();
            StringTokenizer t = new StringTokenizer(b, "[");
            int cnt = t.countTokens();
            if (cnt > 1000) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Wachtürme benötigen mehr als 1000 BB-Codes\n"
                        + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\n"
                        + "Trotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            String result = "Daten in Zwischenablage kopiert.";
            showSuccess(result);
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            showError(result);
        }
    }

    private List<KnownVillage> getSelectedVillages() {
        final List<KnownVillage> selectedVillages = new ArrayList<>();
        int[] selectedRows = jKnownVillageTable.getSelectedRows();
        if (selectedRows != null && selectedRows.length < 1) {
            return selectedVillages;
        }
        for (Integer selectedRow : selectedRows) {
            KnownVillage a = (KnownVillage) KnownVillageManager.getSingleton().getAllElements()
                    .get(jKnownVillageTable.convertRowIndexToModel(selectedRow));
            if (a != null) {
                selectedVillages.add(a);
            }
        }
        return selectedVillages;
    }

    @Override
    public void resetView() {
        KnownVillageManager.getSingleton().addManagerListener(this);
        MarkerManager.getSingleton().addManagerListener(this);
        jKnownVillageTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());

        ((KnownVillageTableModel) jKnownVillageTable.getModel()).fireTableDataChanged();
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JCheckBox jKnownVillageFrameAlwaysOnTop;
    private org.jdesktop.swingx.JXPanel jKnownVillagePanel;
    private static final org.jdesktop.swingx.JXTable jKnownVillageTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXPanel jXPanel1;
    // End of variables declaration//GEN-END:variables

    static {
        jKnownVillageTable.setHighlighters(new CompoundHighlighter(HighlighterFactory.createSimpleStriping(), HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B)));

        jKnownVillageTable.setColumnControlVisible(true);
        jKnownVillageTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jKnownVillageTable.setDefaultRenderer(Integer.class, new BuildingLevelCellRenderer());
        jKnownVillageTable.setDefaultEditor(Integer.class, new BuildingLevelCellEditor());
    }
}
