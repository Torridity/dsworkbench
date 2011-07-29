/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MarkerTableTab.java
 *
 * Created on Mar 27, 2011, 3:50:19 PM
 */
package de.tor.tribes.ui;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.TroopTableTab.TRANSFER_TYPE;
import de.tor.tribes.ui.decorator.GroupPredicate;
import de.tor.tribes.ui.models.SupportTroopsTableModel;
import de.tor.tribes.ui.renderer.NumberFormatCellRenderer;
import de.tor.tribes.ui.renderer.PercentCellRenderer;
import de.tor.tribes.ui.renderer.SupportTreeTableCellRenderer;
import de.tor.tribes.ui.renderer.SupportTroopTableHeaderRenderer;
import de.tor.tribes.ui.renderer.TroopAmountListCellRenderer;
import de.tor.tribes.ui.renderer.VisibilityCellRenderer;
import de.tor.tribes.ui.tree.IncomingTroopsUserObject;
import de.tor.tribes.ui.tree.OutgoingTroopsUserObject;
import de.tor.tribes.ui.views.DSWorkbenchTroopsFrame;
import de.tor.tribes.util.BrowserCommandSender;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.bb.TroopListFormatter;
import de.tor.tribes.util.troops.SupportVillageTroopsHolder;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * @author Torridity
 */
public class SupportTroopTableTab extends javax.swing.JPanel implements ListSelectionListener, TabInterface {

    private static Logger logger = Logger.getLogger("TroopTableTab");
    private String sTroopSet = null;
    private final static JXTreeTable jxTroopTable = new JXTreeTable();
    private static boolean KEY_LISTENER_ADDED = false;
    private PainterHighlighter highlighter = null;
    private ActionListener actionListener = null;
    private SupportRefillDialog mRefillDialog = null;

    static {
        jxTroopTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jxTroopTable.setColumnControlVisible(true);
        jxTroopTable.setDefaultRenderer(Float.class, new PercentCellRenderer());
        jxTroopTable.setDefaultRenderer(Boolean.class, new VisibilityCellRenderer());
        jxTroopTable.setDefaultRenderer(Number.class, new NumberFormatCellRenderer());
        jxTroopTable.setTreeCellRenderer(new SupportTreeTableCellRenderer());
    }

    /** Creates new form TroopTableTab
     * @param pTroopSet
     * @param pActionListener
     */
    public SupportTroopTableTab(final ActionListener pActionListener) {
        actionListener = pActionListener;
        sTroopSet = TroopsManager.SUPPORT_GROUP;
        initComponents();
        mRefillDialog = new SupportRefillDialog(DSWorkbenchTroopsFrame.getSingleton(), true);
        jScrollPane1.setViewportView(jxTroopTable);
        if (!KEY_LISTENER_ADDED) {
            KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
            KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
            jxTroopTable.registerKeyboardAction(pActionListener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxTroopTable.registerKeyboardAction(pActionListener, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxTroopTable.getActionMap().put("find", new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    //no find for support tab
                    JOptionPaneHelper.showInformationBox(jxTroopTable, "Die Suchfunktion ist für die Unterstützungsansicht nicht verfügbar", "Information");
                }
            });

            KEY_LISTENER_ADDED = true;
        }
        jxTroopTable.getSelectionModel().addListSelectionListener(SupportTroopTableTab.this);
        jTroopAmountList.setCellRenderer(new TroopAmountListCellRenderer());
    }

