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
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class StandardAttackTypeCellRenderer implements TableCellRenderer {

    private static Logger logger = Logger.getLogger("AttackDialog (StandardAttackTypeRenderer)");
    private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    private List<ImageIcon> icons = null;

    public StandardAttackTypeCellRenderer() {
        try {
            icons = new LinkedList<>();
            icons.add(new ImageIcon("./graphics/icons/axe.png"));
            icons.add(new ImageIcon("./graphics/icons/snob.png"));
            icons.add(new ImageIcon("./graphics/icons/def.png"));
            icons.add(new ImageIcon("./graphics/icons/fake.png"));
            icons.add(new ImageIcon("./graphics/icons/def_fake.png"));
        } catch (Exception e) {
            logger.warn("Failed to load attack type icons");
            icons = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        try {
            String type = (String) value;
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            switch (type) {
                case "Keiner":
                    //no icon!?
                    ((JLabel) c).setText("-");
                    ((JLabel) c).setIcon(null);
                    break;
                case "Off":
                    ((JLabel) c).setText("");
                    ((JLabel) c).setIcon(icons.get(0));
                    break;
                case "AG":
                    ((JLabel) c).setText("");
                    ((JLabel) c).setIcon(icons.get(1));
                    break;
                case "Unterstützung":
                    ((JLabel) c).setText("");
                    ((JLabel) c).setIcon(icons.get(2));
                    break;
                case "Fake":
                    ((JLabel) c).setText("");
                    ((JLabel) c).setIcon(icons.get(3));
                    break;
                case "Fake (Deff)":
                    ((JLabel) c).setText("");
                    ((JLabel) c).setIcon(icons.get(4));
                    break;
            }
        } catch (Exception e) {
            //cast problem
        }
        return c;
    }
}
