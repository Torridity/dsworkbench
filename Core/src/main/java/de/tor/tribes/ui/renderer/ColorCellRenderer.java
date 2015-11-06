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
package de.tor.tribes.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Charon
 */
public class ColorCellRenderer extends DefaultTableRenderer {

    private boolean bMarkSelection = true;

    public ColorCellRenderer() {
    }

    public ColorCellRenderer(boolean pMarkSelection) {
        bMarkSelection = pMarkSelection;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel l = (JLabel) c;

        Color selectColor = (Color) value;
        if (isSelected && bMarkSelection && selectColor != null) {
            selectColor = selectColor.darker();
        }
        l.setBackground(selectColor);
        l.setForeground(selectColor);
        l.setText("");
        return l;
    }
}
