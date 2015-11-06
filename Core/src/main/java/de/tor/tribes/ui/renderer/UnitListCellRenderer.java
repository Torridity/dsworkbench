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

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.Constants;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 *
 * @author Torridity
 */
public class UnitListCellRenderer extends JLabel implements ListCellRenderer {

    public UnitListCellRenderer() {
        super();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object pValue, int pIndex, boolean pSelected, boolean pHasFocus) {
        // Component c = super.getListCellRendererComponent(list, pValue, pIndex, pSelected, pHasFocus);

        setOpaque(true);
        if (pSelected) {
            setForeground(list.getSelectionForeground());
            super.setBackground(list.getSelectionBackground());
        } else {
            if (pIndex % 2 == 0) {
                setBackground(Constants.DS_ROW_B);
            } else {
                setBackground(Constants.DS_ROW_A);
            }
        }

        try {
            setHorizontalAlignment(SwingConstants.CENTER);
            if (pValue == null) {
                //no icon!?
                setText("-");
                setIcon(null);
            } else {
                setText("");
                setIcon(ImageManager.getUnitIcon((UnitHolder) pValue));
            }
        } catch (Exception e) {
            //cast problem
        }

        return this;

    }
}
