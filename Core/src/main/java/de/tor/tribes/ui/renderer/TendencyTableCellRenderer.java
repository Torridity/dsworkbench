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

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class TendencyTableCellRenderer extends DefaultTableRenderer {

    private ImageIcon yellow = null;
    private ImageIcon red = null;
    private ImageIcon green = null;

    public TendencyTableCellRenderer() {
        yellow = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/yellow_arrow_horizontal.png"));
        red = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/red_arrow_up.png"));
        green = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/green_arrow_down.png"));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        Integer val = (Integer) value;
        String text = "";
        if (val == 0) {
            label.setIcon(yellow);
        } else if (val > 0) {
            label.setIcon(red);
            text = "(+ " + val + ")";
        } else if (val < 0) {
            label.setIcon(green);
            text = "(" + val + ")";
        }
        label.setText(StringUtils.center(text, 9));
        return label;
    }
}
