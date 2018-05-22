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
package de.tor.tribes.ui.panels;

import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.SupportDetailsDialog;
import de.tor.tribes.ui.decorator.GroupPredicate;
import de.tor.tribes.ui.models.TroopsTableModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.NumberFormatCellRenderer;
import de.tor.tribes.ui.renderer.PercentCellRenderer;
import de.tor.tribes.ui.renderer.TroopTableHeaderRenderer;
import de.tor.tribes.ui.views.DSWorkbenchTroopsFrame;
import de.tor.tribes.ui.windows.TroopDetailsDialog;
import de.tor.tribes.util.BrowserInterface;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.bb.TroopListFormatter;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.HorizontalAlignment;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.VerticalAlignment;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.painter.MattePainter;

/**
 *
 * @author Torridity
 */
public class TroopTableTab extends javax.swing.JPanel implements ListSelectionListener {

    private static Logger logger = Logger.getLogger("TroopTableTab");

    public enum TRANSFER_TYPE {

        CLIPBOARD_PLAIN, CLIPBOARD_BB, CUT_TO_INTERNAL_CLIPBOARD, COPY_TO_INTERNAL_CLIPBOARD, FROM_INTERNAL_CLIPBOARD
    }
    private String sTroopSet = null;
    private final static JXTable jxTroopTable = new JXTable();
    private static TroopsTableModel troopModel = null;
    private static boolean KEY_LISTENER_ADDED = false;
    private PainterHighlighter highlighter = null;
    private ActionListener actionListener = null;
    private SupportDetailsDialog mSupportDetailsDialog = null;
    private TroopDetailsDialog mTroopDetailsDialog = null;

    static {
        jxTroopTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jxTroopTable.setColumnControlVisible(true);
        jxTroopTable.setDefaultRenderer(Float.class, new PercentCellRenderer());
        jxTroopTable.setDefaultRenderer(Integer.class, new NumberFormatCellRenderer());
        jxTroopTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        troopModel = new TroopsTableModel(TroopsManager.getSingleton().getDefaultGroupName());
        jxTroopTable.setModel(troopModel);
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
        jxTroopTable.addHighlighter(new PainterHighlighter(HighlightPredicate.EDITABLE, new ImagePainter(back, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)));
    }

    /**
     * Creates new form TroopTableTab
     *
     * @param pTroopSet
     * @param pActionListener
     */
    public TroopTableTab(String pTroopSet, final ActionListener pActionListener) {
        actionListener = pActionListener;
        sTroopSet = pTroopSet;
        initComponents();
        jScrollPane1.setViewportView(jxTroopTable);
        mSupportDetailsDialog = new SupportDetailsDialog(DSWorkbenchTroopsFrame.getSingleton(), true);
        mTroopDetailsDialog = new TroopDetailsDialog(DSWorkbenchTroopsFrame.getSingleton(), true);
        if (!KEY_LISTENER_ADDED) {
            KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
            KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
            jxTroopTable.registerKeyboardAction(pActionListener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxTroopTable.registerKeyboardAction(pActionListener, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxTroopTable.getActionMap().put("find", new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    pActionListener.actionPerformed(new ActionEvent(jxTroopTable, 0, "Find"));
                }
            });

            KEY_LISTENER_ADDED = true;
        }
        jxTroopTable.getSelectionModel().addListSelectionListener(TroopTableTab.this);
        //   jTroopAmountList.setCellRenderer(new TroopAmountListCellRenderer());
    }

