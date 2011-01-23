/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.ui.renderer.ColorListCellRenderer;
import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;

/**
 *
 * @author Torridity
 */
public class LinkGroupColorCellEditor extends DefaultCellEditor {

    private JComboBox comboComponent = null;
    public static final Color COLOR1 = Color.RED;
    public static final Color COLOR2 = Color.GREEN;
    public static final Color COLOR3 = Color.BLUE;
    public static final Color COLOR4 = Color.MAGENTA;

    public LinkGroupColorCellEditor() {
        super(new JComboBox());
        setClickCountToStart(2);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        comboComponent = new javax.swing.JComboBox() {

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

        model.addElement(Constants.DS_BACK_LIGHT);
        model.addElement(COLOR1);
        model.addElement(COLOR2);
        model.addElement(COLOR3);
        model.addElement(COLOR4);
        comboComponent.setBorder(BorderFactory.createEmptyBorder());
        comboComponent.setModel(model);

        comboComponent.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    stopCellEditing();
                }
            }
        });

        comboComponent.setRenderer(new ColorListCellRenderer());
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
    }

    @Override
    public Object getCellEditorValue() {
        return comboComponent.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        comboComponent.setSelectedItem(value);
        return comboComponent;
    }
}
