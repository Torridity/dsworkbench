/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchChurchFrame.java
 *
 * Created on 29.03.2009, 15:11:27
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.Church;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.editors.ColorChooserCellEditor;
import de.tor.tribes.ui.editors.VillageCellEditor;
import de.tor.tribes.ui.renderer.ColorCellRenderer;
import de.tor.tribes.ui.renderer.SortableTableHeaderRenderer;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.church.ChurchManager;
import de.tor.tribes.util.church.ChurchManagerListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.log4j.Logger;

/**
 * @author Charon
 */
public class DSWorkbenchChurchFrame extends AbstractDSWorkbenchFrame implements ChurchManagerListener {

    private static Logger logger = Logger.getLogger("ChurchView");
    private static DSWorkbenchChurchFrame SINGLETON = null;
    private TableCellRenderer mHeaderRenderer = null;

    public static DSWorkbenchChurchFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchChurchFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchChurchFrame */
    DSWorkbenchChurchFrame() {
        initComponents();
        try {
            jChurchFrameAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("church.frame.alwaysOnTop")));
            setAlwaysOnTop(jChurchFrameAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        mHeaderRenderer = new SortableTableHeaderRenderer();

        /* DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
        c.setBackground(Constants.DS_BACK);
        DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
        r.setText("<html><b>" + r.getText() + "</b></html>");
        return c;
        }
        };

        for (int i = 0; i < 4; i++) {
        mHeaderRenderers.add(headerRenderer);
        }*/

        jChurchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selected = jChurchTable.getSelectedRows().length;
                if (selected == 0) {
                    setTitle("Kirchen");
                } else if (selected == 1) {
                    setTitle("Kirchen (1 Kirche ausgewählt)");
                } else if (selected > 1) {
                    setTitle("Kirchen (" + selected + " Kirchen ausgewählt)");
                }
            }
        });

        jChurchTable.setDefaultRenderer(Color.class, new ColorCellRenderer());
        jChurchTable.setDefaultEditor(Color.class, new ColorChooserCellEditor(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //update church color as soon as the colorchooser cell editor has closed
                try {
                    Village v = (Village) jChurchTable.getValueAt(jChurchTable.getSelectedRow(), 1);
                    Church c = ChurchManager.getSingleton().getChurch(v);
                    Color color = (Color) jChurchTable.getValueAt(jChurchTable.getSelectedRow(), 3);
                    c.setRangeColor(color);
                } catch (Exception ex) {
                    logger.warn("Failed to change church range color", ex);
                }
            }
        }));
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.church_view", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>

        pack();
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
        jChurchTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jChurchFrameAlwaysOnTop = new javax.swing.JCheckBox();

        setTitle("Kirchen");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jChurchTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jChurchTable);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/center.png"))); // NOI18N
        jButton1.setToolTipText("Gewähltes Kirchendorf auf der Karte zentrieren");
        jButton1.setMaximumSize(new java.awt.Dimension(57, 33));
        jButton1.setMinimumSize(new java.awt.Dimension(57, 33));
        jButton1.setPreferredSize(new java.awt.Dimension(57, 33));
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCenterChurchVillageEvent(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(239, 235, 223));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton2.setToolTipText("Gewählte Kirchendörfer löschen");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveChurchVillagesEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE))
                .addContainerGap())
        );

        jChurchFrameAlwaysOnTop.setText("Immer im Vordergrund");
        jChurchFrameAlwaysOnTop.setOpaque(false);
        jChurchFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireChurchFrameOnTopEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jChurchFrameAlwaysOnTop, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jChurchFrameAlwaysOnTop)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireChurchFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireChurchFrameOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireChurchFrameOnTopEvent

    private void fireRemoveChurchVillagesEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveChurchVillagesEvent
        int[] rows = jChurchTable.getSelectedRows();
        if (rows.length == 0) {
            return;
        }
        String message = ((rows.length == 1) ? "Kirchendorf " : (rows.length + " Kirchendörfer ")) + "wirklich löschen?";
        if (JOptionPaneHelper.showQuestionConfirmBox(this, message, "Löschen", "Nein", "Ja") == JOptionPane.YES_OPTION) {
            //get markers to remove
            List<Village> toRemove = new LinkedList<Village>();
            jChurchTable.invalidate();
            for (int i = rows.length - 1; i >= 0; i--) {
                int row = jChurchTable.convertRowIndexToModel(rows[i]);
                Village v = ((Village) ((DefaultTableModel) jChurchTable.getModel()).getValueAt(row, 1));
                toRemove.add(v);
            }
            jChurchTable.revalidate();
            //remove all selected markers and update the view once
            ChurchManager.getSingleton().removeChurches(toRemove.toArray(new Village[]{}));
        }
    }//GEN-LAST:event_fireRemoveChurchVillagesEvent

    private void fireCenterChurchVillageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCenterChurchVillageEvent
        int[] rows = jChurchTable.getSelectedRows();
        if (rows.length != 1) {
            return;
        }
        int row = jChurchTable.convertRowIndexToModel(rows[0]);
        Village v = ((Village) ((DefaultTableModel) jChurchTable.getModel()).getValueAt(row, 1));
        DSWorkbenchMainFrame.getSingleton().centerVillage(v);
    }//GEN-LAST:event_fireCenterChurchVillageEvent

    protected void setupChurchPanel() {
        jChurchTable.invalidate();
        jChurchTable.setModel(ChurchManager.getSingleton().getTableModel());
        ChurchManager.getSingleton().addChurchManagerListener(this);
        //setup renderer and general view
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jChurchTable.getModel());
        jChurchTable.setRowSorter(sorter);
        jChurchTable.setDefaultEditor(Village.class, new VillageCellEditor());
        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        //update view
        ChurchManager.getSingleton().churchesUpdatedExternally();
        jChurchTable.revalidate();
        jChurchTable.repaint();
    }

    @Override
    public void fireChurchesChangedEvent() {
        jChurchTable.invalidate();
        jChurchTable.setModel(ChurchManager.getSingleton().getTableModel());

        //setup table view
        jChurchTable.getColumnModel().getColumn(2).setMaxWidth(75);

        for (int i = 0; i < jChurchTable.getColumnCount(); i++) {
            jChurchTable.getColumn(jChurchTable.getColumnName(i)).setHeaderRenderer(mHeaderRenderer);
        }

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(ChurchManager.getSingleton().getTableModel());
        jChurchTable.setRowSorter(sorter);
        jChurchTable.revalidate();
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchChurchFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jChurchFrameAlwaysOnTop;
    private javax.swing.JTable jChurchTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
