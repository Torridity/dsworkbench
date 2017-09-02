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

import de.tor.tribes.util.Constants;
import de.tor.tribes.util.village.KnownVillage;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.apache.log4j.Logger;

/**
 * @author Charon
 * @author extremeCrazyCoder
 */
public class WatchtowerLevelCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JComboBox mEditor = new JComboBox();

    public WatchtowerLevelCellEditor() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for(int i = 1; i <= KnownVillage.getMaxBuildingLevel("watchtower"); i++)
            model.addElement(i);
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
