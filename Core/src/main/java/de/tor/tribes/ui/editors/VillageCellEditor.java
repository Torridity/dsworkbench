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

import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;

/**
 *
 * @author Torridity
 */
public class VillageCellEditor extends DefaultCellEditor {
    private JComboBox comboComponent = null;

    //TODO change the way this selection works
    public VillageCellEditor() {
        super(new JComboBox());
        comboComponent = (JComboBox) super.editorComponent;
        setClickCountToStart(2);
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Village v = (Village) value;
        Tribe t = v.getTribe();
        DefaultComboBoxModel model = null;
        if (t.equals(Barbarians.getSingleton())) {
            model = new DefaultComboBoxModel(new Village[]{v});
        } else {
            model = new DefaultComboBoxModel(v.getTribe().getVillageList());
        }
        comboComponent.setModel(model);
        comboComponent.setSelectedItem(value);
        return comboComponent;
    }
}
