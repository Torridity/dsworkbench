/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AttackTargetPanel.java
 *
 * Created on Oct 15, 2011, 9:54:36 AM
 */
package de.tor.tribes.ui.wiz.tap;

import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSplitPane;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.components.VillageOverviewMapPanel;
import de.tor.tribes.ui.components.VillageSelectionPanel;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.FakeCellRenderer;
import de.tor.tribes.util.Constants;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanel;
import org.netbeans.spi.wizard.WizardPanelNavResult;

/**
 *
 * @author Torridity
 */
public class AttackTargetPanel extends javax.swing.JPanel implements WizardPanel {

    private static final String GENERAL_INFO = "<html>Du befindest dich in der Zielauswahl f&uuml; die zu planenden Angriffe. W&auml;hle die "
            + "D&ouml;rfer aus die du angreifen magst und f&uuml;ge sie &uuml;ber den entsprechenden Button ein. Du kannst DS Workbench "
            + "per STRG+V auch dazu veranlassen, in der Zwischenablage nach Dorfkoordinaten zu suchen.<br/>"
            + "Willst du mehrere Angriffe auf ein Dorf durchf&uuml;hren, so f&uuml;ge diese entsprechenden D&ouml;rfer einfach mehrmals ein."
            + "</html>";
    private static AttackTargetPanel singleton = null;
    private WizardController controller = null;
    private VillageSelectionPanel villageSelectionPanel = null;
    private VillageOverviewMapPanel overviewPanel = null;

    public static synchronized AttackTargetPanel getSingleton() {
        if (singleton == null) {
            singleton = new AttackTargetPanel();
        }
        return singleton;
    }

    public void setController(WizardController pWizCtrl) {
        controller = pWizCtrl;
    }

