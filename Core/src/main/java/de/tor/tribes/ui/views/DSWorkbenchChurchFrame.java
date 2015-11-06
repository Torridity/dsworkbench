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
import de.tor.tribes.types.test.DummyVillage;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.panels.GenericTestPanel;
import de.tor.tribes.ui.editors.ChurchLevelCellEditor;
import de.tor.tribes.ui.models.ChurchTableModel;
import de.tor.tribes.ui.renderer.ColorCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PropertyHelper;
import de.tor.tribes.util.church.ChurchManager;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.ConsoleAppender;
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

/**
 * @author Charon
 */
public class DSWorkbenchChurchFrame extends AbstractDSWorkbenchFrame implements GenericManagerListener, ListSelectionListener {

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        ((ChurchTableModel) jChurchTable.getModel()).fireTableDataChanged();
    }
    private static Logger logger = Logger.getLogger("ChurchView");
    private static DSWorkbenchChurchFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;

    public static synchronized DSWorkbenchChurchFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchChurchFrame();
        }
        return SINGLETON;
    }

    /**
     * Creates new form DSWorkbenchChurchFrame
     */
    DSWorkbenchChurchFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jChurchPanel.add(centerPanel, BorderLayout.CENTER);
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
        jChurchTable.registerKeyboardAction(listener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jChurchTable.registerKeyboardAction(listener, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        try {
            jChurchFrameAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("church.frame.alwaysOnTop")));
            setAlwaysOnTop(jChurchFrameAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }

        jChurchTable.setModel(new ChurchTableModel());
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.church_view", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
        jChurchTable.getSelectionModel().addListSelectionListener(DSWorkbenchChurchFrame.this);
        pack();
    }

    @Override
    public void toBack() {
        jChurchFrameAlwaysOnTop.setSelected(false);
        fireChurchFrameOnTopEvent(null);
        super.toBack();
    }

    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".menu.visible", centerPanel.isMenuVisible());
        pConfig.setProperty(getPropertyPrefix() + ".alwaysOnTop", jChurchFrameAlwaysOnTop.isSelected());

        PropertyHelper.storeTableProperties(jChurchTable, pConfig, getPropertyPrefix());

    }

    public void restoreCustomProperties(Configuration pConfig) {
        centerPanel.setMenuVisible(pConfig.getBoolean(getPropertyPrefix() + ".menu.visible", true));

        try {
            jChurchFrameAlwaysOnTop.setSelected(pConfig.getBoolean(getPropertyPrefix() + ".alwaysOnTop"));
        } catch (Exception e) {
        }

        setAlwaysOnTop(jChurchFrameAlwaysOnTop.isSelected());

        PropertyHelper.restoreTableProperties(jChurchTable, pConfig, getPropertyPrefix());
    }

    public String getPropertyPrefix() {
        return "church.view";
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
        jChurchPanel = new org.jdesktop.swingx.JXPanel();
        jChurchFrameAlwaysOnTop = new javax.swing.JCheckBox();
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

        jChurchTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jChurchTable);

        jXPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        setTitle("Kirchen");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jChurchPanel.setBackground(new java.awt.Color(239, 235, 223));
        jChurchPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 500;
        gridBagConstraints.ipady = 300;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jChurchPanel, gridBagConstraints);

        jChurchFrameAlwaysOnTop.setText("Immer im Vordergrund");
        jChurchFrameAlwaysOnTop.setOpaque(false);
        jChurchFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireChurchFrameOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jChurchFrameAlwaysOnTop, gridBagConstraints);

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

    private void fireChurchFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireChurchFrameOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireChurchFrameOnTopEvent

    private void jXLabel1fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXLabel1fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXLabel1fireHideInfoEvent

    private void buildMenu() {
        JXTaskPane transferPane = new JXTaskPane();
        transferPane.setTitle("Übertragen");
        JXButton transferVillageList = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/ui/center_ingame.png")));
        transferVillageList.setToolTipText("Zentriert das Kirchendorf im Spiel");
        transferVillageList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                centerChurchInGame();
            }
        });
        transferPane.getContentPane().add(transferVillageList);

        if (!GlobalOptions.isMinimal()) {
            JXButton button = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/center_24x24.png")));
            button.setToolTipText("Zentriert das Kirchendorf auf der Hauptkarte");
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    centerChurchVillage();
                }
            });

            transferPane.getContentPane().add(button);
        }
        centerPanel.setupTaskPane(transferPane);
    }

    private Village getSelectedCurch() {
        int row = jChurchTable.getSelectedRow();
        if (row >= 0) {
            try {
                Village v = (Village) ((ChurchTableModel) jChurchTable.getModel()).getValueAt(jChurchTable.convertRowIndexToModel(row), 1);
                return v;
            } catch (Exception e) {
            }
        }
        return null;
    }

    private void centerChurchVillage() {
        Village v = getSelectedCurch();
        if (v != null) {
            DSWorkbenchMainFrame.getSingleton().centerVillage(v);
        } else {
            showInfo("Keine Kirche gewählt");
        }
    }

    private void centerChurchInGame() {
        Village v = getSelectedCurch();
        if (v != null) {
            BrowserCommandSender.centerVillage(v);
        } else {
            showInfo("Keine Kirche gewählt");
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jChurchTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Kirche gewählt" : " Kirchen gewählt"));
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
        int[] rows = jChurchTable.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        String message = ((rows.length == 1) ? "Kirchendorf " : (rows.length + " Kirchendörfer ")) + "wirklich löschen?";
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            //get markers to remove
            List<Village> toRemove = new LinkedList<Village>();
            jChurchTable.invalidate();
            for (int i = rows.length - 1; i >= 0; i--) {
                int row = jChurchTable.convertRowIndexToModel(rows[i]);
                int col = jChurchTable.convertColumnIndexToModel(1);
                Village v = ((Village) ((ChurchTableModel) jChurchTable.getModel()).getValueAt(row, col));
                toRemove.add(v);
            }
            jChurchTable.revalidate();
            //remove all selected markers and update the view once
            ChurchManager.getSingleton().removeChurches(toRemove.toArray(new Village[]{}));
            showSuccess(toRemove.size() + ((toRemove.size() == 1) ? " Kirche gelöscht" : " Kirchen gelöscht"));
        }
    }

    private void bbCopySelection() {
        try {
            int[] rows = jChurchTable.getSelectedRows();
            if (rows.length == 0) {
                return;
            }

            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]Kirchendörfer[/size][/u]\n\n");
            } else {
                buffer.append("[u]Kirchendörfer[/u]\n\n");
            }

            buffer.append("[table]\n");
            buffer.append("[**]Spieler[||]Dorf[||]Radius[/**]\n");


            for (int i = 0; i < rows.length; i++) {
                int row = jChurchTable.convertRowIndexToModel(rows[i]);
                int tribeCol = jChurchTable.convertColumnIndexToModel(0);
                int villageCol = jChurchTable.convertColumnIndexToModel(1);
                int rangeCol = jChurchTable.convertColumnIndexToModel(2);
                buffer.append("[*]").
                        append(((Tribe) ((ChurchTableModel) jChurchTable.getModel()).getValueAt(row, tribeCol)).toBBCode()).
                        append("[|]").
                        append(((Village) ((ChurchTableModel) jChurchTable.getModel()).getValueAt(row, villageCol)).toBBCode()).
                        append("[|]").
                        append(((Integer) ((ChurchTableModel) jChurchTable.getModel()).getValueAt(row, rangeCol)));
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
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Kirchen benötigen mehr als 1000 BB-Codes\n"
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
        ChurchManager.getSingleton().addManagerListener(this);
        MarkerManager.getSingleton().addManagerListener(this);
        jChurchTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        String[] cols = new String[]{"Radius", "Farbe"};
        for (String col : cols) {
            TableColumnExt columns = jChurchTable.getColumnExt(col);
            columns.setPreferredWidth(80);
            columns.setMaxWidth(80);
            columns.setWidth(80);
        }

        ((ChurchTableModel) jChurchTable.getModel()).fireTableDataChanged();
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        DSWorkbenchChurchFrame.getSingleton().resetView();
        for (int i = 0; i < 50; i++) {
            ChurchManager.getSingleton().addChurch(new DummyVillage((short) i, (short) i), 2);
        }

        DSWorkbenchChurchFrame.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchChurchFrame.getSingleton().setVisible(true);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JCheckBox jChurchFrameAlwaysOnTop;
    private org.jdesktop.swingx.JXPanel jChurchPanel;
    private static final org.jdesktop.swingx.JXTable jChurchTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXPanel jXPanel1;
    // End of variables declaration//GEN-END:variables

    static {
        HighlightPredicate.ColumnHighlightPredicate colu = new HighlightPredicate.ColumnHighlightPredicate(0, 1, 2);
        jChurchTable.setHighlighters(new CompoundHighlighter(colu, HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B)));

        jChurchTable.setColumnControlVisible(true);
        jChurchTable.setDefaultRenderer(Color.class, new ColorCellRenderer());
        jChurchTable.setDefaultEditor(Integer.class, new ChurchLevelCellEditor());
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
        jChurchTable.addHighlighter(new PainterHighlighter(HighlightPredicate.EDITABLE, new ImagePainter(back, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)));
    }
}
