/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchFormFrame.java
 *
 * Created on 03.01.2009, 23:50:25
 */
package de.tor.tribes.ui.views;

import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.types.AbstractForm;
import de.tor.tribes.types.Circle;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.AbstractDSWorkbenchFrame;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.FormConfigFrame;
import de.tor.tribes.ui.GenericTestPanel;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.models.FormTableModel;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.VisibilityCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.map.FormManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTaskPane;
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
public class DSWorkbenchFormFrame extends AbstractDSWorkbenchFrame implements ListSelectionListener, GenericManagerListener {

    private static Logger logger = Logger.getLogger("FormFrame");
    private static DSWorkbenchFormFrame SINGLETON = null;
    private GenericTestPanel centerPanel = null;

    public static synchronized DSWorkbenchFormFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchFormFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchFormFrame */
    DSWorkbenchFormFrame() {
        initComponents();
        centerPanel = new GenericTestPanel();
        jFormPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setChildPanel(jFormTablePanel);
        jFormsTable.setModel(new FormTableModel());
        jFormsTable.getSelectionModel().addListSelectionListener(DSWorkbenchFormFrame.this);
        buildMenu();
        try {
            jAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("form.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }

        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        jFormsTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                copySelectionToInternalClipboard();
            }
        }, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        //   GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.form_view", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>
    }

    @Override
    public void resetView() {
        //update view
        FormManager.getSingleton().addManagerListener(DSWorkbenchFormFrame.this);
        jFormsTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        String[] cols = new String[]{"X", "Y", "Höhe", "Breite", "Sichtbar"};
        for (String col : cols) {
            TableColumnExt columns = jFormsTable.getColumnExt(col);
            columns.setPreferredWidth(80);
            columns.setMaxWidth(80);
            columns.setWidth(80);
        }

        ((FormTableModel) jFormsTable.getModel()).fireTableDataChanged();
    }

