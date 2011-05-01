/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

/**
 *
 * @author Torridity
 */
public class LinkGroupColorCellEditor extends DefaultCellEditor {

    private MultiBooleanTableCellEditor cellEditor = null;
    public static final Color COLOR1 = Color.RED;
    public static final Color COLOR2 = Color.GREEN;
    public static final Color COLOR3 = Color.BLUE;
    public static final Color COLOR4 = Color.MAGENTA;

    public LinkGroupColorCellEditor() {
        super(new JComboBox());
        setClickCountToStart(2);
        cellEditor = new MultiBooleanTableCellEditor();
        cellEditor.setBorder(BorderFactory.createEmptyBorder());

        cellEditor.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    fireEditingStopped();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    fireEditingCanceled();
                }
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        return cellEditor.getSelection();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        cellEditor.setSelection((Integer) value);
        return cellEditor;
    }
}
