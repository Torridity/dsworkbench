/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Charon
 */
public class SpreadSheetCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JTextField mEditor = new JTextField();

    public SpreadSheetCellEditor() {
        mEditor.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                mEditor.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                mEditor.select(0, 0);
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        try {
            return Integer.parseInt(mEditor.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        mEditor.setText(Integer.toString((Integer) value));
        mEditor.setHorizontalAlignment(SwingConstants.RIGHT);
        return mEditor;
    }
}
