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

import de.tor.tribes.dssim.ui.DSWorkbenchSimulatorFrame;
import de.tor.tribes.dssim.util.AStarResultReceiver;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.models.ReportManagerTableModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.FightReportCellRenderer;
import de.tor.tribes.ui.renderer.NoteIconCellRenderer;
import de.tor.tribes.ui.renderer.ReportWallCataCellRenderer;
import de.tor.tribes.ui.renderer.TribeCellRenderer;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.ui.renderer.VillageCellRenderer;
import de.tor.tribes.ui.views.DSWorkbenchReportFrame;
import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import de.tor.tribes.ui.windows.ReportShowDialog;
import de.tor.tribes.ui.wiz.tap.AttackTargetPanel;
import de.tor.tribes.ui.wiz.tap.TacticsPlanerWizard;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.bb.ReportListFormatter;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.decorator.PatternPredicate;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.HorizontalAlignment;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.VerticalAlignment;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author Torridity
 */
public class ReportTableTab extends javax.swing.JPanel implements ListSelectionListener, AStarResultReceiver {

    @Override
    public void fireNotifyOnResultEvent(Point point, int pAttacks) {
        if (point == null) {
            showError("Die Zielkoordinate ist ungültig");
            return;
        }
        Village v = DataHolder.getSingleton().getVillages()[point.x][point.y];
        if (v != null) {
            List<Village> villages = new ArrayList<>();
            for (int i = 0; i < pAttacks; i++) {
                villages.add(v);
            }
            AttackTargetPanel.getSingleton().addVillages(villages.toArray(new Village[pAttacks]));
            TacticsPlanerWizard.show();
        } else {
            showError("Das Zieldorf ist ungültig");
        }
    }
    private static Logger logger = Logger.getLogger("ReportTableTab");

    public enum TRANSFER_TYPE {

        CLIPBOARD_BB, ASTAR, CUT_TO_INTERNAL_CLIPBOARD, COPY_TO_INTERNAL_CLIPBOARD, FROM_INTERNAL_CLIPBOARD
    }
    private String sReportSet = null;
    private final static JXTable jxReportTable = new JXTable();
    private static ReportManagerTableModel reportModel = null;
    private static boolean KEY_LISTENER_ADDED = false;
    private static PainterHighlighter highlighter = null;
    private Hashtable<Integer, List<ReportShowDialog>> showDialogs = null;

    static {
        jxReportTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jxReportTable.setColumnControlVisible(true);
        jxReportTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jxReportTable.setDefaultRenderer(Integer.class, new NoteIconCellRenderer(NoteIconCellRenderer.ICON_TYPE.NOTE));
        jxReportTable.setDefaultRenderer(Date.class, new DateCellRenderer("dd.MM.yy HH:mm"));
        jxReportTable.setDefaultRenderer(Tribe.class, new TribeCellRenderer());
        jxReportTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
        reportModel = new ReportManagerTableModel(ReportManager.DEFAULT_GROUP);
        jxReportTable.setModel(reportModel);
        TableColumnExt statCol = jxReportTable.getColumnExt("Status");
        statCol.setCellRenderer(new FightReportCellRenderer());
        TableColumnExt miscCol = jxReportTable.getColumnExt("Sonstiges");
        miscCol.setCellRenderer(new ReportWallCataCellRenderer());
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
        jxReportTable.addHighlighter(new PainterHighlighter(HighlightPredicate.EDITABLE, new ImagePainter(back, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)));

    }

