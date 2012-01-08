/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.ui.components.ColoredProgressBar;
import de.tor.tribes.ui.util.ColorGradientHelper;
import de.tor.tribes.util.Constants;
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

    private NumberFormat format = NumberFormat.getInstance();

    public PercentCellRenderer() {
        super();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
    }

    public PercentCellRenderer(NumberFormat pCustomFormat) {
        this();
        format = pCustomFormat;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        ColoredProgressBar p = new ColoredProgressBar(0, 100);

        Float val = (Float) value * 100;

        Color color = null;

        if (row % 2 == 0) {
            color = Constants.DS_ROW_A;
        } else {
            color = Constants.DS_ROW_B;
        }
        p.setBackground(color);
        p.setForeground(ColorGradientHelper.getGradientColor(val, Color.RED, color));
        if (isSelected) {
            p.setBackground(table.getSelectionBackground());
        }
        p.setStringPainted(true);
        p.setValue(Math.round(val));

        return p;
    }
}
