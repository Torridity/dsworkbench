/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.ui.components.DateTimeField;
import java.awt.Component;
import java.util.Calendar;
import java.util.Date;
import javax.swing.DefaultCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 *
 * @author Jejkal
 */
public class DateSpinEditor extends DefaultCellEditor {

    private final JSpinner spinnerComponent = new javax.swing.JSpinner();
private DateTimeField dtf = new DateTimeField();
    public DateSpinEditor() {
        super(new JTextField(""));
        setClickCountToStart(2);
    }

    @Override
    public Object getCellEditorValue() {
        return dtf.getSelectedDate();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        dtf.setDate((Date)value);
        return dtf;
    }
}
