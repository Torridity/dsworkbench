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

import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.editors.NoteIconCellEditor.ICON_TYPE;
import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;

/**
 *
 * @author Torridity
 */
public class NoteIconListCellRenderer extends DefaultListCellRenderer {

    private ICON_TYPE type = ICON_TYPE.NOTE;

    public NoteIconListCellRenderer(ICON_TYPE pType) {
        type = pType;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        try {
            JLabel label = ((JLabel) c);
            label.setText("");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if (type.equals(ICON_TYPE.NOTE) && ((Integer) value) == -1) {
                BufferedImage symbol = ImageManager.getNoteIcon(-1);
                label.setIcon(new ImageIcon(symbol.getScaledInstance(18, 18, BufferedImage.SCALE_FAST)));
            } else {
                BufferedImage symbol = (type.equals(ICON_TYPE.NOTE)) ? ImageManager.getNoteSymbol((Integer) value) : ImageManager.getNoteIcon((Integer) value);
                label.setIcon(new ImageIcon(symbol.getScaledInstance(18, 18, BufferedImage.SCALE_FAST)));
            }
        } catch (Exception e) {
        }
        return c;
    }
}
