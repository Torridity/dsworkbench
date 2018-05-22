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
import de.tor.tribes.ui.util.ColorGradientHelper;
import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Charon
 */
public class PercentCellRenderer extends DefaultTableRenderer {

    private DefaultTableCellRenderer renderer = null;
    private NumberFormat format = NumberFormat.getInstance();
    private boolean fromString = false;

    public PercentCellRenderer(boolean pFromString) {
        super();
        renderer = new DefaultTableCellRenderer();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        fromString = pFromString;
    }

    public PercentCellRenderer() {
        this(false);
    }

    public PercentCellRenderer(NumberFormat pCustomFormat) {
        this();
        format = pCustomFormat;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ColoredProgressBar p = new ColoredProgressBar(0, 100);
        p.setBackground(c.getBackground());
        p.setStringPainted(true);
        if (!fromString) {
            if(value == null) {
                value = 100.0f;
            }
            Float val = (Float) value * 100;
            p.setForeground(ColorGradientHelper.getGradientColor(val, Color.RED, c.getBackground()));
            p.setValue(Math.round(val));
        } else {
            float perc = 100.0f;
            String val = "-/-";
            if(value != null) {
                val = (String) value;
                String[] values = val.split("/");
                int first = Integer.parseInt(values[0]);
                int second = Integer.parseInt(values[1]);
                perc = (float) first / (float) second * 100;
            }
            p.setForeground(ColorGradientHelper.getGradientColor(perc, Color.RED, c.getBackground()));
            p.setValue(Math.round(perc));
            p.setString(val);
            
        }

        return p;
    }
}
