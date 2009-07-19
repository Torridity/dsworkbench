/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchSimulatorFrame.java
 *
 * Created on 19.07.2009, 16:45:19
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.ui.editors.SpreadSheetCellEditor;
import de.tor.tribes.ui.editors.TechCellEditor;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.sim.FighterPart;
import de.tor.tribes.util.sim.OldSimulator;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Charon
 */
public class DSWorkbenchSimulatorFrame extends javax.swing.JFrame {

    private OldSimulator sim = null;

    /** Creates new form DSWorkbenchSimulatorFrame */
    public DSWorkbenchSimulatorFrame() {
        initComponents();
        sim = new OldSimulator();
        sim.parseUnits();
        try {
            ImageManager.loadUnitIcons();
        } catch (Exception e) {
            e.printStackTrace();
        }
        buildTables();
        buildResultTable(false, 0, 0, 0, 1);
    }

    private void buildTables() {
        DefaultTableModel attackerModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Einheit", "Anzahl", "Tech"
                }) {

            Class[] types = new Class[]{
                String.class, Integer.class, Double.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return (columnIndex != 0);
            }
        };

        jAttackerTable.setModel(attackerModel);
        jAttackerTable.setDefaultEditor(Double.class, new TechCellEditor(3));
        jAttackerTable.setDefaultEditor(Integer.class, new SpreadSheetCellEditor());
        jAttackerTable.invalidate();
        for (UnitHolder unit : sim.getUnits()) {
            attackerModel.addRow(new Object[]{unit.getPlainName(), 0, 1});
        }
        jAttackerTable.revalidate();
        jAttackerTable.getColumnModel().getColumn(0).setMaxWidth(40);
        jAttackerTable.getColumnModel().getColumn(1).setMaxWidth(60);
        jAttackerTable.getColumnModel().getColumn(2).setMaxWidth(40);
        jAttackerTable.setRowHeight(20);
        jDefenderTable.setRowHeight(20);
        jDefenderTable.setBackground(Constants.DS_BACK_LIGHT);
        jAttackerTable.setBackground(Constants.DS_BACK_LIGHT);
        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jScrollPane2.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                JLabel label = (JLabel) c;
                label.setIcon(ImageManager.getUnitIcon(sim.getUnitByPlainName((String) value)));
                label.setText("");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        };

        jAttackerTable.setDefaultRenderer(String.class, renderer);
        jDefenderTable.setDefaultRenderer(String.class, renderer);

        DefaultTableModel defenderModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Einheit", "Anzahl", "Tech"
                }) {

            Class[] types = new Class[]{
                String.class, Integer.class, Double.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return (columnIndex != 0);
            }
        };

        jDefenderTable.setModel(defenderModel);
        jDefenderTable.setDefaultEditor(Double.class, new TechCellEditor(3));
        jDefenderTable.setDefaultEditor(Integer.class, new SpreadSheetCellEditor());
        jDefenderTable.invalidate();
        for (UnitHolder unit : sim.getUnits()) {
            defenderModel.addRow(new Object[]{unit.getPlainName(), 0, 1});
        }
        jDefenderTable.revalidate();
        jDefenderTable.getColumnModel().getColumn(0).setMaxWidth(40);
        jDefenderTable.getColumnModel().getColumn(1).setMaxWidth(60);
        jDefenderTable.getColumnModel().getColumn(2).setMaxWidth(40);
        for (int i = 0; i < jDefenderTable.getColumnCount(); i++) {
            DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                    c.setBackground(Constants.DS_BACK);
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                    r.setText("<html><b>" + r.getText() + "</b></html>");
                    return c;
                }
            };
            jDefenderTable.getColumn(jDefenderTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
            jAttackerTable.getColumn(jAttackerTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }

        attackerModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                fireCalculateEvent();
            }
        });

        defenderModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                fireCalculateEvent();
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jNightBonus = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAttackerTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jDefenderTable = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jResultTable = new javax.swing.JTable();
        jWallInfo = new javax.swing.JLabel();
        jWallSpinner = new javax.swing.JSpinner();
        jMoralSpinner = new javax.swing.JSpinner();
        jLuckSpinner = new javax.swing.JSpinner();
        jBuildingInfo = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jCataTargetSpinner = new javax.swing.JSpinner();
        jOffToKillBox = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Simulator");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jLabel28.setText("Wall");

        jLabel29.setText("Moral");
        jLabel29.setMaximumSize(new java.awt.Dimension(30, 14));
        jLabel29.setMinimumSize(new java.awt.Dimension(30, 14));
        jLabel29.setPreferredSize(new java.awt.Dimension(30, 14));

        jLabel30.setText("Glück");
        jLabel30.setMaximumSize(new java.awt.Dimension(30, 14));
        jLabel30.setMinimumSize(new java.awt.Dimension(30, 14));
        jLabel30.setPreferredSize(new java.awt.Dimension(30, 14));

        jNightBonus.setText("Nachbonus");
        jNightBonus.setOpaque(false);
        jNightBonus.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireNightBonusStateChangedEvent(evt);
            }
        });

        jScrollPane1.setBackground(new java.awt.Color(225, 213, 190));
        jScrollPane1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(225, 213, 190), 1, true));
        jScrollPane1.setMaximumSize(new java.awt.Dimension(200, 404));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(200, 404));
        jScrollPane1.setOpaque(false);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 404));

        jAttackerTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jAttackerTable.setGridColor(new java.awt.Color(225, 213, 190));
        jAttackerTable.setOpaque(false);
        jScrollPane1.setViewportView(jAttackerTable);

        jScrollPane2.setBackground(new java.awt.Color(225, 213, 190));
        jScrollPane2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(225, 213, 190), 1, true));
        jScrollPane2.setMaximumSize(new java.awt.Dimension(200, 404));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(200, 404));
        jScrollPane2.setOpaque(false);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(200, 404));

        jDefenderTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jDefenderTable.setGridColor(new java.awt.Color(225, 213, 190));
        jDefenderTable.setOpaque(false);
        jScrollPane2.setViewportView(jDefenderTable);

        jScrollPane3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(225, 213, 190), 1, true));

        jResultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jResultTable.setShowHorizontalLines(false);
        jScrollPane3.setViewportView(jResultTable);

        jWallInfo.setBackground(new java.awt.Color(255, 255, 255));

        jWallSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 20, 1));
        jWallSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireStateChangedEvent(evt);
            }
        });

        jMoralSpinner.setModel(new javax.swing.SpinnerNumberModel(100, 30, 100, 1));
        jMoralSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireStateChangedEvent(evt);
            }
        });

        jLuckSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, -25.0d, 25.0d, 0.1d));
        jLuckSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireStateChangedEvent(evt);
            }
        });

        jBuildingInfo.setBackground(new java.awt.Color(255, 255, 255));

        jLabel31.setText("Katapultziel");

        jCataTargetSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 30, 1));
        jCataTargetSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireStateChangedEvent(evt);
            }
        });

        jOffToKillBox.setText("OffToKill");
        jOffToKillBox.setOpaque(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jWallInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLuckSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                                    .addComponent(jMoralSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jNightBonus)
                        .addGap(79, 79, 79)
                        .addComponent(jOffToKillBox)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 148, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jCataTargetSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                                    .addComponent(jWallSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)))))
                    .addComponent(jBuildingInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, 0, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jWallSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMoralSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLuckSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jNightBonus))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                        .addComponent(jCataTargetSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jOffToKillBox)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jWallInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBuildingInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Tech3", jPanel1);

        jButton1.setText("Schließen");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1)
                        .addContainerGap())
                    .addComponent(jButton1)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireStateChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireStateChangedEvent
        fireCalculateEvent();
}//GEN-LAST:event_fireStateChangedEvent

    private void fireNightBonusStateChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireNightBonusStateChangedEvent
        fireCalculateEvent();
    }//GEN-LAST:event_fireNightBonusStateChangedEvent

    private void fireCalculateEvent() {

        Hashtable<UnitHolder, FighterPart> off = buildOffTable();
        Hashtable<UnitHolder, FighterPart> def = buildDefTable();
        boolean nightBonus = jNightBonus.isSelected();
        int wallLevel = (Integer) jWallSpinner.getValue();
        int cataTarget = (Integer) jCataTargetSpinner.getValue();
        double luck = (Double) jLuckSpinner.getValue();
        double moral = (Integer) jMoralSpinner.getValue();
        if (!jOffToKillBox.isSelected()) {
            sim.calculate(off, def, nightBonus, luck, moral, wallLevel, cataTarget);

            boolean won = sim.hasWon();
            double offDecrement = sim.getOffDecrement();
            double defDecrement = sim.getDefDecrement();
            int wallResult = sim.getWallResult();
            int cataResult = sim.getCataResult();

            buildResultTable(won, offDecrement, defDecrement, wallResult, cataResult);
        } else {
            Integer count = 1;
            calculateOffsToKill(off, def, nightBonus, luck, moral, wallLevel, cataTarget, count);

        }
    }

    private Hashtable<UnitHolder, FighterPart> buildOffTable() {
        Hashtable<UnitHolder, FighterPart> off = new Hashtable<UnitHolder, FighterPart>();
        DefaultTableModel model = (DefaultTableModel) jAttackerTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String unit = (String) model.getValueAt(i, 0);
            UnitHolder u = sim.getUnitByPlainName(unit);
            int cnt = (Integer) model.getValueAt(i, 1);
            int tech = (Integer) model.getValueAt(i, 2);
            if (unit.equals("snob")) {
                tech = 1;
            }
            if (cnt > 0) {
                off.put(u, new FighterPart(u, cnt, tech));
            }
        }
        return off;
    }

    private Hashtable<UnitHolder, FighterPart> buildDefTable() {
        Hashtable<UnitHolder, FighterPart> def = new Hashtable<UnitHolder, FighterPart>();
        DefaultTableModel model = (DefaultTableModel) jDefenderTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String unit = (String) model.getValueAt(i, 0);
            UnitHolder u = sim.getUnitByPlainName(unit);
            int cnt = (Integer) model.getValueAt(i, 1);
            int tech = (Integer) model.getValueAt(i, 2);
            if (unit.equals("snob")) {
                tech = 1;
            }
            if (cnt > 0) {
                def.put(u, new FighterPart(u, cnt, tech));
            }
        }
        return def;
    }

    private void calculateOffsToKill(Hashtable<UnitHolder, FighterPart> pOff, Hashtable<UnitHolder, FighterPart> pDef, boolean pNightBonus, double pLuck, double pMoral, int pWallLevel, int pBuildingLevel, Integer cnt) {
        sim.calculate(pOff, pDef, pNightBonus, pLuck, pMoral, pWallLevel, pBuildingLevel);
        boolean won = sim.hasWon();
        double offDecrement = sim.getOffDecrement();
        double defDecrement = sim.getDefDecrement();
        int wallResult = sim.getWallResult();
        int cataResult = sim.getCataResult();
        if (won) {
            System.out.println("Won after " + cnt + " rounds");
            buildResultTable(won, offDecrement, defDecrement, wallResult, cataResult);
        } else {
            //get remaining troops
            for (int i = 0; i < sim.getUnits().size(); i++) {
                UnitHolder unit = sim.getUnits().get(i);
                FighterPart part = pDef.get(unit);
                if (part != null) {
                    int defUnitCount = part.getUnitCount();
                    int defLoss = 0;
                    if (defDecrement >= 1) {
                        defLoss = defUnitCount;
                    } else {
                        defLoss = (int) Math.round(defDecrement * (double) defUnitCount);
                    }
                    part.setUnitCount(defUnitCount - defLoss);
                    pDef.put(unit, part);
                }
            }
            calculateOffsToKill(pOff, pDef, pNightBonus, pLuck, pMoral, wallResult, cataResult, new Integer(cnt + 1));
        }
    }

    private void buildResultTable(boolean pWon, double pOffDecrement, double pDefDecrement, int pWallResult, int pCataResult) {

        // <editor-fold defaultstate="collapsed" desc=" Build result model">
        DefaultTableModel resultModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "", "spear", "sword", "axe", "spy", "light", "heavy", "ram", "catapult", "snob"
                }) {

            Class[] types = new Class[]{
                String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class
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
        jResultTable.setModel(resultModel);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build header renderer">
        for (int i = 0; i < jResultTable.getColumnCount(); i++) {
            DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                    c.setBackground(Constants.DS_BACK);
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    ((JLabel) c).setIcon(ImageManager.getUnitIcon(sim.getUnitByPlainName((String) value)));
                    ((JLabel) c).setText("");
                    return c;
                }
            };
            jResultTable.getColumn(jResultTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Build result table rows">
        List<Object> attackerBefore = new LinkedList<Object>();
        attackerBefore.add("Angreifer");
        List<Object> attackerLosses = new LinkedList<Object>();
        attackerLosses.add("Verluste");
        List<Object> attackerSurvivors = new LinkedList<Object>();
        attackerSurvivors.add("Überlebende");
        List<Object> defenderBefore = new LinkedList<Object>();
        defenderBefore.add("Verteidiger");
        List<Object> defenderLosses = new LinkedList<Object>();
        defenderLosses.add("Verluste");
        List<Object> defenderSurvivors = new LinkedList<Object>();
        defenderSurvivors.add("Überlebende");

        for (int i = 0; i < jAttackerTable.getRowCount(); i++) {
            int attUnitCount = (Integer) jAttackerTable.getValueAt(i, 1);
            int defUnitCount = (Integer) jDefenderTable.getValueAt(i, 1);
            attackerBefore.add(attUnitCount);
            defenderBefore.add(defUnitCount);
            int attLoss = 0;
            if (pOffDecrement >= 1) {
                attLoss = attUnitCount;
            } else {
                attLoss = (int) Math.round(pOffDecrement * (double) attUnitCount);
            }

            int defLoss = 0;
            if (pDefDecrement >= 1) {
                defLoss = defUnitCount;
            } else {
                defLoss = (int) Math.round(pDefDecrement * (double) defUnitCount);
            }

            attackerLosses.add(attLoss);
            defenderLosses.add(defLoss);
            attackerSurvivors.add(attUnitCount - attLoss);
            defenderSurvivors.add(defUnitCount - defLoss);
        }

        jResultTable.invalidate();
        resultModel.addRow(attackerBefore.toArray());
        resultModel.addRow(attackerLosses.toArray());
        resultModel.addRow(attackerSurvivors.toArray());
        resultModel.addRow(new Object[]{});
        resultModel.addRow(defenderBefore.toArray());
        resultModel.addRow(defenderLosses.toArray());
        resultModel.addRow(defenderSurvivors.toArray());

        jResultTable.revalidate();
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Winner/Loser color renderer">
        final boolean won = pWon;
        DefaultTableCellRenderer winLossRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                c.setBackground(Constants.DS_BACK);
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                try {
                    ((JLabel) c).setText(Integer.toString((Integer) value));
                } catch (Exception e) {
                    ((JLabel) c).setText((String) value);
                }

                if (won) {
                    if (row == 0 || row == 1 || row == 2) {
                        ((JLabel) c).setBackground(Color.GREEN);
                    } else if (row == 4 || row == 5 || row == 6) {
                        ((JLabel) c).setBackground(Color.RED);
                    }
                } else {
                    if (row == 0 || row == 1 || row == 2) {
                        ((JLabel) c).setBackground(Color.RED);
                    } else if (row == 4 || row == 5 || row == 6) {
                        ((JLabel) c).setBackground(Color.GREEN);
                    }
                }
                return c;
            }
        };
        // </editor-fold>

        jScrollPane3.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jResultTable.setDefaultRenderer(Integer.class, winLossRenderer);
        jResultTable.setDefaultRenderer(String.class, winLossRenderer);
        jResultTable.getColumnModel().getColumn(0).setMinWidth(100);
        jResultTable.getColumnModel().getColumn(0).setResizable(false);

        int wall = (Integer) jWallSpinner.getValue();
        if (wall != pWallResult) {
            jWallInfo.setText("<html>Wall zerstört von Stufe <B>" + wall + "</B> auf Stufe <B>" + pWallResult + "</B></html>");
        }

        int building = (Integer) jCataTargetSpinner.getValue();
        if (building != pCataResult) {
            jBuildingInfo.setText("<html>Gebäude zerstört von Stufe <B>" + building + "</B> auf Stufe <B>" + pCataResult + "</B></html>");
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        Font f = new Font("SansSerif", Font.PLAIN, 11);
        UIManager.put("Label.font", f);
        UIManager.put("TextField.font", f);
        UIManager.put("ComboBox.font", f);
        UIManager.put("EditorPane.font", f);
        UIManager.put("TextArea.font", f);
        UIManager.put("List.font", f);
        UIManager.put("Button.font", f);
        UIManager.put("ToggleButton.font", f);
        UIManager.put("CheckBox.font", f);
        UIManager.put("CheckBoxMenuItem.font", f);
        UIManager.put("Menu.font", f);
        UIManager.put("MenuItem.font", f);
        UIManager.put("OptionPane.font", f);
        UIManager.put("Panel.font", f);
        UIManager.put("PasswordField.font", f);
        UIManager.put("PopupMenu.font", f);
        UIManager.put("ProgressBar.font", f);
        UIManager.put("RadioButton.font", f);
        UIManager.put("ScrollPane.font", f);
        UIManager.put("Table.font", f);
        UIManager.put("TableHeader.font", f);
        UIManager.put("TextField.font", f);
        UIManager.put("TextPane.font", f);
        UIManager.put("ToolTip.font", f);
        UIManager.put("Tree.font", f);
        UIManager.put("Viewport.font", f);


        //UIManager.put("Panel.background", Constants.DS_BACK);
        UIManager.put("Label.background", Constants.DS_BACK);
        UIManager.put("MenuBar.background", Constants.DS_BACK);
        UIManager.put("ScrollPane.background", Constants.DS_BACK);
        UIManager.put("Button.background", Constants.DS_BACK);
        UIManager.put("TabbedPane.background", Constants.DS_BACK);
        UIManager.put("SplitPane.background", Constants.DS_BACK);
        UIManager.put("Separator.background", Constants.DS_BACK);
        UIManager.put("Menu.background", Constants.DS_BACK);
        UIManager.put("OptionPane.background", Constants.DS_BACK);
        UIManager.put("ToolBar.background", Constants.DS_BACK);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchSimulatorFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable jAttackerTable;
    private javax.swing.JLabel jBuildingInfo;
    private javax.swing.JButton jButton1;
    private javax.swing.JSpinner jCataTargetSpinner;
    private javax.swing.JTable jDefenderTable;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JSpinner jLuckSpinner;
    private javax.swing.JSpinner jMoralSpinner;
    private javax.swing.JCheckBox jNightBonus;
    private javax.swing.JCheckBox jOffToKillBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTable jResultTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel jWallInfo;
    private javax.swing.JSpinner jWallSpinner;
    // End of variables declaration//GEN-END:variables
}