    /** Creates new form AttackTargetPanel */
    AttackTargetPanel() {
        initComponents();
        jVillageTable.setModel(new TargetTableModel());
        jVillageTable.setDefaultRenderer(Boolean.class, new FakeCellRenderer());
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
        villageSelectionPanel.enableSelectionElement(VillageSelectionPanel.SELECTION_ELEMENT.GROUP, false);
        villageSelectionPanel.setup();
        jPanel1.add(villageSelectionPanel, BorderLayout.CENTER);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jInfoScrollPane = new javax.swing.JScrollPane();
        jInfoTextPane = new javax.swing.JTextPane();
        jDataPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
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

        jInfoTextPane.setContentType("text/html");
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        jDataPanel.setMinimumSize(new java.awt.Dimension(0, 130));
        jDataPanel.setPreferredSize(new java.awt.Dimension(0, 130));
        jDataPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jDataPanel.add(jPanel1, gridBagConstraints);

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

        setLayout(new java.awt.GridBagLayout());

        jXCollapsiblePane1.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jXCollapsiblePane1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10));
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

    private TargetTableModel getModel() {
        return (TargetTableModel) jVillageTable.getModel();
    }

    private void addVillages(Village[] pVillages) {
        if (pVillages.length == 0) {
            return;
        }
        for (Village village : pVillages) {
            getModel().addRow(village, villageSelectionPanel.isFake());
        }
        if (controller != null && getModel().getRowCount() > 0) {
            controller.setProblem(null);
        }
        jStatusLabel.setText(pVillages.length + " Dorf/Dörfer eingefügt");
        updateOverview();
    }

    private void pasteFromClipboard() {
        String data = "";
        try {
            data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            List<Village> villages = PluginManager.getSingleton().executeVillageParser(data);
            if (!villages.isEmpty()) {
                addVillages(villages.toArray(new Village[villages.size()]));
            }
        } catch (HeadlessException he) {
        } catch (UnsupportedFlavorException ufe) {
        } catch (IOException ioe) {
        }
    }

    private void deleteSelection() {
        int[] selection = jVillageTable.getSelectedRows();
        if (selection.length > 0) {
            List<Integer> rows = new LinkedList<Integer>();
            for (int i : selection) {
                rows.add(jVillageTable.convertRowIndexToModel(i));
            }
            Collections.sort(rows);
            for (int i = rows.size() - 1; i >= 0; i--) {
                getModel().removeRow(rows.get(i), jVillageTable.convertRowIndexToView(rows.get(i)));
            }
            if (controller != null && getModel().getRowCount() == 0) {
                controller.setProblem("Keine Ziele gewählt");
            }
            jStatusLabel.setText(selection.length + " Dorf/Dörfer entfernt");
            updateOverview();
        }
    }

    public Village[] getUsedVillages() {
        List<Village> result = new LinkedList<Village>();
        TargetTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            result.add(model.getRow(i).getVillage());
        }
        return result.toArray(new Village[result.size()]);
    }

    public List<AttackTargetElement> getAllElements() {
        List<AttackTargetElement> elements = new LinkedList<AttackTargetElement>();
        TargetTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            elements.add(model.getRow(i));
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
        for (AttackSourceElement element : AttackSourceFilterPanel.getSingleton().getFilteredElements()) {
            if (!element.isIgnored()) {
                overviewPanel.addVillage(new Point(element.getVillage().getX(), element.getVillage().getY()), Color.yellow);
            }
        }

        for (int i = 0; i < getModel().getRowCount(); i++) {
            Village v = getModel().getRow(i).getVillage();
            overviewPanel.addVillage(new Point(v.getX(), v.getY()), Color.red);
        }
        overviewPanel.repaint();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.tor.tribes.ui.components.CapabilityInfoPanel capabilityInfoPanel1;
    private javax.swing.JPanel jDataPanel;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel jStatusLabel;
    private javax.swing.JScrollPane jTableScrollPane;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXTable jVillageTable;
    private javax.swing.JPanel jVillageTablePanel;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private com.jidesoft.swing.JideSplitPane jideSplitPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (getModel().getRowCount() > 0) {
            AttackCalculationPanel.getSingleton().updateStatus();
            return WizardPanelNavResult.PROCEED;
        }

        controller.setProblem("Keine Ziele gewählt");
        return WizardPanelNavResult.REMAIN_ON_PAGE;
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

class TargetTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Spieler", "Dorf", "Fake", "Angriffe"
    };
    private Class[] types = new Class[]{
        Tribe.class, Village.class, Boolean.class, Integer.class
    };
    private final List<AttackTargetElement> elements = new LinkedList<AttackTargetElement>();

    public TargetTableModel() {
        super();
    }

    public void addRow(final Village pVillage, boolean pFake) {
        Object result = CollectionUtils.find(elements, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ((AttackTargetElement) o).getVillage().equals(pVillage);
            }
        });

        if (result == null) {
            elements.add(new AttackTargetElement(pVillage, pFake));
        } else {
            AttackTargetElement resultElem = (AttackTargetElement) result;
            resultElem.addAttack();
            resultElem.setFake(pFake);
        }
        fireTableDataChanged();
    }

    protected void removeTargets(List<Village> pToRemove) {
        for (AttackTargetElement elem : elements.toArray(new AttackTargetElement[elements.size()])) {
            if (pToRemove.contains(elem.getVillage())) {
                elements.remove(elem);
            }
        }
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        if (elements == null) {
            return 0;
        }
        return elements.size();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public void removeRow(int row, int viewRow) {
        AttackTargetElement elem = elements.get(row);
        if (!elem.removeAttack()) {
            elements.remove(row);
            fireTableDataChanged();
        } else {
            fireTableRowsUpdated(row, row);
        }
    }

    public AttackTargetElement getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        AttackTargetElement element = elements.get(row);
        switch (column) {
            case 0:
                return element.getVillage().getTribe();
            case 1:
                return element.getVillage();
            case 2:
                return element.isFake();
            default:
                return element.getAttacks();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}

class AttackTargetElement {

    private Village village = null;
    private int attacks = 1;
    private boolean fake = false;

    public AttackTargetElement(Village pVillage) {
        village = pVillage;
    }

    public AttackTargetElement(Village pVillage, boolean pFake) {
        this(pVillage);
        fake = pFake;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AttackTargetElement) {
            return ((AttackTargetElement) obj).getVillage().equals(getVillage());
        }
        return false;
    }

    public Village getVillage() {
        return village;
    }

    public int getAttacks() {
        return attacks;
    }

    public void addAttack() {
        attacks++;
    }

    public boolean removeAttack() {
        attacks--;
        return attacks > 0;
    }

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean pValue) {
        fake = pValue;
    }
}
