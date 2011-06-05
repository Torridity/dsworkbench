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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.test.DummyProfile;
import de.tor.tribes.types.test.DummyVillage;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.GenericTestPanel;
import de.tor.tribes.ui.models.DistanceTableModel;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.DistanceTableCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.ProfileManager;
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
import org.jdesktop.swingx.JXTaskPane;
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
    private static final DistanceTableCellRenderer cellRenderer = new DistanceTableCellRenderer();

    public static synchronized DSWorkbenchDistanceFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchDistanceFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchDistanceFrame */
    DSWorkbenchDistanceFrame() {
        initComponents();
        centerPanel = new GenericTestPanel(true);
        jDistancePanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildPanel(jPanel2);
        buildMenu();
        // jDistanceTable.setModel(DistanceTableModel.getSingleton());
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

    private void buildMenu() {
        JXTaskPane editPane = new JXTaskPane();
        editPane.setTitle("Bereichsfärbung");
        editPane.getContentPane().add(rangeSlider1);
        centerPanel.setupTaskPane(editPane);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int selectionCount = jDistanceTable.getColumnModel().getSelectedColumns().length;
        if (selectionCount != 0) {
            showInfo(selectionCount + ((selectionCount == 1) ? " Spalte gewählt" : " Spalten gewählt"));
        }
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

        jDistanceTable.setModel(new DistanceTableModel());
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
                //jDistanceTable.invalidate();
                for (Village v : villages) {
                    DistanceManager.getSingleton().addVillage(v);
                }
                ((DistanceTableModel) jDistanceTable.getModel()).fireTableStructureChanged();
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

        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        rangeSlider1 = new com.jidesoft.swing.RangeSlider();
        jCheckBox1 = new javax.swing.JCheckBox();
        jDistancePanel = new javax.swing.JPanel();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();

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
        jDistanceTable.setDoubleBuffered(true);
        jDistanceTable.setEditable(false);
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

        rangeSlider1.setMajorTickSpacing(10);
        rangeSlider1.setMaximum(70);
        rangeSlider1.setMinorTickSpacing(1);
        rangeSlider1.setPaintLabels(true);
        rangeSlider1.setPaintTicks(true);
        rangeSlider1.setValue(10);
        rangeSlider1.setExtent(10);
        rangeSlider1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireRangeUpdateEvent(evt);
            }
        });
        rangeSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireStateChangedEvent(evt);
            }
        });

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

        capabilityInfoPanel1.setBbSupport(false);
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

    private void jXLabel1fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jXLabel1fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_jXLabel1fireHideInfoEvent

    private void fireRangeUpdateEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRangeUpdateEvent
        cellRenderer.setMarkerMin(rangeSlider1.getLowValue());
        cellRenderer.setMarkerMax(rangeSlider1.getHighValue());
        jDistanceTable.repaint();
    }//GEN-LAST:event_fireRangeUpdateEvent

    private void fireStateChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireStateChangedEvent
        cellRenderer.setMarkerMin(rangeSlider1.getLowValue());
        cellRenderer.setMarkerMax(rangeSlider1.getHighValue());
        jDistanceTable.repaint();
    }//GEN-LAST:event_fireStateChangedEvent

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
        try {
            for (Village v : pVillages) {
                DistanceManager.getSingleton().addVillage(v);
            }
            ((DistanceTableModel) jDistanceTable.getModel()).fireTableStructureChanged();
            jDistanceTable.revalidate();
            resetView();
        } catch (Exception e) {
            logger.error("Failed to received dropped villages", e);
        }
    }

    public static void main(String args[]) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));

        GlobalOptions.setSelectedServer("de68");
        DataHolder.getSingleton().loadData(false);
        ProfileManager.getSingleton().loadProfiles();

        GlobalOptions.setSelectedProfile(ProfileManager.getSingleton().getProfiles("de68")[0]);
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        GlobalOptions.setSelectedProfile(new DummyProfile());
        for (int i = 0; i < 10; i++) {
            DistanceManager.getSingleton().addVillage(DataHolder.getSingleton().getRandomVillage());
        }
        DSWorkbenchDistanceFrame.getSingleton().resetView();
        DSWorkbenchDistanceFrame.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchDistanceFrame.getSingleton().setVisible(true);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JPanel jDistancePanel;
    private static final org.jdesktop.swingx.JXTable jDistanceTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private com.jidesoft.swing.RangeSlider rangeSlider1;
    // End of variables declaration//GEN-END:variables

    static {
        HighlightPredicate.ColumnHighlightPredicate colu = new HighlightPredicate.ColumnHighlightPredicate(0);
        jDistanceTable.setHighlighters(new CompoundHighlighter(colu, HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B)));
        jDistanceTable.setColumnControlVisible(true);
        jDistanceTable.setDefaultRenderer(Double.class, cellRenderer);
        //jDistanceTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
    }
}
