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

import de.tor.tribes.types.StorageStatus;
import de.tor.tribes.ui.components.FillingLabel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class StorageCellRenderer extends DefaultTableRenderer {

    private DefaultTableCellRenderer renderer = null;
    private FillingLabel label = null;

    public StorageCellRenderer() {
        super();
        renderer = new DefaultTableCellRenderer();
        label = new FillingLabel();
        label.setOpaque(true);
        label.setColors(new Color[]{new Color(187, 148, 70), new Color(242, 131, 30), new Color(224, 211, 209)});
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel defaultLabel = ((JLabel) c);
        label.setBackground(defaultLabel.getBackground());
        try {
            StorageStatus status = (StorageStatus) value;
            double wood = status.getWoodStatus();
            double clay = status.getClayStatus();
            double iron = status.getIronStatus();
            label.setData(new double[]{wood, clay, iron}, status.getCapacity());

        } catch (Exception e) {
        }
        return label;
    }
}