    private void buildMenu() {
        JXTaskPane editPane = new JXTaskPane();
        editPane.setTitle("Bearbeiten");
        JXButton editButton = new JXButton(new ImageIcon("./graphics/icons/replace2.png"));
        editButton.setToolTipText("Die ausgewählte Zeichnung bearbeiten");
        editButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                showEditFrame();
            }
        });

        editPane.getContentPane().add(editButton);

        JXTaskPane miscPane = new JXTaskPane();
        miscPane.setTitle("Sonstiges");
        JXButton centerButton = new JXButton(new ImageIcon(DSWorkbenchChurchFrame.class.getResource("/res/center_24x24.png")));
        centerButton.setToolTipText("Zentriert die Zeichnung auf der Hauptkarte");
        centerButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                centerFormOnMap();
            }
        });

        miscPane.getContentPane().add(centerButton);

        JXButton searchButton = new JXButton(new ImageIcon("./graphics/icons/search.png"));
        searchButton.setToolTipText("Lässt die gewählten Zeichnungen auf der Hauptkarte kurz aufblinken");
        searchButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                showFormOnMap();
            }
        });
        miscPane.getContentPane().add(searchButton);

        centerPanel.setupTaskPane(editPane, miscPane);
    }

    private void copySelectionToInternalClipboard() {
        try {
            List<AbstractForm> selection = getSelectedForms();
            if (selection.isEmpty()) {
                return;
            }
            ArrayList<Village> villages = new ArrayList<Village>();
            for (AbstractForm f : selection) {
                for (Village v : f.getContainedVillages()) {
                    if (!villages.contains(v)) {
                        villages.add(v);
                    }
                }
            }
            StringBuilder builder = new StringBuilder();
            for (Village v : villages) {
                builder.append(v.toString()).append("\n");
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
            showSuccess(villages.size() + ((villages.size() == 1) ? " Dorf " : " Dörfer ") + "in die Zwischenablage kopiert");
        } catch (Exception e) {
            logger.error("Failed to copy villages to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            showError(result);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jFormsTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Zeichnung gewählt" : " Zeichnungen gewählt"));
            }
        }
    }

    public void showInfo(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(getBackground()));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    public void showSuccess(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.GREEN));
        jXLabel1.setForeground(Color.BLACK);
        jXLabel1.setText(pMessage);
    }

    public void showError(String pMessage) {
        infoPanel.setCollapsed(false);
        jXLabel1.setBackgroundPainter(new MattePainter(Color.RED));
        jXLabel1.setForeground(Color.WHITE);
        jXLabel1.setText(pMessage);
    }

    public void updateVisibility() {
        ((FormTableModel) jFormsTable.getModel()).fireTableDataChanged();
    }

    private void centerFormOnMap() {
        List<AbstractForm> selection = getSelectedForms();
        if (selection.isEmpty()) {
            showError("Keine Zeichnung gewählt");
            return;
        }
        Rectangle r = selection.get(0).getBounds();
        if (r != null) {
            DSWorkbenchMainFrame.getSingleton().centerPosition((int) Math.rint(r.getCenterX()), (int) Math.rint(r.getCenterY()));
        }
    }

    private void showEditFrame() {
        List<AbstractForm> selection = getSelectedForms();
        if (selection.isEmpty()) {
            showError("Keine Zeichnung gewählt");
            return;
        }

        AbstractForm toEdit = selection.get(0);
        if (toEdit != null) {
            if ((MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_DRAW_CIRCLE)
                    || (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_DRAW_LINE)
                    || (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_DRAW_RECT)
                    || (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_DRAW_TEXT)
                    || (MapPanel.getSingleton().getCurrentCursor() == ImageManager.CURSOR_DRAW_FREEFORM)) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
            }
            FormConfigFrame.getSingleton().setupAndShowInEditMode(toEdit);
        }
    }

    private void showFormOnMap() {
        List<AbstractForm> selection = getSelectedForms();
        if (selection.isEmpty()) {
            showError("Keine Zeichnung gewählt");
            return;
        }
        for (AbstractForm f : selection) {
            f.setShowMode(true);
        }
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

        jFormTablePanel = new org.jdesktop.swingx.JXPanel();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jFormPanel = new javax.swing.JPanel();
        jAlwaysOnTop = new javax.swing.JCheckBox();
        capabilityInfoPanel1 = new de.tor.tribes.ui.CapabilityInfoPanel();

        jFormTablePanel.setLayout(new java.awt.BorderLayout());

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        jFormTablePanel.add(infoPanel, java.awt.BorderLayout.SOUTH);

        jFormsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(jFormsTable);

        jFormTablePanel.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        setTitle("Formen");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jFormPanel.setBackground(new java.awt.Color(239, 235, 223));
        jFormPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 439;
        gridBagConstraints.ipady = 319;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jFormPanel, gridBagConstraints);

        jAlwaysOnTop.setText("Immer im Vordergrund");
        jAlwaysOnTop.setOpaque(false);
        jAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireFormFrameAlwaysOnTopEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jAlwaysOnTop, gridBagConstraints);

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

    private void fireFormFrameAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireFormFrameAlwaysOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireFormFrameAlwaysOnTopEvent

    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        infoPanel.setCollapsed(true);
}//GEN-LAST:event_fireHideInfoEvent

    private List<AbstractForm> getSelectedForms() {
        final List<AbstractForm> selectedForms = new LinkedList<AbstractForm>();
        int[] selectedRows = jFormsTable.getSelectedRows();
        if (selectedRows != null && selectedRows.length < 1) {
            return selectedForms;
        }
        for (Integer selectedRow : selectedRows) {
            AbstractForm f = (AbstractForm) FormManager.getSingleton().getAllElements().get(jFormsTable.convertRowIndexToModel(selectedRow));
            if (f != null) {
                selectedForms.add(f);
            }
        }
        return selectedForms;
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    @Override
    public void dataChangedEvent() {
        dataChangedEvent(null);
    }

    @Override
    public void dataChangedEvent(String pGroup) {
        ((FormTableModel) jFormsTable.getModel()).fireTableDataChanged();
    }

    public static void main(String[] args) {
        try {
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        DSWorkbenchFormFrame.getSingleton().setSize(800, 600);
        FormManager.getSingleton().addForm(new Circle());
        FormManager.getSingleton().addForm(new de.tor.tribes.types.Rectangle());
        FormManager.getSingleton().addForm(new Circle());
        FormManager.getSingleton().addForm(new de.tor.tribes.types.Rectangle());
        FormManager.getSingleton().addForm(new Circle());
        FormManager.getSingleton().addForm(new de.tor.tribes.types.Rectangle());
        FormManager.getSingleton().addForm(new Circle());
        FormManager.getSingleton().addForm(new de.tor.tribes.types.Rectangle());
        DSWorkbenchFormFrame.getSingleton().resetView();
        DSWorkbenchFormFrame.getSingleton().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DSWorkbenchFormFrame.getSingleton().setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.CapabilityInfoPanel capabilityInfoPanel1;
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JCheckBox jAlwaysOnTop;
    private javax.swing.JPanel jFormPanel;
    private org.jdesktop.swingx.JXPanel jFormTablePanel;
    private static final org.jdesktop.swingx.JXTable jFormsTable = new org.jdesktop.swingx.JXTable();
    private javax.swing.JScrollPane jScrollPane4;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    // End of variables declaration//GEN-END:variables

    static {
        jFormsTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jFormsTable.setColumnControlVisible(true);
        jFormsTable.setDefaultRenderer(Boolean.class, new VisibilityCellRenderer());
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
        jFormsTable.addHighlighter(new PainterHighlighter(HighlightPredicate.EDITABLE, new ImagePainter(back, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)));
    }
}
