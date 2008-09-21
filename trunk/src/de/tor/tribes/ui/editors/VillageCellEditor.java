/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.types.Village;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Jejkal
 */
public class VillageCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JComboBox comboComponent = new javax.swing.JComboBox();

    public VillageCellEditor() {
        comboComponent.addKeyListener(new KeyListener() {

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
    /* comboComponent.addItemListener(new ItemListener() {
    
    @Override
    public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.DESELECTED) {
    comboComponent.transferFocus();
    }
    }
    });
    comboComponent.addFocusListener(new FocusListener() {
    
    @Override
    public void focusGained(FocusEvent e) {
    }
    
    @Override
    public void focusLost(FocusEvent e) {
    fireEditingStopped();
    }
    });*/
    }

    @Override
    public Object getCellEditorValue() {
        return comboComponent.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Village current = (Village) value;
        DefaultComboBoxModel model = new DefaultComboBoxModel(current.getTribe().getVillageList().toArray(new Village[]{}));

        comboComponent.setModel(model);
        comboComponent.setSelectedItem(value);
        //comboComponent.setSelectedItem(value);
        return comboComponent;
    }
}
