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
    public enum LayoutStyle {
        SENT_NOTSENT ("/res/ui/unsent_small.gif", "/res/ui/sent_small.gif"),
        DRAW_NOTDRAW ("/res/ui/not_draw_small.gif", "/res/ui/draw_small.gif"),
        VISIBLE_INVISIBLE ("/res/ui/eye_forbidden.png", "/res/ui/eye.png");
        
        private final String checkedImg;
        private final String uncheckedImg;
        LayoutStyle(String uncheckedImg, String checkedImg) {
            this.uncheckedImg = uncheckedImg;
            this.checkedImg = checkedImg;
        }
        public String checkedImg() { return checkedImg; }
        public String uncheckedImg() { return uncheckedImg; }
    }
    private JCheckBox editorComponent = null;
    private ImageIcon checked = null;
    private ImageIcon unchecked = null;
    
    public CustomCheckBoxEditor(LayoutStyle layout) {
        this(layout.uncheckedImg(), layout.checkedImg());
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
