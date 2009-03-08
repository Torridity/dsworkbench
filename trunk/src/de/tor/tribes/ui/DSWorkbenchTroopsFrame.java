/*
 * DSWorkbenchTroopsFrame.java
 *
 * Created on 2. Oktober 2008, 13:34
 */
package de.tor.tribes.ui;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.editors.DateSpinEditor;
import de.tor.tribes.ui.models.TroopsManagerTableModel;
import de.tor.tribes.ui.renderer.DateCellRenderer;
import de.tor.tribes.util.Constants;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.log4j.Logger;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.TroopsManagerListener;
import java.awt.Component;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import de.tor.tribes.ui.renderer.NumberFormatCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author  Jejkal
 */
public class DSWorkbenchTroopsFrame extends AbstractDSWorkbenchFrame implements TroopsManagerListener {

    private static Logger logger = Logger.getLogger("TroopsDialog");
    private static DSWorkbenchTroopsFrame SINGLETON = null;
    private List<DefaultTableCellRenderer> renderers = new LinkedList<DefaultTableCellRenderer>();
    private List<ImageIcon> mPowerIcons = new LinkedList<ImageIcon>();

    public static synchronized DSWorkbenchTroopsFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchTroopsFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchTroopsFrame */
    DSWorkbenchTroopsFrame() {
        initComponents();

        getContentPane().setBackground(Constants.DS_BACK);

        try {
            jTroopsInformationAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("troops.frame.alwaysOnTop")));
            setAlwaysOnTop(jTroopsInformationAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        //color scrollpanes of selection dialog
        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jTroopsTable.setColumnSelectionAllowed(false);
        jTroopsTable.setModel(TroopsManagerTableModel.getSingleton());
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(TroopsManagerTableModel.getSingleton());
        /*sorter.setSortsOnUpdates(false);
        sorter.setMaxSortKeys(2);*/
        jTroopsTable.setRowSorter(sorter);
        jTroopsTable.setDefaultRenderer(Integer.class, new NumberFormatCellRenderer());
        jTroopsTable.setDefaultRenderer(Double.class, new NumberFormatCellRenderer());

        jTroopsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selected = jTroopsTable.getSelectedRows().length;
                if (selected == 0) {
                    setTitle("Truppen");
                } else if (selected == 1) {
                    setTitle("Truppen (1 Dorf ausgewählt)");
                } else if (selected > 1) {
                    setTitle("Truppen (" + selected + " Dörfer ausgewählt)");
                }
            }
        });

        try {
            mPowerIcons.add(new ImageIcon("graphics/icons/att.png"));
            mPowerIcons.add(new ImageIcon("graphics/icons/def.png"));
            mPowerIcons.add(new ImageIcon("graphics/icons/def_cav.png"));
            mPowerIcons.add(new ImageIcon("graphics/icons/def_archer.png"));
        } catch (Exception e) {
            logger.error("Failed to read table header icons", e);
        }
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.troops_view", GlobalOptions.getHelpBroker().getHelpSet());
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

        jTroopsInformationAlwaysOnTop = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTroopsTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();

        setTitle("Truppen");

        jTroopsInformationAlwaysOnTop.setText("Immer im Vordergrund");
        jTroopsInformationAlwaysOnTop.setOpaque(false);
        jTroopsInformationAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireTroopsFrameOnTopEvent(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jTroopsTable.setBackground(new java.awt.Color(236, 233, 216));
        jTroopsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTroopsTable.setOpaque(false);
        jScrollPane1.setViewportView(jTroopsTable);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton1.setToolTipText("Gewählte Truppeninformationen entfernen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveTroopsEvent(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTroopsInformationAlwaysOnTop)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTroopsInformationAlwaysOnTop)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void fireTroopsFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireTroopsFrameOnTopEvent
    setAlwaysOnTop(!isAlwaysOnTop());
}//GEN-LAST:event_fireTroopsFrameOnTopEvent

private void fireRemoveTroopsEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveTroopsEvent
    int[] rows = jTroopsTable.getSelectedRows();
    if (rows.length == 0) {
        return;
    }

    String message = ((rows.length == 1) ? "Eintrag " : (rows.length + " Einträge ")) + "wirklich löschen?";
    UIManager.put("OptionPane.noButtonText", "Nein");
    UIManager.put("OptionPane.yesButtonText", "Ja");
    int res = JOptionPane.showConfirmDialog(this, message, "Truppeninformationen entfernen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    UIManager.put("OptionPane.noButtonText", "No");
    UIManager.put("OptionPane.yesButtonText", "Yes");
    if (res != JOptionPane.YES_OPTION) {
        return;
    }

    jTroopsTable.editingCanceled(new ChangeEvent(this));

    for (int r = rows.length - 1; r >= 0; r--) {
        jTroopsTable.invalidate();
        int row = jTroopsTable.convertRowIndexToModel(rows[r]);
        TroopsManagerTableModel.getSingleton().removeRow(row);
        jTroopsTable.revalidate();
    }
    jTroopsTable.updateUI();
}//GEN-LAST:event_fireRemoveTroopsEvent

    protected void setupTroopsPanel() {
        jTroopsTable.invalidate();
        jTroopsTable.setModel(new DefaultTableModel());
        jTroopsTable.revalidate();

        jTroopsTable.setModel(TroopsManagerTableModel.getSingleton());
        TroopsManager.getSingleton().addTroopsManagerListener(this);
        //setup renderer and general view
        jTroopsTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                c.setBackground(Constants.DS_BACK);
                DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                int unitCount = DataHolder.getSingleton().getUnits().size();
                r.setHorizontalAlignment(JLabel.CENTER);
                if (column < 3) {
                    r.setText("<html><b>" + r.getText() + "</b></html>");
                } else if (column < 3 + unitCount) {
                    try {
                        r.setIcon(ImageManager.getUnitIcon(column - 3));
                    } catch (Exception e) {
                        r.setText(DataHolder.getSingleton().getUnits().get(column - 3).getName());
                    }
                } else {
                    if (column == unitCount + 3) {
                        //off col
                        r.setIcon(mPowerIcons.get(0));
                    } else if (column == unitCount + 4) {
                        //def col
                        r.setIcon(mPowerIcons.get(1));
                    } else if (column == unitCount + 5) {
                        //def cav col
                        r.setIcon(mPowerIcons.get(2));
                    } else {
                        //def archer col
                        r.setIcon(mPowerIcons.get(3));
                    }
                }
                return r;
            }
        };

        for (int i = 0; i < jTroopsTable.getColumnCount(); i++) {
            TableColumn column = jTroopsTable.getColumnModel().getColumn(i);
            column.setHeaderRenderer(headerRenderer);
            if ((i > 2 && i < DataHolder.getSingleton().getUnits().size() + 3)) {
                column.setWidth(60);
                column.setPreferredWidth(60);
            //column.setResizable(false);
            }
            renderers.add(headerRenderer);
        }
        jTroopsTable.revalidate();
        TroopsManager.getSingleton().forceUpdate();
    }

    @Override
    public void fireTroopsChangedEvent() {
        try {
            jTroopsTable.invalidate();
            jTroopsTable.getTableHeader().setReorderingAllowed(false);

            for (int i = 0; i < jTroopsTable.getColumnCount(); i++) {
                jTroopsTable.getColumnModel().getColumn(i).setHeaderRenderer(renderers.get(i));
            }

            jTroopsTable.revalidate();
            jTroopsTable.updateUI();
        } catch (Exception e) {
            logger.error("Failed to update troops table", e);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox jTroopsInformationAlwaysOnTop;
    private javax.swing.JTable jTroopsTable;
    // End of variables declaration//GEN-END:variables
}
