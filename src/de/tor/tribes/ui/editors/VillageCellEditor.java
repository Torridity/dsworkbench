/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.types.Village;
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
 * @author Jejkal
 */
public class VillageCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JComboBox comboComponent = new javax.swing.JComboBox();

    public VillageCellEditor() {
        comboComponent.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    fireEditingStopped();
                }
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        return comboComponent.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Village current = (Village) value;
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        for (Village v : current.getTribe().getVillageList()) {
            model.addElement(v);
        }
        comboComponent.setModel(model);
        comboComponent.setSelectedItem(value);
        comboComponent.setSelectedItem(value);
        return comboComponent;
    }
}
