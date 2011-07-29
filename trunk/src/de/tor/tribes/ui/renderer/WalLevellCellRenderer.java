/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class WalLevellCellRenderer extends DefaultTableRenderer {

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
