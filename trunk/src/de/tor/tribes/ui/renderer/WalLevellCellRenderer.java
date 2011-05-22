/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.SystemColor;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class WalLevellCellRenderer extends DefaultTableRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        //  JLabel label = (JLabel) c;

        Integer wallLevel = (Integer) value;

        FarbigeProgressBar p = new FarbigeProgressBar(0, 20);

        /*Color col = Color.GREEN;
        
        if (wallLevel > 5 && wallLevel < 15) {
        col = Color.YELLOW;
        } else if (wallLevel <= 5) {
        col = Color.RED;
        }*/
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

    class FarbigeProgressBar extends JProgressBar {

        public FarbigeProgressBar(int start, int end) {
            setMinimum(start);
            setMaximum(end);
            setForeground(SystemColor.window);
            setBackground(SystemColor.window);
            setBorder(new EmptyBorder(3, 5, 3, 5));
            Dimension size = new Dimension(300, 20);
            setPreferredSize(size);
            setMaximumSize(size);
            setMinimumSize(size);
            BasicProgressBarUI ui = new BasicProgressBarUI() {

                protected Color getSelectionForeground() {
                    return Color.BLACK;
                }

                protected Color getSelectionBackground() {
                    return Color.BLACK;
                }
            };
            setUI(ui);
        }
    }
}
