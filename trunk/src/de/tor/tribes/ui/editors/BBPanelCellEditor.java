/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.ui.BBPanel;
import de.tor.tribes.util.BBChangeListener;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Torridity
 */
public class BBPanelCellEditor extends AbstractCellEditor implements TableCellEditor {

    private BBPanel editor = null;
    private JDialog dlg = null;

    public BBPanelCellEditor(JTextField pF) {
        editor = new BBPanel(new BBChangeListener() {

            @Override
            public void fireBBChangedEvent() {
                dlg.setVisible(false);
                fireEditingStopped();
            }
        });
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= 2;
        }
        return true;
    }

    @Override
    public Object getCellEditorValue() {
        return editor.getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editor.setBBCode((String) value);
        editor.setEditMode(true);
        // editor.setSize(200, 100);
        /*editor.setPreferredSize(new Dimension(200, 100));
        editor.setMinimumSize(new Dimension(200, 100));*/
        dlg = new JDialog(new JFrame(), true);
        dlg.setLocation(MouseInfo.getPointerInfo().getLocation());
        dlg.setResizable(false);
        dlg.setUndecorated(true);
        JPanel p = new JPanel();
        p.add(editor);
        dlg.getContentPane().add(p);
        dlg.pack();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                dlg.setVisible(true);
            }
        });

        return new JLabel("");
    }
}
