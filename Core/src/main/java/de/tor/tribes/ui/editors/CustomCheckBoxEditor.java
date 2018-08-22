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

import de.tor.tribes.ui.renderer.CustomBooleanRenderer;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;

/**
 *
 * @author extremeCrazyCoder
 */
public class CustomCheckBoxEditor extends DefaultCellEditor {
    private JCheckBox editorComponent = null;
    private ImageIcon checked = null;
    private ImageIcon unchecked = null;
    
    public CustomCheckBoxEditor(CustomBooleanRenderer.LayoutStyle layout) {
        this(layout.falseImg(), layout.trueImg());
    }
    
    /**
     * Use null for default Images
     * 
     * @param checkedImg Image that should be displayed if checked
     * @param uncheckedImg Image that should be displayed if not checked
     */
    public CustomCheckBoxEditor(String uncheckedImg, String checkedImg) {
        super(new JCheckBox());
        setClickCountToStart(0);
        
        editorComponent = (JCheckBox) super.editorComponent;
        editorComponent.setHorizontalAlignment(SwingConstants.CENTER);
        editorComponent.setText("");
        
        try {
            unchecked = new ImageIcon(this.getClass().getResource(uncheckedImg));
            checked = new ImageIcon(this.getClass().getResource(checkedImg));
        } catch (Exception e) {
            unchecked = null;
            checked = null;
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value != null && (Boolean) value) {
            editorComponent.setIcon(checked);
        } else {
            editorComponent.setIcon(unchecked);
        }
        
        return editorComponent;
    }
}
