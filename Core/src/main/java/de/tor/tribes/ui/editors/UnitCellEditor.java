/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.ui.renderer.UnitListCellRenderer;
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
//public class UnitCellEditor extends AbstractCellEditor implements TableCellEditor {
public class UnitCellEditor extends DefaultCellEditor {

    private JComboBox comboComponent = null;

    public UnitCellEditor() {
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
        model.addElement(UnknownUnit.getSingleton());
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            model.addElement(unit);
        }
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

        comboComponent.setRenderer(new UnitListCellRenderer());
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
