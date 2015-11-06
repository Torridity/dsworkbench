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

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Torridity
 */
public class DrawNotDrawEditor extends AbstractCellEditor implements TableCellEditor {

    private JCheckBox editorComponent = null;

    public DrawNotDrawEditor() {
        editorComponent = new JCheckBox();
        try {
            editorComponent.setIcon(new ImageIcon(this.getClass().getResource("/res/ui/draw_small.gif")));
            editorComponent.setSelectedIcon(new ImageIcon(this.getClass().getResource("/res/ui/not_draw_small.gif")));
            editorComponent.setHorizontalAlignment(SwingConstants.CENTER);
        } catch (Exception e) {
            editorComponent.setIcon(null);
            editorComponent.setSelectedIcon(null);
        }
        editorComponent.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                stopCellEditing();
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        return editorComponent.isSelected();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editorComponent.setSelected((Boolean) value);
        editorComponent.setText(null);
        return editorComponent;
    }
}
