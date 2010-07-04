/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import java.awt.Component;
import java.util.Arrays;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;

/**
 *
 * @author Jejkal
 */
public class VillageCellEditor extends DefaultCellEditor {//extends AbstractCellEditor implements TableCellEditor {

    //private JComboBox comboComponent = null;
    public VillageCellEditor() {
        super(new JComboBox());
        setClickCountToStart(2);
        // <editor-fold defaultstate="collapsed" desc=" Old Stuff ">
        /*  comboComponent = new javax.swing.JComboBox() {

        @Override
        public void processMouseEvent(MouseEvent e) {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (isDisplayable() && focusOwner == this && !isPopupVisible()) {
        showPopup();
        }
        }

        @Override
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
        });*/
        // </editor-fold>
    }

    /*@Override
    public Object getCellEditorValue() {
    return (JComboBox.getSelectedItem();
    }*/
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Village current = (Village) value;
        Tribe t = current.getTribe();

        Village[] villages = null;
        if (t != Barbarians.getSingleton()) {
            //use tribes villages
            villages = t.getVillageList();
        } else {
            //use single village (barbarian)
            villages = new Village[]{current};
        }
        Arrays.sort(villages);
        DefaultComboBoxModel model = new DefaultComboBoxModel(villages);
        ((JComboBox) editorComponent).setModel(model);
        ((JComboBox) editorComponent).setSelectedItem(value);

        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
