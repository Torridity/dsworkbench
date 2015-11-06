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

import de.tor.tribes.types.TagMapMarker;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Torridity
 */
public class TagMapMarkerCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final TagMapMarkerEditorImpl editor = new TagMapMarkerEditorImpl();

    @Override
    public Object getCellEditorValue() {
        TagMapMarker m = new TagMapMarker();
        m.setTagColor(editor.getColor());
        m.setTagIcon(editor.getIcon());
        return m;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        TagMapMarker marker = (TagMapMarker) value;
        editor.setColor(marker.getTagColor());
        editor.setIcon(marker.getTagIcon());
        return editor;
    }
}
