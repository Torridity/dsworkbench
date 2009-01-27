/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import java.awt.Component;
import java.util.Date;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.table.TableCellEditor;

/**
 *
 * @author Jejkal
 */
public class DateSpinEditor extends AbstractCellEditor implements TableCellEditor {

    private final JSpinner spinnerComponent = new javax.swing.JSpinner();

    public DateSpinEditor() {
        spinnerComponent.setBorder(BorderFactory.createEmptyBorder());
        spinnerComponent.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.SECOND));
        ((DateEditor) spinnerComponent.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        ((DateEditor) spinnerComponent.getEditor()).getFormat().applyPattern("dd.MM.yy HH:mm:ss");
    }

    @Override
    public Object getCellEditorValue() {
        return spinnerComponent.getValue();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        spinnerComponent.setValue((Date) value);
        return spinnerComponent;
    }
}
