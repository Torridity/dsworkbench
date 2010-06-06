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
//public class DateSpinEditor extends AbstractCellEditor implements TableCellEditor {
public class DateSpinEditor extends DefaultCellEditor {

    private final JSpinner spinnerComponent = new javax.swing.JSpinner();
private DateTimeField dtf = new DateTimeField();
    public DateSpinEditor() {
        super(new JTextField(""));
       /* spinnerComponent.setBorder(BorderFactory.createEmptyBorder());
        spinnerComponent.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.SECOND));

        ((DateEditor) spinnerComponent.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        ((DateEditor) spinnerComponent.getEditor()).getFormat().applyPattern("dd.MM.yy HH:mm:ss");
        KeyListener l = new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    fireEditingStopped();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                   fireEditingStopped();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                   fireEditingStopped();
                }
            }
        };
        ((DateEditor) spinnerComponent.getEditor()).addKeyListener(l);
        spinnerComponent.addKeyListener(l);*/
        setClickCountToStart(2);
    }

    @Override
    public Object getCellEditorValue() {
        //return spinnerComponent.getValue();
        return dtf.getSelectedDate();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
       /* if (value == null) {
            spinnerComponent.setValue(Calendar.getInstance().getTime());
        } else {
            spinnerComponent.setValue((Date) value);
        }

        return spinnerComponent;*/

        dtf.setDate((Date)value);
        return dtf;
    }
}
