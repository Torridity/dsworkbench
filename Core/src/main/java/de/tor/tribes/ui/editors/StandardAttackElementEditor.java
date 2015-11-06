/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.types.StandardAttackElement;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Charon
 */
public class StandardAttackElementEditor extends AbstractCellEditor implements TableCellEditor {

    private final JTextField editor = new JTextField();

    @Override
    public Object getCellEditorValue() {
        return editor.getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editor.setText(((StandardAttackElement) value).toString());
        return editor;
    }
}
