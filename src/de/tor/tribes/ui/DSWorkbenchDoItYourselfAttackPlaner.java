/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchDoItYourselflAttackPlaner.java
 *
 * Created on Nov 25, 2009, 10:27:45 PM
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.editors.UnitCellEditor;
import de.tor.tribes.ui.models.DoItYourselfAttackTableModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Component;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;

/**
 *@TODO (1.9) Add help page
 * @author Torridity
 */
public class DSWorkbenchDoItYourselfAttackPlaner extends AbstractDSWorkbenchFrame {

    private static Logger logger = Logger.getLogger("DoItYourselflAttackPlaner");
    private static DSWorkbenchDoItYourselfAttackPlaner SINGLETON = null;
    private List<TableCellRenderer> mHeaderRenderers = null;

    /** Creates new form DSWorkbenchDoItYourselflAttackPlaner */
    DSWorkbenchDoItYourselfAttackPlaner() {
        initComponents();
        try {
            jAlwaysOnTopBox.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("doityourself.attack.frame.alwaysOnTop")));
            setAlwaysOnTop(jAlwaysOnTopBox.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        mHeaderRenderers = new LinkedList<TableCellRenderer>();
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

        for (int i = 0; i < 7; i++) {
            mHeaderRenderers.add(headerRenderer);
        }

       // jAttackTable.setDefaultRenderer(Village.class, new VillageCellRenderer());
        jAttackTable.setDefaultEditor(Village.class, new DefaultCellEditor(new JTextField("")));
        jAttackTable.setDefaultEditor(Date.class, new DateSpinEditor());
        jAttackTable.setDefaultRenderer(Date.class, new DateCellRenderer());
        jAttackTable.setDefaultEditor(UnitHolder.class, new UnitCellEditor());
       // jAttackTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jAttackTable.setRowHeight(20);
        jAttackTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selected = jAttackTable.getSelectedRows().length;
                if (selected == 0) {
                    setTitle("Angriffe");
                } else if (selected == 1) {
                    setTitle("Angriffe (1 Angriff ausgewählt)");
                } else if (selected > 1) {
                    setTitle("Angriffe (" + selected + " Angriffe ausgewählt)");
                }
            }
        });

        DoItYourselfCountdownThread thread = new DoItYourselfCountdownThread();
        thread.setDaemon(true);
        thread.start();

        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        //     GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.markers_view", GlobalOptions.getHelpBroker().getHelpSet());
// </editor-fold>

        pack();
    }

    public static DSWorkbenchDoItYourselfAttackPlaner getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchDoItYourselfAttackPlaner();
        }
        return SINGLETON;
    }

    protected void setupAttackPlaner() {
        jAttackTable.invalidate();
        jAttackTable.setModel(DoItYourselfAttackTableModel.getSingleton());
        //setup renderer and general view
        DoItYourselfAttackTableModel.getSingleton().clear();
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jAttackTable.getModel());
        jAttackTable.setRowSorter(sorter);

        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        for (int i = 0; i < jAttackTable.getColumnCount(); i++) {
            jAttackTable.getColumn(jAttackTable.getColumnName(i)).setHeaderRenderer(mHeaderRenderers.get(i));
        }
        jAttackTable.revalidate();
        jAttackTable.repaint();
    }

    protected void updateCountdown() {
        jAttackTable.repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAttackTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jAlwaysOnTopBox = new javax.swing.JCheckBox();

        setTitle("Manueller Angriffsplaner");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jAttackTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jAttackTable);

        jButton1.setText("jButton1");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAddAttackEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .addComponent(jButton1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jAlwaysOnTopBox.setText("Immer im Vordergrund");
        jAlwaysOnTopBox.setOpaque(false);
        jAlwaysOnTopBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAttackFrameOnTopEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAlwaysOnTopBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jAlwaysOnTopBox)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireAttackFrameOnTopEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAttackFrameOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireAttackFrameOnTopEvent

    private void fireAddAttackEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAddAttackEvent
        DoItYourselfAttackTableModel.getSingleton().addAttack(null, null, Calendar.getInstance().getTime(), DataHolder.getSingleton().getUnitByPlainName("ram"));
        jAttackTable.repaint();
    }//GEN-LAST:event_fireAddAttackEvent
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAlwaysOnTopBox;
    private javax.swing.JTable jAttackTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}

class DoItYourselfCountdownThread extends Thread {

    public DoItYourselfCountdownThread() {
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (DSWorkbenchDoItYourselfAttackPlaner.getSingleton().isVisible()) {
                    DSWorkbenchDoItYourselfAttackPlaner.getSingleton().updateCountdown();
                    sleep(100);
                } else {
                    sleep(1000);
                }
            } catch (Exception e) {
            }
        }
    }
}
