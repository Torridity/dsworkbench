/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AttackSourcePanel.java
 *
 * Created on Oct 15, 2011, 9:54:36 AM
 */
package de.tor.tribes.ui.wiz.dep;

import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideSplitPane;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.Defense;
import de.tor.tribes.types.DefenseInformation;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.TroopSplitDialog.TroopSplit;
import de.tor.tribes.ui.components.VillageOverviewMapPanel;
import de.tor.tribes.ui.components.VillageSelectionPanel;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.views.DSWorkbenchSOSRequestAnalyzer;
import de.tor.tribes.ui.wiz.dep.DefenseSourcePanel.SupportSourceElement;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.PluginManager;
import de.tor.tribes.util.TableHelper;
import de.tor.tribes.util.sos.SOSManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
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
public class DefenseSourcePanel extends javax.swing.JPanel implements WizardPanel {

    private static final String GENERAL_INFO = "Du befindest dich in der Dorfauswahl. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, "
            + "mit denen du verteidigen m&ouml;chtest. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:"
            + "<ul> <li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>"
            + "<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Gruppen der Gruppen&uuml;bersicht</li>"
            + "</ul> </html>";
    private static DefenseSourcePanel singleton = null;
    private WizardController controller = null;
    private VillageSelectionPanel villageSelectionPanel = null;
    private VillageOverviewMapPanel overviewPanel = null;

    public static synchronized DefenseSourcePanel getSingleton() {
        if (singleton == null) {
            singleton = new DefenseSourcePanel();
        }
        return singleton;
    }

    public void setController(WizardController pWizCtrl) {
        controller = pWizCtrl;
    }

    /** Creates new form AttackSourcePanel */
    DefenseSourcePanel() {
        initComponents();
        jVillageTable.setModel(new SupportTableModel());
        jVillageTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jVillageTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());

        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        villageSelectionPanel = new VillageSelectionPanel(new VillageSelectionPanel.VillageSelectionPanelListener() {

            @Override
            public void fireVillageSelectionEvent(Village[] pSelection) {
                addVillages(pSelection);
            }
        });


