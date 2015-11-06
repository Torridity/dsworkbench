/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.util.Constants;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Charon
 */
public class ChurchLevelCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JComboBox mEditor = new JComboBox();

    public ChurchLevelCellEditor() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(4);
        model.addElement(6);
        model.addElement(8);
        mEditor.setModel(model);
        mEditor.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    stopCellEditing();
                }
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        return mEditor.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        mEditor.setSelectedItem(value);
        if (isSelected) {
            mEditor.setBackground(Constants.DS_BACK);
        } else {
            mEditor.setBackground(Constants.DS_BACK_LIGHT);
        }
        return mEditor;
    }
}
