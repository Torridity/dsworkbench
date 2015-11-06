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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import net.java.dev.colorchooser.ColorChooser;

/**
 *
 * @author Torridity
 */
public class ColorChooserCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final ColorChooser chooserComponent = new ColorChooser();

    public ColorChooserCellEditor(ActionListener pListener) {
        chooserComponent.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopCellEditing();
            }
        });
        chooserComponent.addActionListener(pListener);
    }

    @Override
    public Object getCellEditorValue() {
        return chooserComponent.getColor();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        chooserComponent.setColor((Color) value);
        return chooserComponent;
    }
}
