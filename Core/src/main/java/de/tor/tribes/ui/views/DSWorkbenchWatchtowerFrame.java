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
import de.tor.tribes.ui.models.WatchtowerTableModel;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.renderer.ColorCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.util.*;
import de.tor.tribes.util.village.KnownVillageManager;
import de.tor.tribes.util.village.KnownVillage;
import de.tor.tribes.util.mark.MarkerManager;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.HorizontalAlignment;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.VerticalAlignment;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Charon
 * @author extremeCrazyCoder
 */
public class DSWorkbenchWatchtowerFrame extends AbstractDSWorkbenchFrame implements GenericManagerListener, ListSelectionListener {

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        ((WatchtowerTableModel) jWatchtowerTable.getModel()).fireTableDataChanged();
    }
    private final static Logger logger = Logger.getLogger("WatchtowerView");
    private static DSWorkbenchWatchtowerFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;

    public static synchronized DSWorkbenchWatchtowerFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchWatchtowerFrame();
        }
        return SINGLETON;
    }

    /**
     * Creates new form DSWorkbenchWatchtowerFrame
     */
    DSWorkbenchWatchtowerFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jWatchtowerPanel.add(centerPanel, BorderLayout.CENTER);
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
        jWatchtowerTable.registerKeyboardAction(listener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jWatchtowerTable.registerKeyboardAction(listener, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        jWatchtowerFrameAlwaysOnTop.setSelected(GlobalOptions.getProperties().getBoolean("watchtower.frame.alwaysOnTop"));
        setAlwaysOnTop(jWatchtowerFrameAlwaysOnTop.isSelected());

        jWatchtowerTable.setModel(new WatchtowerTableModel());
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            //TODO create help page
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.church_view", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
        jWatchtowerTable.getSelectionModel().addListSelectionListener(DSWorkbenchWatchtowerFrame.this);
        pack();
    }

    @Override
    public void toBack() {
        jWatchtowerFrameAlwaysOnTop.setSelected(false);
        fireWatchtowerFrameOnTopEvent(null);
        super.toBack();
    }

    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jWatchtowerFrameAlwaysOnTop.isSelected());

        PropertyHelper.storeTableProperties(jWatchtowerTable, pConfig, getPropertyPrefix());

    }

    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));

        try {
            jWatchtowerFrameAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception ignored) {
        }

        setAlwaysOnTop(jWatchtowerFrameAlwaysOnTop.isSelected());

        PropertyHelper.restoreTableProperties(jWatchtowerTable, pConfig, getPropertyPrefix());
    }

    @Override
    public String getPropertyPrefix() {
        return "watchtower.view";
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
        jWatchtowerPanel = new org.jdesktop.swingx.JXPanel();
        jWatchtowerFrameAlwaysOnTop = new javax.swing.JCheckBox();
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

        jWatchtowerTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jWatchtowerTable);

        jXPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        setTitle("Wachtürme");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jWatchtowerPanel.setBackground(new java.awt.Color(239, 235, 223));
        jWatchtowerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 500;
        gridBagConstraints.ipady = 300;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jWatchtowerPanel, gridBagConstraints);

        jWatchtowerFrameAlwaysOnTop.setText("Immer im Vordergrund");
        jWatchtowerFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireWatchtowerFrameOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jWatchtowerFrameAlwaysOnTop, gridBagConstraints);

        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setPastable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        getAccessibleContext().setAccessibleName("Wachtürme");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireWatchtowerFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireWatchtowerFrameOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireWatchtowerFrameOnTopEvent

    private void jXLabel1fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXLabel1fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXLabel1fireHideInfoEvent

    private void buildMenu() {
        JXTaskPane transferPane = new JXTaskPane();
        transferPane.setTitle("Übertragen");
        JXButton transferVillageList = new JXButton(new ImageIcon(DSWorkbenchWatchtowerFrame.class.getResource("/res/ui/center_ingame.png")));
        transferVillageList.setToolTipText("Zentriert das Wachturmndorf im Spiel");
        transferVillageList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                centerWatchtowerInGame();
            }
        });
        transferPane.getContentPane().add(transferVillageList);

        if (!GlobalOptions.isMinimal()) {
            JXButton button = new JXButton(new ImageIcon(DSWorkbenchWatchtowerFrame.class.getResource("/res/center_24x24.png")));
            button.setToolTipText("Zentriert das Wachturmndorf auf der Hauptkarte");
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    centerWatchtowerVillage();
                }
            });

            transferPane.getContentPane().add(button);
        }
        centerPanel.setupTaskPane(transferPane);
    }

    private KnownVillage getSelectedWatchtower() {
        int row = jWatchtowerTable.getSelectedRow();
        if (row >= 0) {
            try {
                return (KnownVillage) jWatchtowerTable.getModel().getValueAt(jWatchtowerTable.convertRowIndexToModel(row), 1);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void centerWatchtowerVillage() {
        KnownVillage v = getSelectedWatchtower();
        if (v != null) {
            DSWorkbenchMainFrame.getSingleton().centerVillage(v.getVillage());
        } else {
            showInfo("Kein Wachturm gewählt");
        }
    }

    private void centerWatchtowerInGame() {
        KnownVillage v = getSelectedWatchtower();
        if (v != null) {
            BrowserInterface.centerVillage(v.getVillage());
        } else {
            showInfo("Kein Wachturm gewählt");
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jWatchtowerTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Wachturm gewählt" : " Wachtürme gewählt"));
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
        int[] rows = jWatchtowerTable.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        String message = ((rows.length == 1) ? "Wachturmndorf " : (rows.length + " Wachturmdörfer ")) + "wirklich löschen?";
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            //get markers to remove
            List<Village> toRemove = new LinkedList<>();
            jWatchtowerTable.invalidate();
            for (int i = rows.length - 1; i >= 0; i--) {
                int row = jWatchtowerTable.convertRowIndexToModel(rows[i]);
                int col = jWatchtowerTable.convertColumnIndexToModel(1);
                Village v = ((KnownVillage) jWatchtowerTable.getModel()
                        .getValueAt(row, col)).getVillage();
                toRemove.add(v);
            }
            jWatchtowerTable.revalidate();
            //remove all selected markers and update the view once
            KnownVillageManager.getSingleton().removeWatchtowers(toRemove.toArray(new Village[]{}));
            showSuccess(toRemove.size() + ((toRemove.size() == 1) ? " Wachturm gelöscht" : " Wachtürme gelöscht"));
        }
    }

    private void bbCopySelection() {
        try {
            int[] rows = jWatchtowerTable.getSelectedRows();
            if (rows.length == 0) {
                return;
            }

            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]Watchturmdörfer[/size][/u]\n\n");
            } else {
                buffer.append("[u]Watchturmdörfer[/u]\n\n");
            }

            buffer.append("[table]\n");
            buffer.append("[**]Spieler[||]Dorf[||]Radius[/**]\n");

            for (int row1 : rows) {
                int row = jWatchtowerTable.convertRowIndexToModel(row1);
                int tribeCol = jWatchtowerTable.convertColumnIndexToModel(0);
                int villageCol = jWatchtowerTable.convertColumnIndexToModel(1);
                int rangeCol = jWatchtowerTable.convertColumnIndexToModel(2);
                buffer.append("[*]").
                        append(((Tribe) jWatchtowerTable.getModel().getValueAt(row, tribeCol)).toBBCode()).
                        append("[|]").
                        append(((Village) jWatchtowerTable.getModel().getValueAt(row, villageCol)).toBBCode()).
                        append("[|]").
                        append(jWatchtowerTable.getModel().getValueAt(row, rangeCol));
            }

            buffer.append("[/table]");

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

    @Override
    public void resetView() {
        KnownVillageManager.getSingleton().addManagerListener(this);
        MarkerManager.getSingleton().addManagerListener(this);
        jWatchtowerTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        String[] cols = new String[]{"Stufe", "Farbe"};
        for (String col : cols) {
            TableColumnExt columns = jWatchtowerTable.getColumnExt(col);
            columns.setPreferredWidth(80);
            columns.setMaxWidth(80);
            columns.setWidth(80);
        }

        ((WatchtowerTableModel) jWatchtowerTable.getModel()).fireTableDataChanged();
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JCheckBox jWatchtowerFrameAlwaysOnTop;
    private org.jdesktop.swingx.JXPanel jWatchtowerPanel;
    private static final org.jdesktop.swingx.JXTable jWatchtowerTable = new org.jdesktop.swingx.JXTable();
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXPanel jXPanel1;
    // End of variables declaration//GEN-END:variables

    static {
        HighlightPredicate.ColumnHighlightPredicate colu = new HighlightPredicate.ColumnHighlightPredicate(0, 1, 2);
        jWatchtowerTable.setHighlighters(new CompoundHighlighter(colu, HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B)));

        jWatchtowerTable.setColumnControlVisible(true);
        jWatchtowerTable.setDefaultRenderer(Color.class, new ColorCellRenderer());
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for(int i = 1; i <= KnownVillage.getMaxBuildingLevel("watchtower"); i++)
            model.addElement(i);
        jWatchtowerTable.setDefaultEditor(Integer.class, new DefaultCellEditor(new JComboBox(model)));
        BufferedImage back = ImageUtils.createCompatibleBufferedImage(5, 5, BufferedImage.BITMASK);
        Graphics2D g = back.createGraphics();
        GeneralPath p = new GeneralPath();
        p.moveTo(0, 0);
        p.lineTo(5, 0);
        p.lineTo(5, 5);
        p.closePath();
        g.setColor(Color.GREEN.darker());
        g.fill(p);
        g.dispose();
        jWatchtowerTable.addHighlighter(new PainterHighlighter(HighlightPredicate.EDITABLE, new ImagePainter(back, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)));
    }
}
