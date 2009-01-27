/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchRankFrame.java
 *
 * Created on 26.12.2008, 23:22:59
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class DSWorkbenchRankFrame extends AbstractDSWorkbenchFrame {

    private static Logger logger = Logger.getLogger("RankDialog");
    private static DSWorkbenchRankFrame SINGLETON = null;

    public static synchronized DSWorkbenchRankFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchRankFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchRankFrame */
    DSWorkbenchRankFrame() {
        initComponents();
        getContentPane().setBackground(Constants.DS_BACK);
        try {
            jAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("rank.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jRankTable.setColumnSelectionAllowed(false);
        jRankTable.getTableHeader().setReorderingAllowed(false);
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.ranking_view", GlobalOptions.getHelpBroker().getHelpSet());
// </editor-fold>
        updateRankTable();
    }

    public void updateRankTable() {
        //build model depending of rank type
        int type = jRankTypeBox.getSelectedIndex();
        switch (type) {
            case 0: {
                buildTribeRanking();
                break;
            }
            case 1: {
                buildAllyRanking();
                break;
            }
            case 2: {
                buildBashTribeRanking();
                break;
            }
            case 3: {
                buildBashAllyRanking();
                break;
            }
        }

        //setup sorter, header and numeric renderer
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>();
        jRankTable.setRowSorter(sorter);
        sorter.setModel(jRankTable.getModel());
        for (int i = 0; i < jRankTable.getColumnCount(); i++) {
            DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                    c.setBackground(Constants.DS_BACK);
                    DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                    r.setText("<html><b>" + r.getText() + "</b></html>");
                    return c;
                }
            };
            jRankTable.getColumn(jRankTable.getColumnName(i)).setHeaderRenderer(headerRenderer);
        }
        TableCellRenderer renderer = new TableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                nf.setMinimumFractionDigits(0);
                JLabel f = (JLabel) c;
                f.setText(nf.format(value));

                return f;
            }
        };
        jRankTable.setDefaultRenderer(Integer.class, renderer);
        jRankTable.setDefaultRenderer(Double.class, renderer);

        sorter.toggleSortOrder(0);
    }

    /**Build table for tribe ranking*/
    private void buildTribeRanking() {
        logger.debug("Building tribe ranking");
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Rang", "Name", "Stamm", "Punkte", "Dörfer", "Punkte/Dorf"
                }) {

            Class[] types = new Class[]{
                Integer.class, String.class, String.class, Double.class, Integer.class, Double.class
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

        Ally a = getAlly();

        String filter = getFilter();

        if (a == null) {
            Enumeration<Integer> tIDs = DataHolder.getSingleton().getTribes().keys();
            while (tIDs.hasMoreElements()) {
                Tribe next = DataHolder.getSingleton().getTribes().get(tIDs.nextElement());
                String ally = "";
                if (next.getAlly() != null) {
                    ally = next.getAlly().toString();
                }
                double pointPerVillage = 0.0;
                int v = next.getVillages();
                if (v > 0) {
                    pointPerVillage = next.getPoints() / v;
                }
                String name = next.getName();
                if ((filter == null) || (name.toLowerCase().indexOf(filter) > -1) || (ally.toLowerCase().indexOf(filter) > -1)) {
                    model.addRow(new Object[]{next.getRank(), name, ally, next.getPoints(), v, pointPerVillage});
                }
            }
        } else {
            for (Tribe t : a.getTribes()) {
                double pointPerVillage = 0.0;
                String ally = a.toString();
                int v = t.getVillages();
                if (v > 0) {
                    pointPerVillage = t.getPoints() / v;
                }

                String name = t.getName();
                if ((filter == null) || (name.toLowerCase().indexOf(filter) > -1) || (ally.toLowerCase().indexOf(filter) > -1)) {
                    model.addRow(new Object[]{t.getRank(), name, ally, t.getPoints(), v, pointPerVillage});
                }
            }
        }
        jRankTable.setModel(model);
    }

    /**Build table for ally ranking*/
    private void buildAllyRanking() {
        logger.debug("Building ally ranking");
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Rang", "Name", "Tag", "Punkte Top 40", "Punkte", "Mitglieder", "Punkte/Mitglied", "Dörfer"
                }) {

            Class[] types = new Class[]{
                Integer.class, String.class, String.class, Double.class, Double.class, Integer.class, Double.class, Integer.class
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

        String filter = getFilter();

        Enumeration<Integer> tIDs = DataHolder.getSingleton().getAllies().keys();
        while (tIDs.hasMoreElements()) {
            Ally next = DataHolder.getSingleton().getAllies().get(tIDs.nextElement());
            double pointPerTribe = 0.0;
            int m = next.getMembers();
            if (m > 0) {
                pointPerTribe = next.getPoints() / next.getMembers();
            }

            String name = next.getName();
            String tag = next.getTag();
            if ((filter == null) || (name.toLowerCase().indexOf(filter) > -1) || (tag.toLowerCase().indexOf(filter) > -1)) {
                model.addRow(new Object[]{next.getRank(), name, tag, next.getPoints(), next.getAll_points(), m, pointPerTribe, next.getVillages()});
            }
        }
        jRankTable.setModel(model);
    }

    /**Build table for tribe bash ranking*/
    private void buildBashTribeRanking() {
        logger.debug("Building tribe bash ranking");
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Rang (Off)", "Rang (Deff)", "Name", "Kills Off", "Kills Deff", "Kills:Punkte<BR/>Off [%]", "Kills:Punkte<BR/>Deff [%]"
                }) {

            Class[] types = new Class[]{
                Integer.class, Integer.class, String.class, Double.class, Double.class, Double.class, Double.class
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

        Ally a = getAlly();

        String filter = getFilter();

        if (a == null) {
            Enumeration<Integer> tIDs = DataHolder.getSingleton().getTribes().keys();
            while (tIDs.hasMoreElements()) {
                Tribe next = DataHolder.getSingleton().getTribes().get(tIDs.nextElement());
                double p = next.getPoints();
                if (p > 0) {
                    double killsPerPointOff = killsPerPointOff = 100 * next.getKillsAtt() / p;
                    double killsPerPointDef = killsPerPointDef = 100 * next.getKillsDef() / p;

                    int rankOff = next.getRankAtt();
                    int rankDef = next.getRankDef();
                    if (rankOff == 0) {
                        rankOff = DataHolder.getSingleton().getTribes().size();
                    }
                    if (rankDef == 0) {
                        rankDef = DataHolder.getSingleton().getTribes().size();
                    }
                    String name = next.getName();
                    if ((filter == null) || (name.toLowerCase().indexOf(filter) > -1)) {
                        model.addRow(new Object[]{rankOff, rankDef, name, next.getKillsAtt(), next.getKillsDef(), killsPerPointOff, killsPerPointDef});
                    }
                }
            }
        } else {
            for (Tribe t : a.getTribes()) {
                double p = t.getPoints();
                if (p > 0) {
                    double killsPerPointOff = 100 * t.getKillsAtt() / p;
                    double killsPerPointDef = 100 * t.getKillsDef() / p;

                    int rankOff = t.getRankAtt();
                    int rankDef = t.getRankDef();
                    if (rankOff == 0) {
                        rankOff = DataHolder.getSingleton().getTribes().size();
                    }
                    if (rankDef == 0) {
                        rankDef = DataHolder.getSingleton().getTribes().size();
                    }
                    String name = t.getName();
                    if ((filter == null) || (name.toLowerCase().indexOf(filter) > -1)) {
                        model.addRow(new Object[]{rankOff, rankDef, name, t.getKillsAtt(), t.getKillsDef(), killsPerPointOff, killsPerPointDef});
                    }
                }
            }
        }

        jRankTable.setModel(model);
    }

    /**Build table for ally bash ranking*/
    private void buildBashAllyRanking() {
        logger.debug("Building ally bash ranking");
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Name", "Tag", "Mitglieder", "Kills Off", "Kills Deff", "Kills:Punkte<BR/>Off [%]", "Kills:Punkte<BR/>Deff [%]"
                }) {

            Class[] types = new Class[]{
                String.class, String.class, Integer.class, Double.class, Double.class, Double.class, Double.class
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

        String filter = getFilter();

        Enumeration<Integer> tIDs = DataHolder.getSingleton().getAllies().keys();
        while (tIDs.hasMoreElements()) {
            Ally next = DataHolder.getSingleton().getAllies().get(tIDs.nextElement());
            double p = next.getAll_points();
            if (p > 0) {
                int killsOff = 0;
                int killsDef = 0;
                for (Tribe t : next.getTribes()) {
                    killsOff += t.getKillsAtt();
                    killsDef += t.getKillsDef();
                }
                double killsPerPointOff = 100 * killsOff / p;
                double killsPerPointDef = 100 * killsDef / p;

                String name = next.getName();
                String tag = next.getTag();
                if ((filter == null) || (name.toLowerCase().indexOf(filter) > -1) || (tag.toLowerCase().indexOf(filter) > -1)) {
                    model.addRow(new Object[]{name, tag, next.getMembers(), killsOff, killsDef, killsPerPointOff, killsPerPointDef});
                }
            }
        }
        jRankTable.setModel(model);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jAlwaysOnTop = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jRankTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jRankTypeBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jAllyBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jFilterField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setTitle("Rangliste");

        jAlwaysOnTop.setText("Immer im Vordergrund");
        jAlwaysOnTop.setOpaque(false);
        jAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireRankFrameAlwaysOnTopEvent(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jRankTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jRankTable);

        jLabel1.setText("Rangliste");

        jRankTypeBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Spieler", "Stämme", "Besiegte Gegner (Spieler)", "Besiegte Gegner (Stämme)" }));
        jRankTypeBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireRankTypeChangedEvent(evt);
            }
        });

        jLabel3.setText("Stamm");

        jAllyBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAllySelectionChangedEvent(evt);
            }
        });

        jLabel4.setText("Filter");

        jFilterField.setMaximumSize(new java.awt.Dimension(100, 20));
        jFilterField.setMinimumSize(new java.awt.Dimension(100, 20));
        jFilterField.setPreferredSize(new java.awt.Dimension(100, 20));
        jFilterField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireFilterKeyReleasedEvent(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel5.setText("(Ein Ausdruck mit weniger als 3 Zeichen löscht den Filter)");

        jButton1.setText("Filtern");
        jButton1.setToolTipText("List mit dem gewählten Ausdruck filtern");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireFilterEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jAllyBox, 0, 630, Short.MAX_VALUE)
                                    .addComponent(jRankTypeBox, 0, 630, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jFilterField, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jButton1))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jRankTypeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jAllyBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jFilterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addGap(28, 28, 28))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jAlwaysOnTop)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jAlwaysOnTop)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireRankFrameAlwaysOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireRankFrameAlwaysOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireRankFrameAlwaysOnTopEvent

    private void fireRankTypeChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireRankTypeChangedEvent
        updateRankTable();
    }//GEN-LAST:event_fireRankTypeChangedEvent

    private void fireAllySelectionChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAllySelectionChangedEvent
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            updateRankTable();
        }
    }//GEN-LAST:event_fireAllySelectionChangedEvent

    private void fireFilterEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireFilterEvent
        if (jFilterField.getText().length() < 3) {
            jFilterField.setText("");
        }
        updateRankTable();
    }//GEN-LAST:event_fireFilterEvent

    private void fireFilterKeyReleasedEvent(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fireFilterKeyReleasedEvent
         if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            fireFilterEvent(null);
        }
    }//GEN-LAST:event_fireFilterKeyReleasedEvent

    private String getFilter() {
        String filter = jFilterField.getText().toLowerCase();
        if (filter.length() < 3) {
            filter = null;
            logger.debug("Using no filter");
        } else {
            logger.debug("Using filter '" + filter + "'");
        }
        return filter;
    }

    private Ally getAlly() {
        Ally a = null;
        try {
            a = (Ally) jAllyBox.getSelectedItem();
            logger.debug("Show ranking for ally '" + a + "'");
        } catch (Exception e) {
            //all selected
        }
        return a;
    }

    protected void updateAllyList() {
        logger.debug("Updating ally list");
        Enumeration<Integer> allyEnum = DataHolder.getSingleton().getAllies().keys();
        List<Ally> allyList = new LinkedList<Ally>();
        while (allyEnum.hasMoreElements()) {
            allyList.add(DataHolder.getSingleton().getAllies().get(allyEnum.nextElement()));
        }
        Ally[] allies = allyList.toArray(new Ally[]{});
        Arrays.sort(allies, Ally.CASE_INSENSITIVE_ORDER);
        DefaultComboBoxModel model = new DefaultComboBoxModel(allies);
        model.insertElementAt("Alle", 0);
        jAllyBox.setModel(model);
        jAllyBox.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        double d = 34397843;
        double d2 = 4564623;
        double p = 100 * (int) d / (int) d2;
        System.out.println(p);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jAllyBox;
    private javax.swing.JCheckBox jAlwaysOnTop;
    private javax.swing.JButton jButton1;
    private javax.swing.JTextField jFilterField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTable jRankTable;
    private javax.swing.JComboBox jRankTypeBox;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    }
