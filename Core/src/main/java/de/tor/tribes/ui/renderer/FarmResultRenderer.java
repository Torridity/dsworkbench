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

import de.tor.tribes.types.FarmInformation;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class FarmResultRenderer extends DefaultTableRenderer {

    private ImageIcon okIcon = null;
    private ImageIcon impossibleIcon = null;
    private ImageIcon failedIcon = null;
    private ImageIcon disabledIcon = null;
    private ImageIcon unknownIcon = null;

    public FarmResultRenderer() {
        super();
        try {
            okIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/bullet_ball_green.png"));
            impossibleIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/bullet_ball_yellow.png"));
            failedIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/bullet_ball_red.png"));
            disabledIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/bullet_ball_grey.png"));
            unknownIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/bullet_ball_empty.png"));
        } catch (Exception ignored) {
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = ((JLabel) c);
        try {
            label.setText("");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            FarmInformation.FARM_RESULT status = (FarmInformation.FARM_RESULT) value;
            switch (status) {
                case FAILED:
                    label.setIcon(failedIcon);
                    break;
                case IMPOSSIBLE:
                    label.setIcon(impossibleIcon);
                    break;
                case FARM_INACTIVE:
                    label.setIcon(disabledIcon);
                    break;
                case UNKNOWN:
                    label.setIcon(unknownIcon);
                    break;
                default:
                    label.setIcon(okIcon);
            }
        } catch (Exception e) {
            label.setText("?");
            label.setIcon(null);
        }
        return label;
    }
}
