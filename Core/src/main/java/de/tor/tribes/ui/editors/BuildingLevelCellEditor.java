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

import de.tor.tribes.util.BuildingSettings;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;

/**
 *
 * @author extremeCrazyCoder
 */
public class BuildingLevelCellEditor extends DefaultCellEditor {
    private final JComboBox editComp;
    private final DefaultComboBoxModel editModel;
    private int curCol;
    
    public BuildingLevelCellEditor() {
        super(new JComboBox());
        editComp = (JComboBox) editorComponent;
        
        editModel = new DefaultComboBoxModel();
        for(int i = 1; i <= BuildingSettings.getMaxBuildingLevel("main"); i++)
            editModel.addElement(i);
        editComp.setModel(editModel);
        curCol = -1;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if(table.getModel() instanceof BuildingLevelModel) {
            String name = ((BuildingLevelModel) table.getModel()).getBuildingNameForColumn(column);
            if(name == null) return editComp;
            
            editModel.removeAllElements();
            for(int i =  BuildingSettings.getMinBuildingLevel(name); i <= BuildingSettings.getMaxBuildingLevel(name); i++)
                editModel.addElement(i);
            
            curCol = column;
        }
        return editComp;
    }
    
    public interface BuildingLevelModel {
        public String getBuildingNameForColumn(int column);
    }
}
