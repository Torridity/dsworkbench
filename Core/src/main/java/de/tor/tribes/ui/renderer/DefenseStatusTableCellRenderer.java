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

import de.tor.tribes.types.DefenseInformation.DEFENSE_STATUS;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class DefenseStatusTableCellRenderer extends DefaultTableRenderer {
    
    private ImageIcon unknown = null;
    private ImageIcon fine = null;
    private ImageIcon save = null;
    private ImageIcon dangerous = null;
    
    public DefenseStatusTableCellRenderer() {
        unknown = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/bullet_ball_grey.png"));
        fine = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/bullet_ball_yellow.png"));
        save = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/bullet_ball_green.png"));
        dangerous = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/bullet_ball_red.png"));
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        DEFENSE_STATUS val = (DEFENSE_STATUS) value;
        switch (val) {
            case DANGEROUS:
                label.setIcon(dangerous);
                break;
            case FINE:
                label.setIcon(fine);
                break;
            case SAVE:
                label.setIcon(save);
                break;
            default:
                label.setIcon(unknown);
                break;
        }
        label.setText("");
        return label;
    }
}