    @Override
    public void deregister() {
        jxTroopTable.getSelectionModel().removeListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {

            //int selectionCount = jxTroopTable.getSelectedRowCount();
            int selectionCount = getSelectedVillages().size();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Dorf gewählt" : " Dörfer gewählt"));
            }
        } else {
            actionListener.actionPerformed(new ActionEvent(jxTroopTable, 0, "SelectionDone"));
        }
    }

    public void updateSelectionInfo() {
        List<VillageTroopsHolder> selection = getSelectedVillages();
        HashMap<UnitHolder, Integer> amounts = new HashMap<UnitHolder, Integer>();
        //initialize map
        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
            amounts.put(u, 0);
        }
        //fill map
        for (VillageTroopsHolder holder : selection) {
            for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                amounts.put(u, amounts.get(u) + holder.getTroopsOfUnitInVillage(u));
            }
        }
        //fill list
        DefaultListModel model = new DefaultListModel();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
            model.addElement(nf.format(amounts.get(u)) + " " + u.getPlainName());
        }

        jTroopAmountList.setModel(model);
        jTroopAmountList.repaint();
    }

    public void refillSupports() {
        List<VillageTroopsHolder> selection = getSelectedVillages();
        if (selection.isEmpty()) {
            showInfo("Keine Dörfer ausgewählt");
            return;
        }

        mRefillDialog.pack();
        mRefillDialog.setupAndShow(selection);
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

    public String getTroopSet() {
        return sTroopSet;
    }

    public JXTable getTroopTable() {
        return jxTroopTable;
    }

    public void updateSet() {
        System.out.println("Update");
        jxTroopTable.setTreeTableModel(new SupportTroopsTableModel(buildTreeTableData()));
        
        jScrollPane1.setViewportView(jxTroopTable);
        jxTroopTable.getTableHeader().setDefaultRenderer(new SupportTroopTableHeaderRenderer());
    }

    public DefaultMutableTreeNode buildTreeTableData() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        List<ManageableType> elems = TroopsManager.getSingleton().getAllElements(TroopsManager.SUPPORT_GROUP);
        for (ManageableType elem : elems) {
            SupportVillageTroopsHolder s = (SupportVillageTroopsHolder) elem;
            DefaultMutableTreeNode villageNode = new DefaultMutableTreeNode(s);
            Hashtable<Village, Hashtable<UnitHolder, Integer>> incElems = s.getIncomingSupports();
            Enumeration<Village> villageKeys = incElems.keys();
            while (villageKeys.hasMoreElements()) {
                Village villageKey = villageKeys.nextElement();
                villageNode.add(new DefaultMutableTreeNode(new IncomingTroopsUserObject(s.getVillage(), villageKey, incElems.get(villageKey))));
            }
            Hashtable<Village, Hashtable<UnitHolder, Integer>> outElems = s.getOutgoingSupports();
            villageKeys = outElems.keys();
            while (villageKeys.hasMoreElements()) {
                Village villageKey = villageKeys.nextElement();
                villageNode.add(new DefaultMutableTreeNode(new OutgoingTroopsUserObject(s.getVillage(), villageKey, outElems.get(villageKey))));
            }
            root.add(villageNode);
        }
        return root;

    }

    public void updateFilter(final List<Tag> groups, final boolean pRelation, final boolean pFilterRows) {
        if (highlighter != null) {
            jxTroopTable.removeHighlighter(highlighter);
        }
        if (!pFilterRows) {
            jxTroopTable.setRowFilter(null);
            GroupPredicate groupPredicate = new GroupPredicate(groups, 0, pRelation, getTroopSet());
            MattePainter mp = new MattePainter(new Color(0, 0, 0, 120));
            highlighter = new PainterHighlighter(new HighlightPredicate.NotHighlightPredicate(groupPredicate), mp);
            jxTroopTable.addHighlighter(highlighter);
        } else {
            jxTroopTable.setRowFilter(new RowFilter<TableModel, Integer>() {

                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                    Integer row = entry.getIdentifier();
                    VillageTroopsHolder h = (VillageTroopsHolder) TroopsManager.getSingleton().getAllElements().get(row);
                    Village v = h.getVillage();
                    //true == 
                    //false ||
                    boolean result = false;
                    if (pRelation) {
                        //and connection
                        boolean failure = false;
                        for (Tag t : groups) {
                            if (!t.tagsVillage(v.getId())) {
                                failure = true;
                                break;
                            }
                        }
                        if (!failure) {
                            result = true;
                        }
                    } else {
                        //or connection
                        for (Tag t : groups) {
                            if (t.tagsVillage(v.getId())) {
                                result = true;
                                break;
                            }
                        }
                    }
                    return result;
                }
            });
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTroopAmountList = new javax.swing.JList();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setForeground(new java.awt.Color(240, 240, 240));
        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setMinimumSize(new java.awt.Dimension(120, 130));
        jPanel1.setPreferredSize(new java.awt.Dimension(120, 130));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Auswahl"));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(268, 130));

        jTroopAmountList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(jTroopAmountList);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.WEST);

        infoPanel.setCollapsed(true);
        infoPanel.setInheritAlpha(false);

        jXLabel1.setText("Keine Meldung");
        jXLabel1.setOpaque(true);
        jXLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        infoPanel.add(jXLabel1, java.awt.BorderLayout.CENTER);

        add(infoPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        infoPanel.setCollapsed(true);
    }//GEN-LAST:event_fireHideInfoEvent
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXCollapsiblePane infoPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList jTroopAmountList;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    // End of variables declaration//GEN-END:variables

    public void transferSelection(TRANSFER_TYPE pType) {
        switch (pType) {
            case CLIPBOARD_PLAIN:
                break;
            case CLIPBOARD_BB:
                copyBBToExternalClipboardEvent();
                break;
        }
    }

    private void copyBBToExternalClipboardEvent() {
        try {
            List<VillageTroopsHolder> troops = getSelectedVillages();

            if (troops.isEmpty()) {
                showInfo("Keine Dörfer ausgewählt");
                return;
            }
            boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

            StringBuilder buffer = new StringBuilder();
            if (extended) {
                buffer.append("[u][size=12]Truppenübersicht[/size][/u]\n\n");
            } else {
                buffer.append("[u]Truppenübersicht[/u]\n\n");
            }

            buffer.append("Herkunft der Daten: '").append(getTroopSet()).append("'\n\n");
            buffer.append(new TroopListFormatter().formatElements(troops, extended));
            if (extended) {
                buffer.append("\n[size=8]Erstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit [url=\"http://www.dsworkbench.de/index.php?id=23\"]DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/url][/size]\n");
            } else {
                buffer.append("\nErstellt am ");
                buffer.append(new SimpleDateFormat("dd.MM.yy 'um' HH:mm:ss").format(Calendar.getInstance().getTime()));
                buffer.append(" mit [url=\"http://www.dsworkbench.de/index.php?id=23\"]DS Workbench ");
                buffer.append(Constants.VERSION).append(Constants.VERSION_ADDITION + "[/url]\n");
            }

            String b = buffer.toString();
            StringTokenizer t = new StringTokenizer(b, "[");
            int cnt = t.countTokens();
            if (cnt > 1000) {
                if (JOptionPaneHelper.showQuestionConfirmBox(this, "Die ausgewählten Angriffe benötigen mehr als 1000 BB-Codes\n" + "und können daher im Spiel (Forum/IGM/Notizen) nicht auf einmal dargestellt werden.\nTrotzdem exportieren?", "Zu viele BB-Codes", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b), null);
            String result = "Daten in Zwischenablage kopiert.";
            showSuccess(result);
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            //JOptionPaneHelper.showErrorBox(this, result, "Fehler");
            showError(result);
        }
    }

    public void centerVillage() {
       // List<VillageTroopsHolder> selection = getSelectedVillages();
          Village selection = getSelectedVillage();
        //if (selection.isEmpty()) {
            if (selection == null) {
            showInfo("Kein Dorf ausgewählt");
            return;
        }
        //DSWorkbenchMainFrame.getSingleton().centerVillage(selection.get(0).getVillage());
        DSWorkbenchMainFrame.getSingleton().centerVillage(selection);
    }

    public void centerVillageInGame() {
       // List<VillageTroopsHolder> selection = getSelectedVillages();
         Village selection = getSelectedVillage();
       //if (selection.isEmpty()) {
         if (selection == null) {
            showInfo("Kein Dorf ausgewählt");
            return;
        }

      //  BrowserCommandSender.centerVillage(selection.get(0).getVillage());
         BrowserCommandSender.centerVillage(selection);
    }

    public void openPlaceInGame() {
        //List<VillageTroopsHolder> selection = getSelectedVillages();
        Village selection = getSelectedVillage();
        //if (selection.isEmpty()) {
        if (selection == null) {
            showInfo("Kein Dorf ausgewählt");
            return;
        }

        //BrowserCommandSender.openPlaceTroopsView(selection.get(0).getVillage());
        BrowserCommandSender.openPlaceTroopsView(selection);
    }

    public boolean deleteSelection(boolean pAsk) {
        List<VillageTroopsHolder> selectedVillages = getSelectedVillages();

        if (selectedVillages.isEmpty()) {
            showInfo("Kein Dorf ausgewählt");
            return true;
        }

        if (pAsk) {
            String message = "Alle Unterstützungen aus den gewählten Dörfern löschen?";
            int result = JOptionPaneHelper.showQuestionConfirmBox(this, message, "Truppeninformationen löschen", "Nein", "Ja");
            if (result == JOptionPane.YES_OPTION) {
                //remove only from current view
                TroopsManager.getSingleton().invalidate();
                for (VillageTroopsHolder holder : selectedVillages) {
                    TroopsManager.getSingleton().removeElement(getTroopSet(), holder);
                }
                TroopsManager.getSingleton().revalidate(getTroopSet(), true);
                return true;
            } else {
                //remove nothing
                return true;
            }
        }

        showSuccess(selectedVillages.size() + " Truppeninformation(en) gelöscht");
        return true;
    }

    @Override
    public void deleteSelection() {
        deleteSelection(true);
    }

    private List<VillageTroopsHolder> getSelectedVillages() {
        final List<VillageTroopsHolder> selectedVillages = new LinkedList<VillageTroopsHolder>();
        int[] selectedRows = jxTroopTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length < 1) {
            return selectedVillages;
        }

        for (Integer selectedRow : selectedRows) {
            TreePath p = jxTroopTable.getPathForRow(selectedRow);
            if (p.getPathCount() >= 2) {
                VillageTroopsHolder h = (VillageTroopsHolder) ((DefaultMutableTreeNode) p.getPath()[1]).getUserObject();
                if (!selectedVillages.contains(h)) {
                    selectedVillages.add(h);
                }
            }
        }

        return selectedVillages;
    }

    private Village getSelectedVillage() {
        int selectedRow = jxTroopTable.getSelectedRow();
        if (selectedRow == 0) {
            return null;
        }
        TreePath p = jxTroopTable.getPathForRow(selectedRow);
        Object o = p.getLastPathComponent();
        Object uo = ((DefaultMutableTreeNode) o).getUserObject();
        if (uo instanceof VillageTroopsHolder) {
            return ((VillageTroopsHolder) uo).getVillage();
        } else if (uo instanceof IncomingTroopsUserObject) {
            return ((IncomingTroopsUserObject) uo).getTroopsHolder().getVillage();
        } else if (uo instanceof OutgoingTroopsUserObject) {
            return ((OutgoingTroopsUserObject) uo).getTroopsHolder().getVillage();
        }
        return null;

    }
}
