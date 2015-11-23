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

import de.tor.tribes.types.FightReport;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class FightReportCellRenderer extends DefaultTableRenderer {

    private ImageIcon BLUE_ICON = null;
    private ImageIcon GREEN_ICON = null;
    private ImageIcon YELLOW_ICON = null;
    private ImageIcon RED_ICON = null;
    private ImageIcon GREY_ICON = null;

    public FightReportCellRenderer() {
        super();
        try {
            BLUE_ICON = new ImageIcon(this.getClass().getResource("/res/ui/bullet_ball_blue.png"));
            GREEN_ICON = new ImageIcon(this.getClass().getResource("/res/ui/bullet_ball_green.png"));
            YELLOW_ICON = new ImageIcon(this.getClass().getResource("/res/ui/bullet_ball_yellow.png"));
            RED_ICON = new ImageIcon(this.getClass().getResource("/res/ui/bullet_ball_red.png"));
            GREY_ICON = new ImageIcon(this.getClass().getResource("/res/ui/bullet_ball_grey.png"));
        } catch (Exception e) {
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;

        FightReport rep = (FightReport) value;
        try {
            label.setText("");
            if (rep.areAttackersHidden()) {
                label.setIcon(GREY_ICON);
            } else if (rep.isSpyReport()) {
                label.setIcon(BLUE_ICON);
            } else if (rep.wasLostEverything()) {
                label.setIcon(RED_ICON);
            } else if (rep.wasLostNothing()) {
                label.setIcon(GREEN_ICON);
            } else {
                label.setIcon(YELLOW_ICON);
            }
           // label.setToolTipText(FightReportHTMLToolTipGenerator.buildToolTip(rep));
        } catch (Exception e) {
        }
        return label;
    }
}
