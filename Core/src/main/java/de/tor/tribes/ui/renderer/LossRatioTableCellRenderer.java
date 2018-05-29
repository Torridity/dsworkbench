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
import java.text.NumberFormat;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class LossRatioTableCellRenderer extends DefaultTableRenderer {

    private NumberFormat format = NumberFormat.getInstance();

    public LossRatioTableCellRenderer() {
        super();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
    }

    public LossRatioTableCellRenderer(NumberFormat pCustomFormat) {
        this();
        format = pCustomFormat;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        ColoredProgressBar p = new ColoredProgressBar(0, 100);

        Double val = (Double) value;
        if (val == null) val = 0.0;
        if (val > 75.0) {
            p.setForeground(Color.RED);
        } else if (val > 25.0) {
            p.setForeground(Color.YELLOW);
        } else {
            p.setForeground(Color.GREEN);
        }

        p.setStringPainted(true);
        p.setValue((int) Math.round(val));

        return p;
    }
}
