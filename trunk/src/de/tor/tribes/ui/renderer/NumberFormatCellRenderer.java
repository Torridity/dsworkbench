/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Jejkal
 */
public class NumberFormatCellRenderer extends DefaultTableRenderer {

    private NumberFormat format = NumberFormat.getInstance();

    public NumberFormatCellRenderer() {
        super();
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);

    }

    public NumberFormatCellRenderer(NumberFormat pCustomFormat) {
        this();
        format = pCustomFormat;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        if (value != null) {
            label.setText(format.format(value));
        } else {
            label.setText("0");
        }
        return label;
    }
}
