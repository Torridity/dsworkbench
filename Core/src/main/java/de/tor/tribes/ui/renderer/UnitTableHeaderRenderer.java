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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.ui.ImageManager;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Charon
 */
public class UnitTableHeaderRenderer extends DefaultTableCellRenderer {

    private DefaultTableHeaderRenderer defaultRenderer = null;

    public UnitTableHeaderRenderer() {
        defaultRenderer = new DefaultTableHeaderRenderer();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName((String) value);
        JLabel result = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (unit.equals(UnknownUnit.getSingleton())) {
            return result;
        }
        result.setIcon(ImageManager.getUnitIcon(DataHolder.getSingleton().getUnitByPlainName((String) value)));
        result.setText("");
        return result;

    }
}