    /**
     * Creates new form ReportTablePanel
     *
     * @param pReportSet
     * @param pActionListener
     */
    public ReportTableTab(String pReportSet, final ActionListener pActionListener) {
        sReportSet = pReportSet;
        initComponents();
        jScrollPane1.setViewportView(jxReportTable);
        if (!KEY_LISTENER_ADDED) {
            KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
            KeyStroke bbCopy = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK, false);
            KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
            KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false);
            KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
            jxReportTable.registerKeyboardAction(pActionListener, "Copy", copy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxReportTable.registerKeyboardAction(pActionListener, "BBCopy", bbCopy, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxReportTable.registerKeyboardAction(pActionListener, "Cut", cut, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxReportTable.registerKeyboardAction(pActionListener, "Paste", paste, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxReportTable.registerKeyboardAction(pActionListener, "Delete", delete, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            jxReportTable.getActionMap().put("find", new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    pActionListener.actionPerformed(new ActionEvent(jxReportTable, 0, "Find"));
                }
            });

            KEY_LISTENER_ADDED = true;
        }
        showDialogs = new Hashtable<>();
        jxReportTable.getSelectionModel().addListSelectionListener(ReportTableTab.this);
    }

    public void deregister() {
        closeAllReportDialogs();
        jxReportTable.getSelectionModel().removeListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jxReportTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Bericht gewählt" : " Berichte gewählt"));
            }
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

    public String getReportSet() {
        return sReportSet;
    }

    public void viewReport() {
        List<FightReport> selection = getSelectedReports();
        if (selection.isEmpty()) {
            showInfo("Kein Bericht gewählt");
            return;
        }

        int x = 0;
        int y = 0;
        int layer = 0;
        boolean max = false;
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        for (FightReport report : selection) {
            ReportShowDialog dialog = new ReportShowDialog(DSWorkbenchReportFrame.getSingleton(), false);
            dialog.setLocation(new Point(x, y));
            dialog.setParentTab(this, layer);
            dialog.setupAndShow(report);
            List<ReportShowDialog> dialogsInLayer = showDialogs.get(layer);
            if (dialogsInLayer == null) {
                dialogsInLayer = new LinkedList<>();
                showDialogs.put(layer, dialogsInLayer);
            }
            dialogsInLayer.add(dialog);

            if (!max) {
                x += dialog.getWidth();
                if (x > size.getWidth() - dialog.getWidth() / 2) {
                    x = 0;
                    y += dialog.getHeight();
                }

                if (y > size.height - dialog.getHeight() / 2) {
                    x = 0;
                    y = 0;
                    layer++;
                }
            }
        }
    }

    public void closeReportLayer(int pLayer) {
        List<ReportShowDialog> dialogsInLayer = showDialogs.get(pLayer);
        for (ReportShowDialog d : dialogsInLayer) {
            d.dispose();
        }
        showDialogs.remove(pLayer);
    }

    public void closeReportDialog(ReportShowDialog pDialog) {
        List<ReportShowDialog> dialogsInLayer = showDialogs.get(pDialog.getLayer());
        dialogsInLayer.remove(pDialog);
        pDialog.dispose();
    }

    public void closeAllReportDialogs() {
        Enumeration<Integer> keys = showDialogs.keys();
        while (keys.hasMoreElements()) {
            Integer key = keys.nextElement();
            closeReportLayer(key);
        }
    }

    public JXTable getReportTable() {
        return jxReportTable;
    }

    public void updateSet() {
        reportModel.setReportSet(sReportSet);
        String[] cols = new String[]{"Status", "Typ", "Sonstiges"};
        for (String col : cols) {
            TableColumnExt columns = jxReportTable.getColumnExt(col);
            columns.setPreferredWidth(80);
            columns.setMaxWidth(80);
            columns.setWidth(80);
        }
        jScrollPane1.setViewportView(jxReportTable);
        jxReportTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
    }

    public void updateFilter(final String pValue, final List<String> columns, final boolean pCaseSensitive, final boolean pFilterRows) {
        if (highlighter != null) {
            jxReportTable.removeHighlighter(highlighter);
        }
        if (!pFilterRows) {
            jxReportTable.setRowFilter(null);
            final List<Integer> relevantCols = new LinkedList<>();
            List<TableColumn> cols = jxReportTable.getColumns(true);
            for (int i = 0; i < jxReportTable.getColumnCount(); i++) {
                TableColumnExt col = jxReportTable.getColumnExt(i);
                if (col.isVisible() && columns.contains(col.getTitle())) {
                    relevantCols.add(cols.indexOf(col));
                }
            }
            for (Integer col : relevantCols) {
                PatternPredicate patternPredicate0 = new PatternPredicate((pCaseSensitive ? "" : "(?i)") + Matcher.quoteReplacement(pValue), col);
                MattePainter mp = new MattePainter(new Color(0, 0, 0, 120));
                highlighter = new PainterHighlighter(new HighlightPredicate.NotHighlightPredicate(patternPredicate0), mp);
                jxReportTable.addHighlighter(highlighter);
            }
        } else {
            jxReportTable.setRowFilter(new RowFilter<TableModel, Integer>() {

                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                    final List<Integer> relevantCols = new LinkedList<>();
                    List<TableColumn> cols = jxReportTable.getColumns(true);
                    for (int i = 0; i < jxReportTable.getColumnCount(); i++) {
                        TableColumnExt col = jxReportTable.getColumnExt(i);
                        if (col.isVisible() && columns.contains(col.getTitle())) {
                            relevantCols.add(cols.indexOf(col));
                        }
                    }

                    for (Integer col : relevantCols) {
                        if (pCaseSensitive) {
                            if (entry.getStringValue(col).contains(pValue)) {
                                return true;
                            }
                        } else {
                            if (entry.getStringValue(col).toLowerCase().contains(pValue.toLowerCase())) {
                                return true;
                            }
                        }
                    }
                    return false;
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

        jScrollPane1 = new javax.swing.JScrollPane();
        infoPanel = new org.jdesktop.swingx.JXCollapsiblePane();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();

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
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    // End of variables declaration//GEN-END:variables

    public void transferTroopInfos() {
        List<FightReport> selectedReports = getSelectedReports();
        if (selectedReports.isEmpty()) {
            showInfo("Kein Bericht ausgewählt");
            return;
        }

        int res = JOptionPaneHelper.showQuestionThreeChoicesBox(DSWorkbenchReportFrame.getSingleton(), "<html>Welche Truppeninformationen m&ouml;chtest du &uuml;bertragen?<br/><b>Achtung:</b> Alle vorhandene Truppeninformationen des Dorfes werden &uuml;berschrieben.</html>", "Übertragen", "Angreifer", "Verteidiger", "Beide");
        if (res == JOptionPane.NO_OPTION) {
            //attacker
            TroopsManager.getSingleton().invalidate();
            for (FightReport report : selectedReports) {
                Village source = report.getSourceVillage();
                Hashtable<UnitHolder, Integer> attackers = report.getSurvivingAttackers();
                VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(source, TroopsManager.TROOP_TYPE.IN_VILLAGE, true);
                holder.setTroops(attackers);
                holder = TroopsManager.getSingleton().getTroopsForVillage(source, TroopsManager.TROOP_TYPE.OWN, true);
                holder.setTroops(attackers);
                TroopsManager.getSingleton().revalidate(TroopsManager.OWN_GROUP, true);
            }
            TroopsManager.getSingleton().revalidate(TroopsManager.IN_VILLAGE_GROUP, true);
        } else if (res == JOptionPane.YES_OPTION) {
            //defender
            TroopsManager.getSingleton().invalidate();
            for (FightReport report : selectedReports) {
                Village target = report.getTargetVillage();
                Hashtable<UnitHolder, Integer> defenders = report.getSurvivingDefenders();
                VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(target, TroopsManager.TROOP_TYPE.IN_VILLAGE, true);
                holder.setTroops(defenders);
            }
            TroopsManager.getSingleton().revalidate(TroopsManager.IN_VILLAGE_GROUP, true);
        } else if (res == JOptionPane.CANCEL_OPTION) {
            //both
            TroopsManager.getSingleton().invalidate();
            for (FightReport report : selectedReports) {
                Village source = report.getSourceVillage();
                Hashtable<UnitHolder, Integer> attackers = report.getSurvivingAttackers();
                VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(source, TroopsManager.TROOP_TYPE.IN_VILLAGE, true);
                holder.setTroops(attackers);
                holder = TroopsManager.getSingleton().getTroopsForVillage(source, TroopsManager.TROOP_TYPE.OWN, true);
                holder.setTroops(attackers);
                TroopsManager.getSingleton().revalidate(TroopsManager.OWN_GROUP, true);
            }
            for (FightReport report : selectedReports) {
                Village target = report.getTargetVillage();
                Hashtable<UnitHolder, Integer> defenders = report.getSurvivingDefenders();
                VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(target, TroopsManager.TROOP_TYPE.IN_VILLAGE, true);
                holder.setTroops(defenders);
            }
            TroopsManager.getSingleton().revalidate(TroopsManager.IN_VILLAGE_GROUP, true);

        } else {
            return;
        }
        showSuccess("Truppeninformationen übertragen");
    }

    public boolean deleteSelection(boolean pAsk) {
        List<FightReport> selectedReports = getSelectedReports();

        if (pAsk) {
            String message = ((selectedReports.size() == 1) ? "Bericht " : (selectedReports.size() + " Berichte ")) + "wirklich löschen?";
            if (selectedReports.isEmpty() || JOptionPaneHelper.showQuestionConfirmBox(this, message, "Berichte löschen", "Nein", "Ja") != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        jxReportTable.editingCanceled(new ChangeEvent(this));
        ReportManager.getSingleton().removeElements(sReportSet, selectedReports);
        reportModel.fireTableDataChanged();
        showSuccess(selectedReports.size() + " Bericht(e) gelöscht");
        return true;
    }

    public void deleteSelection() {
        deleteSelection(true);
    }

    public void transferSelection(TRANSFER_TYPE pType) {
        switch (pType) {
            case COPY_TO_INTERNAL_CLIPBOARD:
                copyToInternalClipboard();
                break;
            case CUT_TO_INTERNAL_CLIPBOARD:
                cutToInternalClipboard();
                break;
            case FROM_INTERNAL_CLIPBOARD:
                pasteFromInternalClipboard();
                break;
            case CLIPBOARD_BB:
                copyBBToExternalClipboardEvent();
                break;
            case ASTAR:
                transferReportToAStar();
                break;
        }
    }

    private void transferReportToAStar() {
        List<FightReport> selection = getSelectedReports();
        if (selection.isEmpty()) {
            showInfo("Kein Bericht ausgewählt");
            return;
        }

        FightReport report = selection.get(0);
        Hashtable<String, Double> values = new Hashtable<>();
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (!report.areAttackersHidden()) {
                values.put("att_" + unit.getPlainName(), (double) report.getAttackers().get(unit));
            }
            if (!report.wasLostEverything()) {
                values.put("def_" + unit.getPlainName(), (double) report.getDefenders().get(unit));
            }
        }
        if (report.wasBuildingDamaged()) {
            values.put("building", (double) report.getBuildingBefore());
        }
        if (report.wasWallDamaged()) {
            values.put("wall", (double) report.getWallBefore());
        }
        values.put("luck", report.getLuck());
        values.put("moral", report.getMoral());
        if (!GlobalOptions.isOfflineMode()) {
            if (!DSWorkbenchSimulatorFrame.getSingleton().isVisible()) {
                DSWorkbenchSimulatorFrame.getSingleton().setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                DSWorkbenchSimulatorFrame.getSingleton().showIntegratedVersion(DSWorkbenchSettingsDialog.getSingleton().getWebProxy(),GlobalOptions.getSelectedServer());
            }
            Point coord = new Point(report.getTargetVillage().getX(), report.getTargetVillage().getY());
            DSWorkbenchSimulatorFrame.getSingleton().insertValuesExternally(coord, values, this);
        } else {
            JOptionPaneHelper.showInformationBox(this, "A*Star ist im Offline-Modus leider nicht verfügbar.", "Information");
        }
    }

    private void copyBBToExternalClipboardEvent() {
        List<FightReport> selection = getSelectedReports();
        if (selection.isEmpty()) {
            showInfo("Keine Berichte ausgewählt");
            return;
        }
        boolean extended = (JOptionPaneHelper.showQuestionConfirmBox(this, "Erweiterte BB-Codes verwenden (nur für Forum und Notizen geeignet)?", "Erweiterter BB-Code", "Nein", "Ja") == JOptionPane.YES_OPTION);

        StringBuilder b = new StringBuilder();
        if (extended) {
            b.append("[u][size=12]Angriffsberichte[/size][/u]\n\n");
        } else {
            b.append("[u]Angriffsberichte[/u]\n\n");
        }

        b.append(new ReportListFormatter().formatElements(selection, extended)).append("\n");

        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
            String result = "Daten in Zwischenablage kopiert.";
            showSuccess(result);
        } catch (Exception e) {
            logger.error("Failed to copy data to clipboard", e);
            String result = "Fehler beim Kopieren in die Zwischenablage.";
            showError(result);
        }
    }

    private boolean copyToInternalClipboard() {

        List<FightReport> selection = getSelectedReports();
        StringBuilder b = new StringBuilder();
        int cnt = 0;
        for (FightReport r : selection) {
            b.append(FightReport.toInternalRepresentation(r)).append("\n");
            cnt++;
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(b.toString()), null);
            showSuccess(cnt + ((cnt == 1) ? " Bericht kopiert" : " Berichte kopiert"));
            return true;
        } catch (HeadlessException hex) {
            showError("Fehler beim Kopieren der Berichte");
            return false;
        }
    }

    private void cutToInternalClipboard() {
        int size = getSelectedReports().size();
        if (copyToInternalClipboard() && deleteSelection(false)) {
            showSuccess(size + ((size == 1) ? " Bericht ausgeschnitten" : " Berichte ausgeschnitten"));
        } else {
            showError("Fehler beim Ausschneiden der Berichte");
        }
    }

    private void pasteFromInternalClipboard() {
        try {
            String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);

            String[] lines = data.split("\n");
            int cnt = 0;
            ReportManager.getSingleton().invalidate();
            for (String line : lines) {
                FightReport r = FightReport.fromInternalRepresentation(line);
                if (r != null) {
                    ReportManager.getSingleton().addManagedElement(sReportSet, r);
                    cnt++;
                }
            }
            showSuccess(cnt + ((cnt == 1) ? " Bericht eingefügt" : " Berichte eingefügt"));
        } catch (UnsupportedFlavorException | IOException ufe) {
            logger.error("Failed to copy reports from internal clipboard", ufe);
            showError("Fehler beim Einfügen der Berichte");
        }
        reportModel.fireTableDataChanged();
        ReportManager.getSingleton().revalidate();
    }

    private List<FightReport> getSelectedReports() {
        final List<FightReport> selectedReports = new LinkedList<>();
        int[] selectedRows = jxReportTable.getSelectedRows();
        if (selectedRows != null && selectedRows.length < 1) {
            return selectedReports;
        }
        for (Integer selectedRow : selectedRows) {
            FightReport r = (FightReport) ReportManager.getSingleton().getAllElements(sReportSet).get(jxReportTable.convertRowIndexToModel(selectedRow));
            if (r != null) {
                selectedReports.add(r);
            }
        }
        return selectedReports;
    }
}
