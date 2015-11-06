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

import de.tor.tribes.types.TagMapMarker;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.editors.TagMapMarkerEditorImpl;
import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class TagMapMarkerRenderer extends DefaultTableRenderer {

    // private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    final private ImageIcon no_tag = new ImageIcon(TagMapMarkerEditorImpl.class.getResource("/res/remove.gif"));

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        TagMapMarker tagMarker = (TagMapMarker) value;
        Color selectColor = tagMarker.getTagColor();
        if (selectColor == null) {
            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(Constants.DS_ROW_A);
                } else {
                    c.setBackground(Constants.DS_ROW_B);
                }
            }
        } else {
            if (isSelected) {
                selectColor = selectColor.darker();
            }
            c.setBackground(selectColor);
        }

        ((JLabel) c).setText("");
        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        if (tagMarker.getTagIcon() >= 0) {
            ((JLabel) c).setIcon(new ImageIcon(ImageManager.getNoteSymbol(tagMarker.getTagIcon())));//ImageManager.getUnitIcon(tagMarker.getTagIcon(), false));
        } else {
            ((JLabel) c).setIcon(no_tag);
        }
        return c;
    }
}
