/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.ui.BBPanel;
import de.tor.tribes.util.BBChangeListener;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
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
            }
        });
        editor.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
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
        return editor.getBBCode();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editor.setBBCode((String) value);
        editor.setEditMode(true);
        editor.setSize(360, 250);
        editor.setPreferredSize(new Dimension(360, 250));
        editor.setMinimumSize(new Dimension(360, 250));
        dlg = new JDialog(new JFrame(), false);
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