        villageSelectionPanel.enableSelectionElement(VillageSelectionPanel.SELECTION_ELEMENT.ALLY, false);
        villageSelectionPanel.enableSelectionElement(VillageSelectionPanel.SELECTION_ELEMENT.TRIBE, false);
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
        jVillageTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pasteFromClipboard();

            }
        }, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
        jVillageTable.registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelection();
            }
        }, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jInfoTextPane.setText(GENERAL_INFO);
        overviewPanel = new VillageOverviewMapPanel();
        jPanel2.add(overviewPanel, BorderLayout.CENTER);
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

        jTableScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Verwendete Dörfer"));
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
                fireViewChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jVillageTablePanel.add(jToggleButton1, gridBagConstraints);

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

    private void fireViewChangeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireViewChangeEvent
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
    }//GEN-LAST:event_fireViewChangeEvent

    private SupportTableModel getModel() {
        return TableHelper.getTableModel(jVillageTable);
    }

    protected void update() {
        overviewPanel.reset();
        DefenseInformation[] elements = DefenseAnalysePanel.getSingleton().getAllElements();
        List<Village> attackedVillages = new LinkedList<Village>();
        for (DefenseInformation element : elements) {
            attackedVillages.add(element.getTarget());
            overviewPanel.addVillage(element.getTarget(), Color.RED);
        }
        SupportTableModel model = getModel();
        List<Village> villages = new LinkedList<Village>();
        for (int i = 0; i < model.getRowCount(); i++) {
            villages.add(model.getRow(i).getVillage());
        }
        addVillages(villages.toArray(new Village[villages.size()]));
    }

    private void addVillages(Village[] pVillages) {
        SupportTableModel model = getModel();
        Hashtable<Village, Integer> supports = new Hashtable<Village, Integer>();
        for (Village village : pVillages) {
            supports.put(village, getSplits(village));
        }
        //remove used supports
        cleanSplits(supports);

        for (Village village : pVillages) {
            int supportSplits = supports.get(village);
            model.addRow(village, supportSplits);
            Color existing = overviewPanel.getColor(village);
            if (existing == null || !existing.equals(Color.RED)) {
                if (supportSplits == 0) {
                    overviewPanel.addVillage(village, Color.black);
                } else {
                    overviewPanel.addVillage(village, Color.yellow);
                }
            }
        }

        if (model.getRowCount() > 0) {
            controller.setProblem(null);
            model.fireTableDataChanged();
        }
        overviewPanel.repaint();
    }

    private int getSplits(Village pVillage) {
        /*  TroopSplit split = new TroopSplit(pVillage);
        split.update(DSWorkbenchSOSRequestAnalyzer.getSingleton().getDefense(), 10);
        return split.getSplitCount();*/
        return 10;
    }

    private void cleanSplits(Hashtable<Village, Integer> pSplits) {
        for (ManageableType t : SOSManager.getSingleton().getAllElements()) {
            SOSRequest r = (SOSRequest) t;
            Enumeration<Village> targets = r.getTargets();
            while (targets.hasMoreElements()) {
                DefenseInformation info = r.getDefenseInformation(targets.nextElement());
                for (Defense d : info.getSupports()) {
                    Integer split = pSplits.get(d.getSupporter());
                    if (split != null && split != 0) {
                        pSplits.put(d.getSupporter(), split - 1);
                    }
                }
            }
        }
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
                Village v = getModel().getRow(rows.get(i)).getVillage();
                overviewPanel.removeVillage(v);
                getModel().removeRow(rows.get(i));
            }
            if (getModel().getRowCount() == 0) {
                controller.setProblem("Keine Dörfer gewählt");
            }
            overviewPanel.repaint();
        }
    }

    public Village[] getUsedVillages() {
        List<Village> result = new LinkedList<Village>();
        SupportTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            result.add(model.getRow(i).getVillage());
        }
        return result.toArray(new Village[result.size()]);
    }

    public List<SupportSourceElement> getAllElements() {
        List<SupportSourceElement> elements = new LinkedList<SupportSourceElement>();
        SupportTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            elements.add(model.getRow(i));
        }
        return elements;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jDataPanel;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jTableScrollPane;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXTable jVillageTable;
    private javax.swing.JPanel jVillageTablePanel;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    private com.jidesoft.swing.JideSplitPane jideSplitPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (getModel().getRowCount() == 0) {
            controller.setProblem("Keine Dörfer gewählt");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        List<SupportSourceElement> result = new LinkedList<SupportSourceElement>();
        SupportTableModel model = getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            result.add(model.getRow(i));
        }
        DefenseFilterPanel.getSingleton().setup(result.toArray(new SupportSourceElement[result.size()]));
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

    public static class SupportSourceElement {

        private Village village = null;
        private int supports = 0;
        private boolean ignored = false;

        public SupportSourceElement(Village pVillage, int pSupports) {
            village = pVillage;
            supports = pSupports;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SupportSourceElement) {
                return ((SupportSourceElement) obj).getVillage().equals(getVillage());
            }
            return false;
        }

        public Village getVillage() {
            return village;
        }

        public int getSupports() {
            return supports;
        }

        public void setSupports(int pSupports) {
            supports = pSupports;
        }

        public boolean isIgnored() {
            return ignored;
        }

        public void setIgnored(boolean pValue) {
            ignored = pValue;
        }
    }
}

class SupportTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{
        "Spieler", "Dorf", "Einzelverteidigungen"
    };
    private Class[] types = new Class[]{
        Tribe.class, Village.class, Integer.class
    };
    private final List<SupportSourceElement> elements = new LinkedList<SupportSourceElement>();

    public SupportTableModel() {
        super();
    }

    public void clean() {
        elements.clear();
        fireTableDataChanged();
    }

    public void addRow(Village pVillage, int pSupports) {
        elements.add(new SupportSourceElement(pVillage, pSupports));
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

    public void removeRow(int row) {
        elements.remove(row);
        fireTableDataChanged();
    }

    public SupportSourceElement getRow(int row) {
        return elements.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (elements == null || elements.size() - 1 < row) {
            return null;
        }
        SupportSourceElement element = elements.get(row);
        switch (column) {
            case 0:
                return element.getVillage().getTribe();
            case 1:
                return element.getVillage();
            default:
                return element.getSupports();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
}