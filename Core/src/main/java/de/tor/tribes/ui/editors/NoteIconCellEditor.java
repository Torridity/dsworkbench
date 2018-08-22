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

import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.renderer.NoteIconListCellRenderer;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Torridity
 */
public class NoteIconCellEditor extends AbstractCellEditor implements TableCellEditor {

    public enum ICON_TYPE {
        NOTE, MAP
    }
    private JComboBox mEditor = new JComboBox();
    private ICON_TYPE type = ICON_TYPE.NOTE;

    public NoteIconCellEditor(ICON_TYPE pType) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        type = pType;

        switch (type) {
            case NOTE: {
                for (int i = -1; i <= ImageManager.MAX_NOTE_SYMBOL; i++) {
                    model.addElement(i);
                }
                break;
            }
            case MAP: {
                for (int i = -1; i <= ImageManager.ID_NOTE_ICON_13; i++) {
                    model.addElement(i);
                }
                break;
            }
        }


        mEditor.setModel(model);
        mEditor.setRenderer(new NoteIconListCellRenderer(type));
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
    public boolean isCellEditable(EventObject anEvent) {
        return !(anEvent instanceof MouseEvent) || ((MouseEvent) anEvent).getClickCount() >= 2;
    }

    @Override
    public Object getCellEditorValue() {
        return mEditor.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        mEditor.setSelectedItem(value);
        return mEditor;
    }
}
