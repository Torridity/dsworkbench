/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchDistanceFrame.java
 *
 * Created on 30.09.2009, 14:49:50
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.types.test.DummyProfile;
import de.tor.tribes.types.test.DummyVillage;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.GenericTestPanel;
import de.tor.tribes.ui.models.DistanceTableModel;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.DistanceTableCellRenderer;
import de.tor.tribes.ui.renderer.VillageCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.dist.DistanceManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;

/**
 *
 * @author Jejkal
 */
public class DSWorkbenchDistanceFrame extends AbstractDSWorkbenchFrame implements ListSelectionListener {

    private static Logger logger = Logger.getLogger("DistanceFrame");
    private static DSWorkbenchDistanceFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;
    private static DistanceTableCellRenderer cellRenderer = new DistanceTableCellRenderer();

    public static synchronized DSWorkbenchDistanceFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchDistanceFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchDistanceFrame */
    DSWorkbenchDistanceFrame() {
        initComponents();
        centerPanel = new GenericTestPanel(false);
        jDistancePanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildPanel(jPanel2);
        jDistanceTable.setModel(DistanceTableModel.getSingleton());
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        jDistanceTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedColumns();
            }
        }, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jDistanceTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pasteFromClipboard();
            }
        }, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jDistanceTable.getActionMap().put("find", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //disable find
            }
        });
        jDistanceTable.getSelectionModel().addListSelectionListener(DSWorkbenchDistanceFrame.this);
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        //   GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.distance_overview", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        //  if (e.getValueIsAdjusting()) {
        int selectionCount = jDistanceTable.getColumnModel().getSelectedColumns().length;
        if (selectionCount != 0) {
            showInfo(selectionCount + ((selectionCount == 1) ? " Spalte gewählt" : " Spalten gewählt"));
        }
        // }
    }

    @Override
    public void resetView() {
        jDistanceTable.invalidate();
        jDistanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int w0 = 100;
        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
            int w = getGraphics().getFontMetrics().stringWidth(v.getFullName());
            if (w > w0) {
                w0 = w;
            }
        }
        for (int i = 0; i < jDistanceTable.getColumnCount(); i++) {
            TableColumn column = jDistanceTable.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setWidth(w0);
                column.setPreferredWidth(w0);
            } else {
                String v = (String) column.getHeaderValue();
                int w = getGraphics().getFontMetrics().stringWidth(v);
                column.setWidth(w);
                column.setPreferredWidth(w);
            }
        }

        jDistanceTable.setModel(DistanceTableModel.getSingleton());
        jDistanceTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        jDistanceTable.revalidate();
        jDistanceTable.repaint();
    }

    private void deleteSelectedColumns() {
        List<TableColumn> colsToRemove = new LinkedList<TableColumn>();
        int[] selection = jDistanceTable.getSelectedColumns();
        int[] realCols = new int[selection.length];
        for (int i = 0; i < selection.length; i++) {
            colsToRemove.add(jDistanceTable.getColumnModel().getColumn(selection[i]));
            realCols[i] = jDistanceTable.convertColumnIndexToModel(selection[i]);
        }

        colsToRemove.remove(jDistanceTable.getColumn("Eigene"));
        jDistanceTable.invalidate();
        for (TableColumn colu : colsToRemove) {
            jDistanceTable.getColumnModel().removeColumn(colu);
        }
        DistanceManager.getSingleton().removeVillages(realCols);
        jDistanceTable.revalidate();
        ((DistanceTableModel) jDistanceTable.getModel()).fireTableStructureChanged();
        resetView();
        showSuccess(colsToRemove.size() + ((colsToRemove.size() == 1) ? " Spalte " : " Spalten ") + "gelöscht");
    }

    private void pasteFromClipboard() {
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            List<Village> villages = PluginManager.getSingleton().executeVillageParser((String) t.getTransferData(DataFlavor.stringFlavor));//VillageParser.parse((String) t.getTransferData(DataFlavor.stringFlavor));
            if (villages == null || villages.isEmpty()) {
                showError("Es konnten keine Dorfkoodinaten in der Zwischenablage gefunden werden.");
                return;
            } else {
                jDistanceTable.invalidate();
                for (Village v : villages) {
                    DistanceManager.getSingleton().addVillage(v);
                }
                DistanceTableModel.getSingleton().fireTableStructureChanged();
                resetView();
            }
            showSuccess(villages.size() + ((villages.size() == 1) ? " Dorf " : " Dörfer ") + "aus der Zwischenablage eingefügt");
        } catch (Exception e) {
            logger.error("Failed to paste villages from clipboard", e);
            showError("Fehler beim Einfügen aus der Zwischenablage");
        }
    }

    public void showSuccess(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.GREEN));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jCopyFromClipboardEvent = new javax.swing.JButton();
        jCopyFromClipboardEvent1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jMinValue = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jMaxValue = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jCopyFromClipboardEvent2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jDistancePanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jCopyFromClipboardEvent.setBackground(new java.awt.Color(239, 235, 223));
        jCopyFromClipboardEvent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/clipboard.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/tor/tribes/ui/Bundle"); // NOI18N
        jCopyFromClipboardEvent.setText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.text")); // NOI18N
        jCopyFromClipboardEvent.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.toolTipText")); // NOI18N
        jCopyFromClipboardEvent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCopyVillagesFromClipboardEvent(evt);
            }
        });

        jCopyFromClipboardEvent1.setBackground(new java.awt.Color(239, 235, 223));
        jCopyFromClipboardEvent1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jCopyFromClipboardEvent1.setText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.text")); // NOI18N
        jCopyFromClipboardEvent1.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.toolTipText")); // NOI18N
        jCopyFromClipboardEvent1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveColumnEvent(evt);
            }
        });

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setForeground(new java.awt.Color(0, 255, 0));
        jLabel1.setText("Minimale Entfernung");
        jLabel1.setOpaque(true);

        jMinValue.setText("10");
        jMinValue.setToolTipText("Gibt die Entfernung an, ab der Werte in der Tabelle grün eingezeichnet werden");
        jMinValue.setMaximumSize(new java.awt.Dimension(80, 20));
        jMinValue.setMinimumSize(new java.awt.Dimension(80, 20));
        jMinValue.setPreferredSize(new java.awt.Dimension(80, 20));

        jLabel2.setBackground(new java.awt.Color(0, 0, 0));
        jLabel2.setForeground(new java.awt.Color(255, 0, 0));
        jLabel2.setText("Maximale Entfernung");
        jLabel2.setOpaque(true);

        jMaxValue.setText("20");
        jMaxValue.setToolTipText("Gibt die Entfernung an, ab der Werte in der Tabelle rot eingezeichnet werden");
        jMaxValue.setMaximumSize(new java.awt.Dimension(80, 20));
        jMaxValue.setMinimumSize(new java.awt.Dimension(80, 20));
        jMaxValue.setPreferredSize(new java.awt.Dimension(80, 20));

        jLabel3.setText("Felder");

        jLabel4.setText("Felder");

        jCopyFromClipboardEvent2.setBackground(new java.awt.Color(239, 235, 223));
        jCopyFromClipboardEvent2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/replace2.png"))); // NOI18N
        jCopyFromClipboardEvent2.setText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.text")); // NOI18N
        jCopyFromClipboardEvent2.setToolTipText(bundle.getString("DSWorkbenchAttackFrame.jCleanupAttacksButton.toolTipText")); // NOI18N
        jCopyFromClipboardEvent2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireUpdateEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jCopyFromClipboardEvent)
                        .addComponent(jCopyFromClipboardEvent1))
                    .addComponent(jCopyFromClipboardEvent2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jMinValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jMaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCopyFromClipboardEvent)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCopyFromClipboardEvent1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCopyFromClipboardEvent2)
                .addGap(18, 278, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jMinValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jMaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );

        jPanel2.setLayout(new java.awt.BorderLayout());

        jDistanceTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jDistanceTable.setColumnControlVisible(true);
        jDistanceTable.setColumnSelectionAllowed(true);
        jScrollPane2.setViewportView(jDistanceTable);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jXLabel1fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        jPanel2.add(infoPanel, java.awt.BorderLayout.SOUTH);

        setTitle("Entfernungsübersicht");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jCheckBox1.setText("Immer im Vordergrund");
        jCheckBox1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jCheckBox1, gridBagConstraints);

        jDistancePanel.setBackground(new java.awt.Color(239, 235, 223));
        jDistancePanel.setPreferredSize(new java.awt.Dimension(300, 400));
        jDistancePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 668;
        gridBagConstraints.ipady = 471;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jDistancePanel, gridBagConstraints);

        capabilityInfoPanel1.setCopyable(false);
        capabilityInfoPanel1.setSearchable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(capabilityInfoPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireCopyVillagesFromClipboardEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCopyVillagesFromClipboardEvent
        try {
            Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

            List<Village> villages = PluginManager.getSingleton().executeVillageParser((String) t.getTransferData(DataFlavor.stringFlavor));//VillageParser.parse((String) t.getTransferData(DataFlavor.stringFlavor));
            if (villages == null || villages.isEmpty()) {
                JOptionPaneHelper.showInformationBox(this, "Es konnten keine Dorfkoodinaten in der Zwischenablage gefunden werden.", "Information");
                return;
            } else {
                jDistanceTable.invalidate();
                for (Village v : villages) {
                    DistanceManager.getSingleton().addVillage(v);
                }
                DistanceTableModel.getSingleton().fireTableStructureChanged();
                jDistanceTable.revalidate();
                resetView();
            }
        } catch (Exception e) {
            logger.error("Failed to parse villages from clipboard", e);
        }
    }//GEN-LAST:event_fireCopyVillagesFromClipboardEvent

    private void fireRemoveColumnEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveColumnEvent
        int[] cols = jDistanceTable.getSelectedColumns();
        if (cols == null) {
            return;
        }
        jDistanceTable.invalidate();
        int[] correctedCols = new int[cols.length];
        for (int i = 0; i < cols.length; i++) {
            correctedCols[i] = jDistanceTable.convertColumnIndexToModel(cols[i]);
        }

        DistanceManager.getSingleton().removeVillages(correctedCols);
        DistanceTableModel.getSingleton().fireTableStructureChanged();
        jDistanceTable.revalidate();
        resetView();
    }//GEN-LAST:event_fireRemoveColumnEvent

    private void fireUpdateEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireUpdateEvent
        int min = 10;
        int max = 20;
        try {
            min = Integer.parseInt(jMinValue.getText());
        } catch (Exception e) {
            JOptionPaneHelper.showWarningBox(this, "Der Eintrag für den minimalen Wert ist ungültig.", "Fehler");
            return;
        }
        try {
            max = Integer.parseInt(jMaxValue.getText());
        } catch (Exception e) {
            JOptionPaneHelper.showWarningBox(this, "Der Eintrag für den maximalen Wert ist ungültig.", "Fehler");
            return;
        }

        if (min >= max) {
            JOptionPaneHelper.showWarningBox(this, "Der minimale Wert muss kleiner als der maximale Wert sein.", "Fehler");
            return;
        }

        cellRenderer.setMarkerMin(min);
        cellRenderer.setMarkerMax(max);
        System.out.println(jDistanceTable.getColumnModel().getColumn(0).getHeaderRenderer());
        jDistanceTable.repaint();
    }//GEN-LAST:event_fireUpdateEvent

    private void jXLabel1fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXLabel1fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXLabel1fireHideInfoEvent

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
        try {
            for (Village v : pVillages) {
                DistanceManager.getSingleton().addVillage(v);
            }
            DistanceTableModel.getSingleton().fireTableStructureChanged();
            jDistanceTable.revalidate();
            resetView();
        } catch (Exception e) {
            logger.error("Failed to received dropped villages", e);
        }
    }

    public static void main(String args[]) {
        Locale.setDefault(Locale.GERMAN);
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedProfile(new DummyProfile());
        for (int i = 0; i < 10; i++) {
            DistanceManager.getSingleton().addVillage(new DummyVillage((short) i, (short) i));
        }
        DSWorkbenchDistanceFrame.getSingleton().resetView();
        DSWorkbenchDistanceFrame.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchDistanceFrame.getSingleton().setVisible(true);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JButton jCopyFromClipboardEvent;
    private javax.swing.JButton jCopyFromClipboardEvent1;
    private javax.swing.JButton jCopyFromClipboardEvent2;
    private javax.swing.JPanel jDistancePanel;
    private static final org.jdesktop.swingx.JXTable jDistanceTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jMaxValue;
    private javax.swing.JTextField jMinValue;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    // End of variables declaration//GEN-END:variables

    static {
        HighlightPredicate.ColumnHighlightPredicate colu = new HighlightPredicate.ColumnHighlightPredicate(0);
        jDistanceTable.setHighlighters(new CompoundHighlighter(colu, HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B)));
        jDistanceTable.setColumnControlVisible(true);
        jDistanceTable.setDefaultRenderer(Double.class, new DistanceTableCellRenderer());
        jDistanceTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
    }
}
