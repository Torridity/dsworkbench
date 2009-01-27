/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Jejkal
 */
public class VillageCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JComboBox comboComponent = null;

    public VillageCellEditor() {
        comboComponent = new javax.swing.JComboBox() {

            public void processMouseEvent(MouseEvent e) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                if (isDisplayable() && focusOwner == this && !isPopupVisible()) {
                    showPopup();
                }
            }

            public void processFocusEvent(FocusEvent fe) {
            }
        };
        comboComponent.setBorder(BorderFactory.createEmptyBorder());
        comboComponent.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    stopCellEditing();
                }
            }
        });

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
        Tribe t = current.getTribe();
        List<Village> villages = new LinkedList<Village>();
        if (t != null) {
            //use tribes villages
            villages = t.getVillageList();
        } else {
            //use single village (barbarian)
            villages.add(current);
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(villages.toArray(new Village[]{}));
        comboComponent.setModel(model);
        comboComponent.setSelectedItem(value);
        //comboComponent.setSelectedItem(value);
        return comboComponent;
    }
}
