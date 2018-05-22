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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.ui.renderer.DefaultTableHeaderRenderer;
import de.tor.tribes.ui.renderer.NumberFormatCellRenderer;
import de.tor.tribes.util.BrowserInterface;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.dsreal.DSRealManager;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.decorator.PatternPredicate;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.table.TableColumnExt;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

/**
 *
 * @author Torridity
 */
public class RankTableTab extends javax.swing.JPanel implements ListSelectionListener {

    public enum RANK_TYPE {

        TRIBE, ALLY, TRIBE_BASH, ALLY_BASH
    }
    private static Logger logger = Logger.getLogger("RankTableTab");
    private RANK_TYPE eType = null;
    private JXTable jxRankTable = new JXTable();
    private DefaultTableModel theModel = null;
    private PainterHighlighter highlighter = null;

    private void buildTribeModel() {
        logger.debug("Building tribe ranking");
        jxRankTable.getTableHeader().setReorderingAllowed(false);
        theModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Rang", "Name", "Stamm", "Punkte", "Dörfer", "Punkte/Dorf"
                }) {

            Class[] types = new Class[]{
                Integer.class, Tribe.class, Ally.class, Double.class, Integer.class, Double.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        Enumeration<Integer> tIDs = DataHolder.getSingleton().getTribes().keys();
        while (tIDs.hasMoreElements()) {
            Tribe next = DataHolder.getSingleton().getTribes().get(tIDs.nextElement());
            Ally nextAlly = NoAlly.getSingleton();
            if (next.getAlly() != null) {
                nextAlly = next.getAlly();
            }
            double pointPerVillage = 0.0;
            int v = next.getVillages();
            if (v > 0) {
                pointPerVillage = next.getPoints() / v;
            }
            theModel.addRow(new Object[]{next.getRank(), next, nextAlly, next.getPoints(), v, pointPerVillage});
        }
    }

    private void buildAllyModel() {
        logger.debug("Building ally ranking");
        theModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Rang", "Name", "Tag", "Punkte Top 40", "Punkte", "Mitglieder", "Punkte/Mitglied", "Dörfer"
                }) {

            Class[] types = new Class[]{
                Integer.class, Ally.class, String.class, Double.class, Double.class, Integer.class, Double.class, Integer.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        Enumeration<Integer> tIDs = DataHolder.getSingleton().getAllies().keys();
        while (tIDs.hasMoreElements()) {
            Ally next = DataHolder.getSingleton().getAllies().get(tIDs.nextElement());
            double pointPerTribe = 0.0;
            int m = next.getMembers();
            if (m > 0) {
                pointPerTribe = next.getPoints() / next.getMembers();
            }

            String tag = next.getTag();
            theModel.addRow(new Object[]{next.getRank(), next, tag, next.getPoints(), next.getAll_points(), m, pointPerTribe, next.getVillages()});
        }
    }

    private void buildTribeBashModel() {
        logger.debug("Building tribe bash ranking");
        theModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Rang (Off)", "Rang (Deff)", "Name", "Kills Off", "Kills Deff", "<html>Kills:Punkte<BR/>Off [%]</html>", "<html>Kills:Punkte<BR/>Deff [%]</html>"
                }) {

            Class[] types = new Class[]{
                Integer.class, Integer.class, Tribe.class, Double.class, Double.class, Double.class, Double.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };


        Enumeration<Integer> tIDs = DataHolder.getSingleton().getTribes().keys();
        while (tIDs.hasMoreElements()) {
            Tribe next = DataHolder.getSingleton().getTribes().get(tIDs.nextElement());
            double p = next.getPoints();
            if (p > 0) {
                double killsPerPointOff = 100 * next.getKillsAtt() / p;
                double killsPerPointDef = 100 * next.getKillsDef() / p;

                int rankOff = next.getRankAtt();
                int rankDef = next.getRankDef();
                if (rankOff == 0) {
                    rankOff = DataHolder.getSingleton().getTribes().size();
                }
                if (rankDef == 0) {
                    rankDef = DataHolder.getSingleton().getTribes().size();
                }
                theModel.addRow(new Object[]{rankOff, rankDef, next, next.getKillsAtt(), next.getKillsDef(), killsPerPointOff, killsPerPointDef});
            }
        }
    }

    private void buildAllyBashModel() {
        logger.debug("Building ally bash ranking");
        theModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Name", "Tag", "Mitglieder", "Kills Off", "Kills Deff", "<html>Kills:Punkte<BR/>Off [%]</html>", "<html>Kills:Punkte<BR/>Deff [%]</html>"
                }) {

            Class[] types = new Class[]{
                Ally.class, String.class, Integer.class, Double.class, Double.class, Double.class, Double.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };


        Enumeration<Integer> tIDs = DataHolder.getSingleton().getAllies().keys();
        while (tIDs.hasMoreElements()) {
            Ally next = DataHolder.getSingleton().getAllies().get(tIDs.nextElement());
            double p = next.getAll_points();
            if (p > 0) {
                long killsOff = 0;
                long killsDef = 0;
                for (Tribe t : next.getTribes()) {
                    killsOff += t.getKillsAtt();
                    killsDef += t.getKillsDef();
                }
                double killsPerPointOff = 100 * killsOff / p;
                double killsPerPointDef = 100 * killsDef / p;
                String tag = next.getTag();
                theModel.addRow(new Object[]{next, tag, next.getMembers(), killsOff, killsDef, killsPerPointOff, killsPerPointDef});
            }
        }
    }

    /**
     * Creates new form AttackTablePanel
     */
    public RankTableTab(RANK_TYPE pType, final ActionListener pActionListener) {
        eType = pType;
        initComponents();
        switch (eType) {
            case TRIBE: {
                buildTribeModel();
                break;
            }
            case ALLY: {
                buildAllyModel();
                break;
            }
            case TRIBE_BASH: {
                buildTribeBashModel();
                break;
            }
            case ALLY_BASH: {
                buildAllyBashModel();
                break;
            }
        }
        jScrollPane1.setViewportView(jxRankTable);
        jxRankTable.setRowHeight(24);
        jxRankTable.setHighlighters(HighlighterFactory.createAlternateStriping(Constants.DS_ROW_A, Constants.DS_ROW_B));
        jxRankTable.setColumnControlVisible(true);
        jxRankTable.setModel(theModel);
        List<SortKey> keys = new LinkedList<>();
        keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        jxRankTable.getRowSorter().setSortKeys(keys);
        jxRankTable.setDefaultRenderer(Integer.class, new NumberFormatCellRenderer());
        jxRankTable.setDefaultRenderer(Double.class, new NumberFormatCellRenderer());

        jxRankTable.getActionMap().put("find", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pActionListener.actionPerformed(new ActionEvent(jxRankTable, 0, "Find"));
            }
        });

        jxRankTable.getSelectionModel().addListSelectionListener(RankTableTab.this);
    }

    public void deregister() {
        jxRankTable.getSelectionModel().removeListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int selectionCount = jxRankTable.getSelectedRowCount();
            if (selectionCount != 0) {
                showInfo(selectionCount + ((selectionCount == 1) ? " Element gewählt" : " Elemente gewählt"));
            }
        }
    }

    public void openDSReal() {
        int row = jxRankTable.getSelectedRow();
        if (row == -1) {
            return;
        }
        row = jxRankTable.convertRowIndexToModel(row);
        String url = "http://dsreal.de/index.php?screen=file&mode=";
        switch (eType) {
            case TRIBE: {
                //tribe
                Tribe t = (Tribe) theModel.getValueAt(row, 1);
                url += "player&id=" + t.getId() + "&world=" + GlobalOptions.getSelectedServer();
                break;
            }
            case ALLY: {
                //ally
                Ally a = (Ally) theModel.getValueAt(row, 1);
                url += "ally&id=" + a.getId() + "&world=" + GlobalOptions.getSelectedServer();
                break;
            }
            case TRIBE_BASH: {
                //bash tribe
                Tribe t = (Tribe) theModel.getValueAt(row, 2);
                url += "player&id=" + t.getId() + "&world=" + GlobalOptions.getSelectedServer();
                break;
            }
            case ALLY_BASH: {
                //bash ally
                Ally a = (Ally) theModel.getValueAt(row, 0);
                url += "ally&id=" + a.getId() + "&world=" + GlobalOptions.getSelectedServer();
                break;
            }
        }
        BrowserInterface.openPage(url);
    }

    public void showDSRealChart() {


        int row = jxRankTable.getSelectedRow();
        if (row == -1) {
            return;
        }
        row = jxRankTable.convertRowIndexToModel(row);

        switch (eType) {
            case TRIBE: {
                //tribe
                Tribe t = (Tribe) theModel.getValueAt(row, 1);
                DSRealManager.getSingleton().getTribePointsChart(t);
                break;
            }
            case ALLY: {
                //ally
                Ally a = (Ally) theModel.getValueAt(row, 1);
                DSRealManager.getSingleton().getAllyPointsChart(a);
                break;
            }
            case TRIBE_BASH: {
                //bash tribe
                Tribe t = (Tribe) theModel.getValueAt(row, 2);
                DSRealManager.getSingleton().getTribeBashChart(t);
                break;
            }
            case ALLY_BASH: {
                //bash ally
                Ally a = (Ally) theModel.getValueAt(row, 0);
                DSRealManager.getSingleton().getAllyBashChart(a);
                break;
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

    public RANK_TYPE getType() {
        return eType;
    }

    public JXTable getRankTable() {
        return jxRankTable;
    }

    public void updateTab() {
        jxRankTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
    }

    public void updateFilter(final String pValue, final List<String> columns, final boolean pCaseSensitive, final boolean pFilterRows) {
        if (highlighter != null) {
            jxRankTable.removeHighlighter(highlighter);
        }
        if (!pFilterRows) {
            jxRankTable.setRowFilter(null);
            final List<Integer> relevantCols = new LinkedList<>();
            List<TableColumn> cols = jxRankTable.getColumns(true);
            for (int i = 0; i < jxRankTable.getColumnCount(); i++) {
                TableColumnExt col = jxRankTable.getColumnExt(i);
                if (col.isVisible() && columns.contains(col.getTitle())) {
                    relevantCols.add(cols.indexOf(col));
                }
            }
            for (Integer col : relevantCols) {
                PatternPredicate patternPredicate0 = new PatternPredicate((pCaseSensitive ? "" : "(?i)") + Matcher.quoteReplacement(pValue), col);
                MattePainter mp = new MattePainter(new Color(0, 0, 0, 120));
                highlighter = new PainterHighlighter(new HighlightPredicate.NotHighlightPredicate(patternPredicate0), mp);
                jxRankTable.addHighlighter(highlighter);
            }
        } else {
            jxRankTable.setRowFilter(new RowFilter<TableModel, Integer>() {

                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                    final List<Integer> relevantCols = new LinkedList<>();
                    List<TableColumn> cols = jxRankTable.getColumns(true);
                    for (int i = 0; i < jxRankTable.getColumnCount(); i++) {
                        TableColumnExt col = jxRankTable.getColumnExt(i);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
}
