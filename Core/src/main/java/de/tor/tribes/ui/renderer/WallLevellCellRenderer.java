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

import de.tor.tribes.ui.components.ColoredProgressBar;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class WallLevellCellRenderer extends DefaultTableRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        //  JLabel label = (JLabel) c;

        Integer wallLevel = (Integer) value;

        ColoredProgressBar p = new ColoredProgressBar(0, 20);
        p.setForeground(getColor(wallLevel));
        p.setString(Integer.toString(wallLevel));
        p.setStringPainted(true);
        p.setValue(wallLevel);

        return p;
    }

    private Color getColor(int pWallLevel) {
        if (pWallLevel == 0) {
            //value is expired, stroke result
            return Color.RED;
        } else if (pWallLevel <= 20 && pWallLevel > 15) {
            float ratio = (float) (pWallLevel - 15) / (float) 15;
            Color c1 = Color.YELLOW;
            Color c2 = Color.GREEN;
            int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
            int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
            int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
            return new Color(red, green, blue);
        } else if (pWallLevel <= 15) {
            float ratio = (float) pWallLevel / (float) 15;
            Color c1 = Color.RED;
            Color c2 = Color.YELLOW;
            int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
            int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
            int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
            return new Color(red, green, blue);
        } else {
            //default renderer and color
            return Color.GREEN;
        }
    }
}