    public void deregister() {
        jxTroopTable.getSelectionModel().removeListSelectionListener(this);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jxTroopTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Dorf gewählt" : " Dörfer gewählt"));
            }
        } else {
            actionListener.actionPerformed(new ActionEvent(jxTroopTable, 0, "SelectionDone"));
        }
    }

    public void updateSelectionInfo() {
        /*
         * List<VillageTroopsHolder> selection = getSelectedTroopHolders(); HashMap<UnitHolder, Integer> amounts = new HashMap<UnitHolder,
         * Integer>(); //initialize map for (UnitHolder u : DataHolder.getSingleton().getUnits()) { amounts.put(u, 0); } //fill map for
         * (VillageTroopsHolder holder : selection) { for (UnitHolder u : DataHolder.getSingleton().getUnits()) { amounts.put(u,
         * amounts.get(u) + holder.getTroopsOfUnitInVillage(u)); } } //fill list DefaultListModel model = new DefaultListModel();
         * NumberFormat nf = NumberFormat.getInstance(); nf.setMinimumFractionDigits(0); nf.setMaximumFractionDigits(0); for (UnitHolder u :
         * DataHolder.getSingleton().getUnits()) { model.addElement(nf.format(amounts.get(u)) + " " + u.getPlainName()); }
         *
         * jTroopAmountList.setModel(model); jTroopAmountList.repaint();
         */
    }

    public void showSelectionDetails() {
        List<Village> selection = getSelectedVillages();
        if (selection.isEmpty()) {
            showInfo("Keine Dörfer ausgewählt");
            return;
        }
        if (sTroopSet == null || !sTroopSet.equals(TroopsManager.SUPPORT_GROUP)) {
            TroopDetailsDialog details = new TroopDetailsDialog(DSWorkbenchTroopsFrame.getSingleton(), false);
            details.setupAndShow(getSelectedTroopHolders());
        } else {
            SupportDetailsDialog details = new SupportDetailsDialog(DSWorkbenchTroopsFrame.getSingleton(), false);
            details.setupAndShow(selection);
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

    public String getTroopSet() {
        return sTroopSet;
    }

    public JXTable getTroopTable() {
        return jxTroopTable;
    }

    public void updateSet() {
        troopModel.setTroopSet(sTroopSet);
        jScrollPane1.setViewportView(jxTroopTable);
        jxTroopTable.getTableHeader().setDefaultRenderer(new TroopTableHeaderRenderer());
    }

    public void updateFilter(final List<Tag> groups, final boolean pRelation, final boolean pFilterRows) {
        if (highlighter != null) {
            jxTroopTable.removeHighlighter(highlighter);
        }
        if (!pFilterRows) {
            jxTroopTable.setRowFilter(null);
            GroupPredicate groupPredicate = new GroupPredicate(groups, 0, pRelation, sTroopSet);
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

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTroopAmountList = new javax.swing.JList();
        jScrollPane1 = new javax.swing.JScrollPane();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();

        jPanel1.setMinimumSize(new java.awt.Dimension(120, 130));
        jPanel1.setPreferredSize(new java.awt.Dimension(120, 130));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Auswahl"));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(268, 130));

        jTroopAmountList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(jTroopAmountList);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        setBackground(new java.awt.Color(255, 255, 255));
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setForeground(new java.awt.Color(240, 240, 240));
        add(jScrollPane1, java.awt.BorderLayout.CENTER);

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
            List<VillageTroopsHolder> troops = getSelectedTroopHolders();
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

            buffer.append("Herkunft der Daten: '").append(sTroopSet).append("'\n\n");

            buffer.append(new TroopListFormatter().formatElements(troops, extended));

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
        List<VillageTroopsHolder> selection = getSelectedTroopHolders();
        if (selection.isEmpty()) {
            showInfo("Kein Dorf ausgewählt");
            return;
        }
        DSWorkbenchMainFrame.getSingleton().centerVillage(selection.get(0).getVillage());
    }

    public void centerVillageInGame() {
        List<VillageTroopsHolder> selection = getSelectedTroopHolders();
        if (selection.isEmpty()) {
            showInfo("Kein Dorf ausgewählt");
            return;
        }

        BrowserInterface.centerVillage(selection.get(0).getVillage());
    }

    public void openPlaceInGame() {
        List<VillageTroopsHolder> selection = getSelectedTroopHolders();
        if (selection.isEmpty()) {
            showInfo("Kein Dorf ausgewählt");
            return;
        }

        BrowserInterface.openPlaceTroopsView(selection.get(0).getVillage());
    }

    public boolean deleteSelection(boolean pAsk) {
        List<VillageTroopsHolder> selectedVillages = getSelectedTroopHolders();

        if (selectedVillages.isEmpty()) {
            showInfo("Kein Dorf ausgewählt");
            return true;
        }

        if (pAsk) {
            String message = ((selectedVillages.size() == 1) ? "Truppeninformation " : (selectedVillages.size() + " Truppeninformationen ")) + "sind zum Löschen gewählt.\nAus welcher Kategorie sollen die Daten gelöscht werden?";
            int result = JOptionPaneHelper.showQuestionThreeChoicesBox(this, message, "Truppeninformationen löschen", "Nur '" + sTroopSet + "'", "Keine", "Alle");
            if (result == JOptionPane.NO_OPTION) {
                //remove only from current view
                TroopsManager.getSingleton().invalidate();
                for (VillageTroopsHolder holder : selectedVillages) {
                    TroopsManager.getSingleton().removeElement(sTroopSet, holder);
                }
                TroopsManager.getSingleton().revalidate(sTroopSet, true);
                return true;
            } else if (result == JOptionPane.CANCEL_OPTION) {
                //remove all entries
                TroopsManager.getSingleton().invalidate();
                for (VillageTroopsHolder holder : selectedVillages) {
                    for (String group : TroopsManager.getSingleton().getGroups()) {
                        TroopsManager.getSingleton().removeElement(group, holder);
                    }
                }
                TroopsManager.getSingleton().revalidate(true);
                return true;
            } else {
                //remove nothing
                return true;
            }
        }

        showSuccess(selectedVillages.size() + " Truppeninformation(en) gelöscht");
        return true;
    }

    public void deleteSelection() {
        deleteSelection(true);
    }

    private List<VillageTroopsHolder> getSelectedTroopHolders() {
        final List<VillageTroopsHolder> selectedHolders = new LinkedList<>();
        int[] selectedRows = jxTroopTable.getSelectedRows();
        if (selectedRows != null && selectedRows.length < 1) {
            return selectedHolders;
        }
        for (Integer selectedRow : selectedRows) {
            VillageTroopsHolder t = (VillageTroopsHolder) TroopsManager.getSingleton().getAllElements(sTroopSet).get(jxTroopTable.convertRowIndexToModel(selectedRow));
            if (t != null) {
                selectedHolders.add(t);
            }
        }
        return selectedHolders;
    }

    public List<Village> getSelectedVillages() {
        List<VillageTroopsHolder> holders = getSelectedTroopHolders();

        List<Village> villages = new LinkedList<>();
        for (VillageTroopsHolder holder : holders) {
            villages.add(holder.getVillage());
        }
        return villages;
    }
}
