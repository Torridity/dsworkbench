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

import de.tor.tribes.types.StandardAttack;
import de.tor.tribes.ui.ImageManager;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 *
 * @author Torridity
 */
public class StandardAttackListCellRenderer implements ListCellRenderer {
    JLabel renderComponent = null;
    
    public StandardAttackListCellRenderer() {
        super();
        renderComponent = new JLabel();
        renderComponent.setHorizontalAlignment(SwingConstants.CENTER);
        renderComponent.setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object pValue, int pIndex, boolean pSelected, boolean pHasFocus) {
        try {
            StandardAttack att = (StandardAttack) pValue;
            if (pSelected) {
                renderComponent.setForeground(list.getSelectionForeground());
                renderComponent.setBackground(list.getSelectionBackground());
            } else {
                renderComponent.setBackground(list.getBackground());
                renderComponent.setForeground(list.getForeground());
            }
            
            renderComponent.setText(att.getName());
            if (att.getIcon() == ImageManager.NOTE_SYMBOL_NONE) {
                //no icon!?
                renderComponent.setIcon(null);
            } else {
                renderComponent.setIcon(new ImageIcon(ImageManager.getNoteSymbol(att.getIcon())));
            }
        } catch (ClassCastException e) {
            renderComponent.setText("-");
            renderComponent.setIcon(null);
        }

        return renderComponent;
    }
}
